package eu.sshopencloud.marketplace.services.collections;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.collections.*;
import eu.sshopencloud.marketplace.dto.inbox.MessageDto;
import eu.sshopencloud.marketplace.mappers.collections.CollectionListMapper;
import eu.sshopencloud.marketplace.mappers.collections.CollectionMapper;
import eu.sshopencloud.marketplace.model.collections.Collection;
import eu.sshopencloud.marketplace.model.collections.CollectionItem;
import eu.sshopencloud.marketplace.model.items.VersionedItem;
import eu.sshopencloud.marketplace.repositories.collections.CollectionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.inbox.MessagesService;
import eu.sshopencloud.marketplace.services.search.IndexCollectionService;
import eu.sshopencloud.marketplace.services.search.IndexItemService;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.collections.CollectionFactory;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final VersionedItemRepository versionedItemRepository;
    private final IndexCollectionService indexCollectionService;
    private final IndexItemService indexItemService;
    private final MessagesService messagesService;
    private final CollectionFactory collectionFactory;

    public CollectionService(CollectionRepository collectionRepository,
                             VersionedItemRepository versionedItemRepository, IndexCollectionService indexCollectionService, IndexItemService indexItemService, MessagesService messagesService,
                             CollectionFactory collectionFactory) {
        this.collectionRepository = collectionRepository;
        this.versionedItemRepository = versionedItemRepository;
        this.indexCollectionService = indexCollectionService;
        this.indexItemService = indexItemService;
        this.messagesService = messagesService;
        this.collectionFactory = collectionFactory;
    }

    public CollectionDto createCollection(CollectionCreationDto collectionCore) {

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(collectionCore, "Collection");

        Collection collection = collectionFactory.create(collectionCore, errors);

        if (errors.hasErrors())
            throw new ValidationException(errors);

        collectionRepository.save(collection);
        indexCollectionService.indexCollection(collection);
        indexItemService.reIndexItemsInCollection(collection);

        return CollectionMapper.INSTANCE.toDto(collection, PageCoords.builder().page(1).perpage(20).build());
    }

    public PaginatedCollections getCollections(PageCoords pageCoords, CollectionsReadMode mode) {
        switch (mode) {
            case PUBLIC -> {
                return getPublicCollections(pageCoords);
            }
            case PRIVATE -> {
                return getUserPrivateCollections(pageCoords);
            }
            case OWNED_BY_CALLER -> {
                return getCollectionsOwnedByCaller(pageCoords);
            }
            default -> {
                return PaginatedCollections.builder().collections(Collections.emptyList()).count(0).hits(0).page(0).perpage(0).pages(0).build();
            }
        }
    }

    private PaginatedCollections getPublicCollections(PageCoords pageCoords) {
        PageRequest pageRequest = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage());
        Page<Collection> collections = collectionRepository.findAllByVisibleTrue(pageRequest);
        return PaginatedCollections.builder().collections(CollectionListMapper.INSTANCE.toDto(collections.getContent())).count(collections.getContent().size()).hits(collections.getTotalElements()).page(pageRequest.getPageNumber()).perpage(pageRequest.getPageSize()).pages(collections.getTotalPages()).build();
    }

    private PaginatedCollections getUserPrivateCollections(PageCoords pageCoords) {
        if (LoggedInUserHolder.getLoggedInUser() == null) {
            return PaginatedCollections.builder().collections(Collections.emptyList()).count(0).hits(0).page(0).perpage(0).pages(0).build();
        }
        PageRequest pageRequest = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage());
        Page<Collection> collections =
                collectionRepository.findAllByOwnerAndVisible(LoggedInUserHolder.getLoggedInUser(), false, pageRequest);
        return PaginatedCollections.builder().collections(CollectionListMapper.INSTANCE.toDto(collections.getContent())).count(collections.getContent().size()).hits(collections.getTotalElements()).page(pageRequest.getPageNumber()).perpage(pageRequest.getPageSize()).pages(collections.getTotalPages()).build();
    }

    private PaginatedCollections getCollectionsOwnedByCaller(PageCoords pageCoords) {
        if (LoggedInUserHolder.getLoggedInUser() == null) {
            return PaginatedCollections.builder().collections(Collections.emptyList()).count(0).hits(0).page(0).perpage(0).pages(0).build();
        }
        PageRequest pageRequest = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage());
        Page<Collection> collections =
                collectionRepository.findAllByOwner(LoggedInUserHolder.getLoggedInUser(), pageRequest);
        return PaginatedCollections.builder().collections(CollectionListMapper.INSTANCE.toDto(collections.getContent())).count(collections.getContent().size()).hits(collections.getTotalElements()).page(pageRequest.getPageNumber()).perpage(pageRequest.getPageSize()).pages(collections.getTotalPages()).build();
    }

    public CollectionDto getCollection(long id, PageCoords pageCoords) {
        Collection collection =
                collectionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(String.format(
                        "Collection with id %s not found", id)));

        return CollectionMapper.INSTANCE.toDto(collection, pageCoords);
    }

    public CollectionDto updateCollection(long collectionId, CollectionCreationDto collectionCreationDto) {
        
        // we need to acutally retrieve the old value, not just check it exists, as we need the
        // owner information to check that the update is actually allowed
        Collection oldcollection = collectionRepository.findById(collectionId).orElseThrow(() ->
            new EntityNotFoundException(String.format("Collection with id %s not found", collectionId)));

        if (isCollectionUpdatePossible(oldcollection)) {

            // create a new collection instance to hold the updated
            // collection information
            Collection collection = new Collection();
            
            // because we are creating a new copy of the collection
            // so we can roll back on exceptions etc. we need to copy
            // the id, owner, and creation times from the original
            collection.setId(collectionId);
            collection.setOwner(oldcollection.getOwner());
            collection.setCreatedAt(oldcollection.getCreatedAt());

            // a placeholder for any validation errors which may occuir
            BeanPropertyBindingResult errors = new BeanPropertyBindingResult(collectionCreationDto, "Collection");

            // we need to copy the flag across so we check to ensure the new and old
            // values are the same, otherwise we throw an exception if the user is not a moderator
            if (collectionCreationDto.isRecommended() != oldcollection.isRecommended() && !LoggedInUserHolder.getLoggedInUser().isModerator()) {
                throw new AccessDeniedException("Only moderators can set the recommended status of a collection");
            }

            // copy the simple single value fields into the new instance
            collection.setTitle(collectionCreationDto.getTitle());
            collection.setDescription(collectionCreationDto.getDescription());
            collection.setVisible(collectionCreationDto.isVisible());
            collection.setRecommended(collectionCreationDto.isRecommended());
            
            collectionCreationDto.getContainedItems().forEach(item -> {
                VersionedItem versionedItem =
                        versionedItemRepository.findById(item).orElseThrow(() -> new CollectionException("Item not found"));
                CollectionItem collectionItem = new CollectionItem();
                collectionItem.setCollection(collection);
                collectionItem.setItem(versionedItem.getCurrentVersion());
                collection.getCollectionItems().add(collectionItem);
            });

            // create the collection thumbnail
            collectionFactory.createThumbnail(collectionCreationDto, collection, errors);
            
            // handle contributors to the collection
            collection.setContributors(collectionFactory.createContributors(collectionCreationDto, collection, errors));

            if (errors.hasErrors()) {
                // unfortunately we've hit one or more validation errors
                throw new ValidationException(errors);
            }

            // looks like the new instance is valid so correct
            // the updated time field
            collection.setUpdatedAt(ZonedDateTime.now());


            // keep track of the items we need to remove from the solr index
            // we need to do this now otherwise the object will change when we
            // save the new version back to the repository
            List<Long> oldValues = oldcollection.getCollectionItems().stream().map(item->item.getItem().getId()).collect(Collectors.toUnmodifiableList());

            // and save the new instance of the collection
            Collection saved_collection = collectionRepository.save(collection);

            // make sure the collection is indexed properly
            indexCollectionService.indexCollection(saved_collection);

            // now remove the old values from the index
            indexItemService.removeOldValues(oldValues, saved_collection.getId());
            
            // and then index the new values
            indexItemService.reIndexItemsInCollection(saved_collection);

            // return the saved instance in the expected form
            return CollectionMapper.INSTANCE.toDto(saved_collection, PageCoords.builder().page(1).perpage(20).build());
        } else {
            // oops the caller doesn't have permissions to update this collection
            throw new AccessDeniedException("Access denied");
        }
    }

    public void addSuggestionToCollection(long collectionId, CollectionSuggestionCreationDto suggestionCreationDto) {
        Collection collection =
                collectionRepository.findById(collectionId).orElseThrow(() -> new EntityNotFoundException(String.format("Collection with id %s not found", collectionId)));

        if (isCollectionSuggestionAddingPossible(collection)) {
            VersionedItem versionedItem =
                    versionedItemRepository.findById(suggestionCreationDto.getItemPersistentId()).orElseThrow(() -> new CollectionException("Provided persistenId does not exist"));

            CollectionItem collectionItem = new CollectionItem();
            collectionItem.setSuggested(true);
            collectionItem.setCollection(collection);
            collectionItem.setItem(versionedItem.getCurrentVersion());
            collectionItem.setComment(suggestionCreationDto.getComment());
            collection.getCollectionItems().add(collectionItem);

            MessageDto messageDto = new MessageDto();
            messageDto.setContent("There is a new suggestion for your collection: " + collection.getTitle());

            messagesService.sendMessageToUser(messageDto, collection.getOwner());
        } else {
            throw new AccessDeniedException("Access denied");
        }
    }

    public void changeCollectionSuggestionStatus(long collectionId, long suggestionId,  CollectionSuggestionStatusActionDto collectionSuggestionStatusActionDto) {
        Collection collection =
                collectionRepository.findById(collectionId).orElseThrow(() -> new EntityNotFoundException(String.format("Collection with id %s not found", collectionId)));

        if (isCollectionSuggestionStatusPossibleToChange(collection)) {
            CollectionSuggestionStatusActionDto.ACTION action = collectionSuggestionStatusActionDto.getAction();

            if (action == CollectionSuggestionStatusActionDto.ACTION.APPROVE) {
                collection.getCollectionItems().stream()
                        .filter(item -> item.getId().equals(suggestionId))
                        .findFirst()
                        .ifPresent(item -> {
                            item.setSuggested(false);
                            item.setComment(null);
                        });
            }

            if (action == CollectionSuggestionStatusActionDto.ACTION.REJECT) {
                collection.getCollectionItems().removeIf(item -> item.getId().equals(suggestionId));
            }
        }
    }

    public void deleteCollection(long collectionId) {
        Optional<Collection> collection = collectionRepository.findById(collectionId);

        if (collection.isEmpty()) {
            throw new EntityNotFoundException(String.format("Collection with id %s not found", collectionId));
        }

        if (!isCollectionUpdatePossible(collection.get())) {
            throw new AccessDeniedException("Access denied");
        }

        collectionRepository.deleteById(collectionId);
        indexCollectionService.removeFromIndex(collection.get());
    }


    private boolean isCollectionUpdatePossible(Collection collection) {
        if (collection.getOwner().equals(LoggedInUserHolder.getLoggedInUser())) {
            return true;
        }
        return LoggedInUserHolder.getLoggedInUser().isModerator();
    }


    private boolean isCollectionSuggestionAddingPossible(Collection collection) {
        if(collection.isVisible() || collection.getOwner().equals(LoggedInUserHolder.getLoggedInUser())) {
            return true;
        }else{
            log.debug("It is not possible to add suggestions to the collection: {}", collection);
            return false;
        }
    }

    private boolean isCollectionSuggestionStatusPossibleToChange(Collection collection) {
        if(collection.getOwner().equals(LoggedInUserHolder.getLoggedInUser())) {
            return true;
        }else{
            log.debug("It is not possible to change suggestion status for collection: {}", collection);
            return false;
        }
    }

    public enum CollectionsReadMode {
        PRIVATE, PUBLIC, OWNED_BY_CALLER
    }
}
