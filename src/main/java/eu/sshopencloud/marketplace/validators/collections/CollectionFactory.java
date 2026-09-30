package eu.sshopencloud.marketplace.validators.collections;

import eu.sshopencloud.marketplace.dto.collections.CollectionCreationDto;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import eu.sshopencloud.marketplace.model.collections.Collection;
import eu.sshopencloud.marketplace.model.collections.CollectionContributor;
import eu.sshopencloud.marketplace.model.collections.CollectionItem;
import eu.sshopencloud.marketplace.model.items.CollectionThumbnail;
import eu.sshopencloud.marketplace.model.items.VersionedItem;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.repositories.actors.ActorRoleRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.collections.CollectionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Collection create(CollectionCreationDto collectionCreationDto, BeanPropertyBindingResult errors) {

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

    private void createThumbnail(CollectionCreationDto collectionCore, Collection collection, BeanPropertyBindingResult errors) {

        if (collectionCore.getThumbnail() != null && collectionCore.getThumbnail().getInfo() != null) {

            CollectionThumbnail collectionThumbnail = new CollectionThumbnail();

            if (collectionCore.getThumbnail().getInfo().getMediaId() == null) {
                errors.pushNestedPath("info");
                errors.rejectValue(
                        "mediaId", "field.required", "The field mediaId is required"
                );
                errors.popNestedPath();
                return;
            }
            collectionThumbnail.setThumbnailId(collectionCore.getThumbnail().getInfo().getMediaId());
            collectionThumbnail.setCaption(collectionCore.getThumbnail().getCaption());

            collectionThumbnail.setCollection(collection);
            collection.setThumbnail(collectionThumbnail);
        }
    }

    public List<CollectionContributor> createContributors(CollectionCreationDto collectionCreationDto,
                                                          Collection collection, BeanPropertyBindingResult errors) {
        List<CollectionContributor> contributors = new ArrayList<>();

        if (collectionCreationDto.getContributors() != null) {

            for(int i = 0; i < collectionCreationDto.getContributors().size(); i++) {
                errors.pushNestedPath("contributors[" + i + "]");
                CollectionContributor ctrb = new CollectionContributor();
                if (collectionCreationDto.getContributors().get(i).getActor() != null) {
                    Optional<Actor> actor = actorRepository.findById(collectionCreationDto.getContributors().get(i).getActor().getId());
                    if (actor.isPresent()) {
                        ctrb.setActor(actor.get());
                    } else {

                        errors.rejectValue("actor", "field.invalid", "Such actor does not exist");
                    }
                } else {
                    errors.rejectValue("actor", "field.required", "The field actor is required");
                }
                if (collectionCreationDto.getContributors().get(i).getRole() != null) {
                    Optional<ActorRole> role = actorRoleRepository.findById(collectionCreationDto.getContributors().get(i).getRole().getCode());
                    if (role.isPresent()) {
                        ctrb.setRole(role.get());
                    } else {
                        errors.rejectValue("role", "field.invalid", "Such role does not exist");
                    }
                } else {
                    errors.rejectValue("role", "field.required", "The field role is required");
                }

                ctrb.setCollection(collection);
                contributors.add(ctrb);
                errors.popNestedPath();
            }
        }
        return contributors;
    }
}
