package eu.sshopencloud.marketplace.services.collections;

import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.domain.media.exception.MediaNotAvailableException;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


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
    private final MediaStorageService mediaStorageService;

    public CollectionService(CollectionRepository collectionRepository,
                             VersionedItemRepository versionedItemRepository, IndexCollectionService indexCollectionService, IndexItemService indexItemService, MessagesService messagesService,
                             CollectionFactory collectionFactory, MediaStorageService mediaStorageService) {
        this.collectionRepository = collectionRepository;
        this.versionedItemRepository = versionedItemRepository;
        this.indexCollectionService = indexCollectionService;
        this.indexItemService = indexItemService;
        this.messagesService = messagesService;
        this.collectionFactory = collectionFactory;
        this.mediaStorageService = mediaStorageService;
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
        Collection collection =
                collectionRepository.findById(collectionId).orElseThrow(() -> new EntityNotFoundException(String.format("Collection with id %s not found", collectionId)));

        if (isCollectionUpdatePossible(collection)) {

            BeanPropertyBindingResult errors = new BeanPropertyBindingResult(collectionCreationDto, "Collection");

            UUID oldThumbnailId = collection.getThumbnail() != null ? collection.getThumbnail().getThumbnailId() : null;

            collection.setTitle(collectionCreationDto.getTitle());
            collection.setDescription(collectionCreationDto.getDescription());
            collection.setVisible(collectionCreationDto.isVisible());
            
            // we need to copy the flag across so we check to ensure the new and old
            // values are the same, otherwise we throw an exception if the user is not a moderator
            if (collectionCreationDto.isRecommended() != collection.isRecommended() && !LoggedInUserHolder.getLoggedInUser().isModerator()) {
                throw new AccessDeniedException("Only moderators can set the recommended status of a collection");
            }

            collection.setRecommended(collectionCreationDto.isRecommended());
            
            collection.setUpdatedAt(ZonedDateTime.now());

            // build up the list of items in the collection, making sure 
            // we don't lose any existing comments or suggestion flags
            List<Long> oldValues = collectionFactory.createContainedItems(collectionCreationDto, collection, errors);

            // this if/else allows us to remove a thumbnail from the collection
            // without it if the user set the thumbnail to null it would not be removed from the collection
            if (collectionCreationDto.getThumbnail() != null) {
                collectionFactory.createThumbnail(collectionCreationDto, collection, errors);
            } else {
                // orphanRemoval on the thumbnail association will delete the
                // row when the link is removed from the collection side — there is
                // no need to also null the owning side of the association
                collection.setThumbnail(null);
            }

            UUID newThumbnailId = collection.getThumbnail() != null ? collection.getThumbnail().getThumbnailId() : null;
            
            collectionFactory.createContributors(collectionCreationDto, collection, errors);

            if (errors.hasErrors()) {
                throw new ValidationException(errors);
            }

            try {
                // we've put both the link and unlink of the thumbnail inside this try/catch block
                // so that if the linking of the new one fails we can fall through to the catch
                // block and abort the update before we unlink the old one, otherwise we could end
                // up with a collection with no thumbnail if the new one fails to link.

                if (newThumbnailId != null && !newThumbnailId.equals(oldThumbnailId)) {
                    // if the new version has a thumbnail that is different to the old version
                    // then we need to increment the usage counter so that it doesn't get garbage
                    // collected in 2 hours time
                    mediaStorageService.linkToMedia(newThumbnailId);
                }

                if (oldThumbnailId != null && !oldThumbnailId.equals(newThumbnailId)) {
                    // if the collection had a thumbnail before and it has been changed or
                    // removed entirely then decrement the usage counter so it can be
                    // garbage collected if it is no longer used anywhere else
                    mediaStorageService.removeMediaLink(oldThumbnailId);
                }
            } catch (MediaNotAvailableException e) {
                // this should never happen because the call to collectionFactory.createThumbnail()
                // checks that the media is available before it will create the new thumbnail.
                // The only way we could get here is if the media was deleted in that short
                // period between the two calls, which is very unlikely, but we catch it and
                // raise an error if that is the case.
                errors.rejectValue(
                    "thumbnail.info.mediaId",
                    "field.invalid",
                    "The media with the given mediaId does not exist");

                throw new ValidationException(errors);
            }

            indexCollectionService.indexCollection(collection);
            indexItemService.removeOldValues(oldValues, collection.getId());
            indexItemService.reIndexItemsInCollection(collection);

            return CollectionListMapper.INSTANCE.toDto(collection);
        } else {
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

        if (collection.get().getThumbnail() != null) {
            // if the collection has a thumbnail, we need to remove the link to it so
            // that it can be garbage collected if it is no longer used anywhere else
            mediaStorageService.removeMediaLink(collection.get().getThumbnail().getThumbnailId());
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
