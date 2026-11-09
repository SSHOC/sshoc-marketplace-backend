package eu.sshopencloud.marketplace.validators.collections;

import org.springframework.security.access.AccessDeniedException;

import eu.sshopencloud.marketplace.dto.collections.CollectionCreationDto;
import eu.sshopencloud.marketplace.dto.items.ItemMediaCore;
import eu.sshopencloud.marketplace.dto.items.MediaDetailsId;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
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
import eu.sshopencloud.marketplace.validators.items.ItemMediaFactory;
import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.domain.media.exception.MediaNotAvailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;


import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final ItemMediaFactory itemMediaFactory;

    public Collection create(CollectionCreationDto collectionCreationDto, BeanPropertyBindingResult errors) {

        // note that only moderators can set the recommended status of a collection
        if (collectionCreationDto.isRecommended() && !LoggedInUserHolder.getLoggedInUser().isModerator())
            throw new AccessDeniedException("Only moderators can set the recommended status of a collection");

        Collection collection = new Collection();

        collection.setTitle(collectionCreationDto.getTitle());
        collection.setDescription(collectionCreationDto.getDescription());
        collection.setVisible(collectionCreationDto.isVisible());
        
        collection.setRecommended(collectionCreationDto.isRecommended());

        collection.setOwner(LoggedInUserHolder.getLoggedInUser());
        collection.setCreatedAt(ZonedDateTime.now(ZoneId.systemDefault()));
        collection.setUpdatedAt(ZonedDateTime.now(ZoneId.systemDefault()));

        createContributors(collectionCreationDto, collection, errors);

        createContainedItems(collectionCreationDto, collection, errors);

        createThumbnail(collectionCreationDto, collection, errors);

        if (collection.getThumbnail() != null) {
            try {
                mediaStorageService.linkToMedia(collection.getThumbnail().getThumbnailId());
            } catch (MediaNotAvailableException e) {
                // Note that this should be impossible as we explicitly check that the media
                // is available in the createThumbnail method, but we catch it here just in case
                errors.rejectValue(
                    "thumbnail.info.mediaId",
                    "field.invalid",
                    "The media with the given mediaId is not available"
                );
            }
        }

        return collection;
    }

    public List<Long> createContainedItems(CollectionCreationDto collectionCreationDto, Collection collection, BeanPropertyBindingResult errors) {

        // 1. get the incoming item IDs from the DTO, preserving order and uniqueness
        Set<String> incomingItemIds = new LinkedHashSet<>(collectionCreationDto.getContainedItems());

        // 2. Identify which items are being dropped BEFORE we clear anything
        List<Long> itemsRemoved = collection.getCollectionItems().stream()
                .filter(existingItem -> !incomingItemIds.contains(existingItem.getItem().getVersionedItem().getPersistentId()))
                .map(existingItem -> existingItem.getItem().getId())
                .collect(Collectors.toList());

        // 3. map between exiting items in the collection and their persistent IDs for quick lookup
        Map<String, CollectionItem> existingItemsMap = collection.getCollectionItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getItem().getVersionedItem().getPersistentId(),
                        item -> item,
                        (existing, replacement) -> existing // Guard against unexpected duplicates
                ));

        // 4. Construct a fresh list to enforce the sequence provided in the payload
        List<CollectionItem> orderedCollectionItems = new ArrayList<>();

        // we need to fetch the full VersionedItem objects from the database so that we
        // can get the current version of each item, and also to validate that they exist.
        // the easy option would have been to use findById in the loop below, but that
        // would result in a separate database query for each item which for large collections
        // might be a problem. Instead we fetch them all in one go and then map them by their
        // persistent ID for quick lookup.
        Map<String, VersionedItem> versionedItemMap =
            versionedItemRepository.findAllById(incomingItemIds).stream()
            .collect(Collectors.toMap(VersionedItem::getPersistentId, item -> item));

        for (String id : incomingItemIds) {

            // get the VersionedItem from the map we created above, and if it doesn't exist
            // then throw an exception. This will result in a 400 Bad Request response being
            // returned to the client, which is the same as before.
            VersionedItem versionedItem = versionedItemMap.get(id);
            if (versionedItem == null) {
                throw new CollectionException("Item not found");
            }

            if (existingItemsMap.containsKey(id)) {
                // if this item was already in the collection, reuse the existing record
                // which should avoid us loosing any associated metdata like comments or
                // suggested status

                // note that we still need to update the item reference in case the item
                // has been updated since it was added to the collection, otherwise it
                // will be pinned to an old version which will also cause problems with
                // the solr indexer which expects the current version to be present in
                // the collection
                CollectionItem existingItem = existingItemsMap.get(id);
                existingItem.setItem(versionedItem.getCurrentVersion());

                orderedCollectionItems.add(existingItem);
            } else {
                // If it's a new item get the full item from the database and create
                // a new CollectionItem record for it
                CollectionItem collectionItem = new CollectionItem();
                collectionItem.setCollection(collection);
                collectionItem.setItem(versionedItem.getCurrentVersion());
                orderedCollectionItems.add(collectionItem);
            }
        }

        // 5. Replace the contents of the collection list with the correctly
        //    ordered list, so that updates don't cause items to be reordered
        //    which would make them jump around even if the list of items in
        //    the collection hadn't changed
        collection.getCollectionItems().clear();
        collection.getCollectionItems().addAll(orderedCollectionItems);

        // return the list of items that were removed from the collection so
        // that the calling method can handle any necessary cleanup, inlcluding
        // removing them from the solr index
        return itemsRemoved;
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
                            "mediaId", "field.notExist", "The media with the given mediaId does not exist"
                    );
                } else {
                    
                    collectionThumbnail.setThumbnailId(info.getMediaId());
                    collectionThumbnail.setCaption(thumbnail.getCaption());
                }

                errors.popNestedPath();
            } else {
                errors.rejectValue("info", "field.required", "The field info is required");
            }

            if (thumbnail.getConcept() != null) {
                errors.pushNestedPath("concept");
                collectionThumbnail.setConcept(itemMediaFactory.createLicenseConcept(thumbnail.getConcept(), errors));
                errors.popNestedPath();
            }

            if (!errors.hasErrors()) {
                collectionThumbnail.setCollection(collection);
                collection.setThumbnail(collectionThumbnail);
            }

            errors.popNestedPath();
        }
    }
    
    public void createContributors(CollectionCreationDto collectionCreationDto,
                                    Collection collection, BeanPropertyBindingResult errors) {
        
        if (collection.getContributors() == null) {
            // if we are called during the initial creation of a collection then the
            // list of contributors is null, which messes up the logic below, so we need
            // to initialize it to an empty list
            collection.setContributors(new ArrayList<>());
        }

        // Create a map of existing contributors for quick lookup,
        // using a composite key of actor ID and role code        
        Map<String, CollectionContributor> existingContributorsMap = collection.getContributors().stream()
            .filter(c -> c.getActor() != null && c.getRole() != null)
            .collect(Collectors.toMap(
                c -> c.getActor().getId() + "-" + c.getRole().getCode(),
                c -> c,
                (existing, replacement) -> existing
            ));

        // this will hold the new list of contributors that will contain a mixture of
        // existing contributors and new contributors, in the order they were provided
        List<CollectionContributor> mergedContributors = new ArrayList<>();
        
        if (collectionCreationDto.getContributors() != null) {

            // this allows us to check for duplicates in the incoming list of contributors,
            // so that we can reject them
            Set<String> processedIncomingKeys = new HashSet<>();

            for (int i = 0; i < collectionCreationDto.getContributors().size(); i++) {
                errors.pushNestedPath("contributors[" + i + "]");
                
                Actor targetActor = null;
                ActorRole targetRole = null;

                // Here we check that the contributor has a valid actor
                // ID that is present in the database, and depending where
                // we find a problem reject the appropriate field with a message.
                ActorId actorId = collectionCreationDto.getContributors().get(i).getActor();
                if (actorId != null) {
                    errors.pushNestedPath("actor");
                    if (actorId.getId() != null) {
                        Optional<Actor> actor = actorRepository.findById(actorId.getId());
                        if (actor.isPresent()) {
                            targetActor = actor.get();
                        } else {
                            errors.rejectValue("id", "field.invalid",
                                String.format("actor with id %s does not exist", actorId.getId()));
                        }
                    } else {
                        errors.rejectValue("id", "field.required", "actor must have an id field");
                    }
                    errors.popNestedPath();
                } else {
                    errors.rejectValue("actor", "field.required", "The field actor is required");
                }

                // Here we check that the contributor has a valid role
                // ID that is present in the database, and depending where
                // we find a problem reject the appropriate field with a message.
                ActorRoleId roleId = collectionCreationDto.getContributors().get(i).getRole();
                if (roleId != null) {
                    errors.pushNestedPath("role");
                    if (roleId.getCode() != null) {
                        Optional<ActorRole> role = actorRoleRepository.findById(roleId.getCode());
                        if (role.isPresent()) {
                            targetRole = role.get();
                        } else {
                            errors.rejectValue("code", "field.invalid",
                                String.format("Role with code %s does not exist", roleId.getCode()));
                        }
                    } else {
                        errors.rejectValue("code", "field.required", "contributor role must have a code field");
                    }
                    errors.popNestedPath();
                } else {
                    errors.rejectValue("role", "field.required", "The field role is required");
                }

                if (targetActor != null && targetRole != null) {
                    // assuming the contributor has both a valid actor and role then....

                    // we build the key the same as we did for the existing contributors
                    String compositeKey = targetActor.getId() + "-" + targetRole.getCode();

                    if (!processedIncomingKeys.contains(compositeKey)) {
                        // assuming we've not alreay processed this actor/role combination...
                        processedIncomingKeys.add(compositeKey);

                        if (existingContributorsMap.containsKey(compositeKey)) {
                            // if the contributor was already present in the list, re-use
                            // the existing instance
                            mergedContributors.add(existingContributorsMap.get(compositeKey));
                        } else {
                            // if not then create a new instance and add it to the list
                            CollectionContributor ctrb = new CollectionContributor();
                            ctrb.setCollection(collection);
                            ctrb.setActor(targetActor);
                            ctrb.setRole(targetRole);
                            mergedContributors.add(ctrb);
                        }
                    } else {
                        errors.rejectValue("", "field.repeated",
                            String.format("The actor with given role (%s) has already occurred.", targetRole.getLabel()));
                    }
                }

                errors.popNestedPath();
            }
        }

        if (!errors.hasErrors()) {
            // assuming there have been no validation errors we now need to update the
            // collections list of contributors using the mixture of old and new instances.

            // unfortunatelty we can't just clear the list and add all the new instances
            // because that is guaranteed to cause a DuplicateKeyException if any of the
            // contributors are being re-used, so we need to carefully update the list in
            // place. Note this is different to the way we updated the collection items
            // because CollectionContributor uses a composite key, whereas it is safe
            // to clear out CollectionItem which uses a simple auto-generated Id

            // this is just a convenient shorthand so we don't need to keep typing
            // collection.getContributors() over and over again!
            List<CollectionContributor> targetList = collection.getContributors();
            
            // 1. Loop through the exact ordered sequence of reconciled contributors
            for (int index = 0; index < mergedContributors.size(); index++) {
                CollectionContributor incoming = mergedContributors.get(index);
                
                if (index < targetList.size()) {
                    // Overwrite the slot directly if it's a different object instance
                    if (targetList.get(index) != incoming) {
                        targetList.set(index, incoming);
                    }
                } else {
                    // Append if we have grown beyond the original size boundaries
                    targetList.add(incoming);
                }
            }
            
            // 2. Trim away any trailing leftover slots if the incoming collection shrank
            if (targetList.size() > mergedContributors.size()) {
                targetList.subList(mergedContributors.size(), targetList.size()).clear();
            }
        }
    }
}
