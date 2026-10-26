package eu.sshopencloud.marketplace.validators.collections;

import org.springframework.security.access.AccessDeniedException;

import eu.sshopencloud.marketplace.dto.collections.CollectionCreationDto;
import eu.sshopencloud.marketplace.dto.items.ItemMediaCore;
import eu.sshopencloud.marketplace.dto.items.MediaDetailsId;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import eu.sshopencloud.marketplace.model.collections.Collection;
import eu.sshopencloud.marketplace.model.collections.CollectionContributor;
import eu.sshopencloud.marketplace.model.collections.CollectionItem;
import eu.sshopencloud.marketplace.model.items.CollectionThumbnail;
import eu.sshopencloud.marketplace.model.items.VersionedItem;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.repositories.actors.ActorRoleRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.collections.CollectionException;
import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;


import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service responsible for creating new {@link eu.sshopencloud.marketplace.model.collections.Collection}
 * based on the provided data;
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CollectionFactory {

    private final ActorRepository actorRepository;
    private final ActorRoleRepository actorRoleRepository;
    private final VersionedItemRepository versionedItemRepository;
    private final MediaStorageService mediaStorageService;
    private final ConceptRepository conceptRepository;

    public Collection create(CollectionCreationDto collectionCreationDto, BeanPropertyBindingResult errors) {

        // note that only moderators can set the recommended status of a collection
        if (collectionCreationDto.isRecommended() && LoggedInUserHolder.getLoggedInUser().isModerator())
            throw new AccessDeniedException("Only moderators can set the recommended status of a collection");

        Collection collection = new Collection();

        collection.setTitle(collectionCreationDto.getTitle());
        collection.setDescription(collectionCreationDto.getDescription());
        collection.setVisible(collectionCreationDto.isVisible());
        
        collection.setRecommended(collectionCreationDto.isRecommended());

        collection.setOwner(LoggedInUserHolder.getLoggedInUser());
        collection.setCreatedAt(ZonedDateTime.now(ZoneId.systemDefault()));
        collection.setUpdatedAt(ZonedDateTime.now(ZoneId.systemDefault()));

        collection.setContributors(createContributors(collectionCreationDto, collection, errors));

        collectionCreationDto.getContainedItems().forEach(item -> {
            VersionedItem versionedItem =
                    versionedItemRepository.findById(item).orElseThrow(() -> new CollectionException("Item not found"));
            CollectionItem collectionItem = new CollectionItem();
            collectionItem.setCollection(collection);
            collectionItem.setItem(versionedItem.getCurrentVersion());
            collection.getCollectionItems().add(collectionItem);
        });

        createThumbnail(collectionCreationDto, collection, errors);

        return collection;
    }

    public void createThumbnail(CollectionCreationDto collectionCore, Collection collection, BeanPropertyBindingResult errors) {

        if (collectionCore.getThumbnail() != null) {
            errors.pushNestedPath("thumbnail");
            
            ItemMediaCore thumbnail = collectionCore.getThumbnail();

            CollectionThumbnail collectionThumbnail = new CollectionThumbnail();

            if (thumbnail.getInfo() != null) {
                errors.pushNestedPath("info");

                MediaDetailsId info = thumbnail.getInfo();

                if (info.getMediaId() == null) {
                    errors.rejectValue(
                            "mediaId", "field.required", "The field mediaId is required"
                    );
                } else if (!mediaStorageService.ensureMediaAvailable(info.getMediaId())) {
                    errors.rejectValue(
                            "mediaId", "field.invalid", "The media with the given mediaId does not exist"
                    );
                } else {
                    
                    collectionThumbnail.setThumbnailId(info.getMediaId());
                    collectionThumbnail.setCaption(thumbnail.getCaption());
                }

                errors.popNestedPath();
            }

            if (thumbnail.getConcept() != null) {
                errors.pushNestedPath("concept");

                ConceptId conceptId = thumbnail.getConcept();

                if (conceptId.getCode() == null) {
                    errors.rejectValue("code", "field.required",
                        "concept code is a required field");
                } else if (conceptId.getVocabulary() == null) {
                    errors.rejectValue("vocabulary", "field.required",
                        "concept vocabulary is a required field");
                } else if (conceptId.getVocabulary().getCode() == null) {
                    errors.pushNestedPath("vocabulary");
                    errors.rejectValue("code", "field.required",
                        "code is a required field");
                    errors.popNestedPath();
                } else {
                    Optional<Concept> concept = conceptRepository.findByCodeAndVocabularyCode(
                        conceptId.getCode(),conceptId.getVocabulary().getCode());

                    if (concept.isPresent()) {
                        collectionThumbnail.setConcept(concept.get());
                    } else {
                        errors.rejectValue("concept", "field.invalid",
                            "specified concept does not exist");
                    }
                }

                errors.popNestedPath();
            }

            if (!errors.hasErrors()) {
                collectionThumbnail.setCollection(collection);
                collection.setThumbnail(collectionThumbnail);
            }

            errors.popNestedPath();
        }
    }

    public List<CollectionContributor> createContributors(CollectionCreationDto collectionCreationDto,
                                                          Collection collection, BeanPropertyBindingResult errors) {
        List<CollectionContributor> contributors = new ArrayList<>();
        Set<Map.Entry<Long, String>> actorRoles = new HashSet<>();

        if (collectionCreationDto.getContributors() != null) {

            for(int i = 0; i < collectionCreationDto.getContributors().size(); i++) {
                errors.pushNestedPath("contributors[" + i + "]");
                CollectionContributor ctrb = new CollectionContributor();

                ActorId actorId = collectionCreationDto.getContributors().get(i).getActor();
                if (actorId != null) {
                    errors.pushNestedPath("actor");
                    if (actorId.getId() != null) {

                        Optional<Actor> actor = actorRepository.findById(actorId.getId());
                        if (actor.isPresent()) {
                            ctrb.setActor(actor.get());
                        } else {
                            errors.rejectValue(
                                "id", "field.invalid",
                                String.format("actor with id %s does not exist", actorId.getId()));
                        }
                    } else {
                        errors.rejectValue("id", "field.required", "actor must have an id field");
                    }
                    errors.popNestedPath();
                } else {
                    errors.rejectValue("actor", "field.required", "The field actor is required");
                }

                ActorRoleId roleId = collectionCreationDto.getContributors().get(i).getRole();
                if (roleId != null) {
                    errors.pushNestedPath("role");
                    if (roleId.getCode() != null) {
                        Optional<ActorRole> role = actorRoleRepository.findById(roleId.getCode());
                        if (role.isPresent()) {
                            ctrb.setRole(role.get());
                        } else {
                            errors.rejectValue(
                                "code", "field.invalid",
                                String.format("Role with code %s does not exist", roleId.getCode()));
                        }
                    } else {
                        errors.rejectValue("code", "field.required",
                            "contributor role must have a code field");
                    }
                    errors.popNestedPath();
                } else {
                    errors.rejectValue("role", "field.required",
                        "The field role is required");
                }

                if (!errors.hasErrors()) {
                    ctrb.setCollection(collection);

                    Map.Entry<Long, String> actorRoleEntry = Map.entry(
                        ctrb.getActor().getId(), ctrb.getRole().getCode()
                    );

                    if (!actorRoles.contains(actorRoleEntry)) {
                        contributors.add(ctrb);
                        actorRoles.add(actorRoleEntry);
                    }
                    else {
                        errors.rejectValue(
                                "", "field.repeated",
                                String.format("The actor with given role (%s) has already occurred.", ctrb.getRole().getLabel())
                        );
                    }
                }

                errors.popNestedPath();
            }
        }
        return contributors;
    }
}
