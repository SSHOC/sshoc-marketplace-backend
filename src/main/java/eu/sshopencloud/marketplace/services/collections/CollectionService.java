package eu.sshopencloud.marketplace.services.collections;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.collections.*;
import eu.sshopencloud.marketplace.dto.inbox.MessageDto;
import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.dto.items.PaginatedItemsBasic;
import eu.sshopencloud.marketplace.mappers.collections.CollectionMapper;
import eu.sshopencloud.marketplace.model.collections.Collection;
import eu.sshopencloud.marketplace.model.collections.CollectionItem;
import eu.sshopencloud.marketplace.model.items.VersionedItem;
import eu.sshopencloud.marketplace.repositories.collections.CollectionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.inbox.MessagesService;
import eu.sshopencloud.marketplace.services.search.IndexCollectionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final VersionedItemRepository versionedItemRepository;
    private final IndexCollectionService indexCollectionService;
    private final MessagesService messagesService;


    public CollectionService(CollectionRepository collectionRepository,
                             VersionedItemRepository versionedItemRepository, IndexCollectionService indexCollectionService, MessagesService messagesService) {
        this.collectionRepository = collectionRepository;
        this.versionedItemRepository = versionedItemRepository;
        this.indexCollectionService = indexCollectionService;
        this.messagesService = messagesService;
    }


    public CollectionDto createCollection(CollectionCreationDto collectionCore) {
        Collection collection = new Collection();
        collection.setTitle(collectionCore.getTitle());
        collection.setDescription(collectionCore.getDescription());
        collection.setVisible(collectionCore.isVisible());
        collection.setOwner(LoggedInUserHolder.getLoggedInUser());

        collectionCore.getContainedItems().forEach(item -> {
            VersionedItem versionedItem =
                    versionedItemRepository.findById(item).orElseThrow(() -> new CollectionException("Item not found"));
            CollectionItem collectionItem = new CollectionItem();
            collectionItem.setCollection(collection);
            collectionItem.setItem(versionedItem.getCurrentVersion());
            collection.getCollectionItems().add(collectionItem);
        });
        collectionRepository.save(collection);
        indexCollectionService.indexCollection(collection);

        return CollectionMapper.INSTANCE.toDto(collection);
    }

    public PaginatedCollections getCollections(PageCoords pageCoords, boolean privateOnly) {
        if (privateOnly) {
            return getUserPrivateCollections(pageCoords);
        } else {
            return getPublicCollections(pageCoords);
        }
    }

    private PaginatedCollections getPublicCollections(PageCoords pageCoords) {
        PageRequest pageRequest = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage());
        Page<Collection> collections = collectionRepository.findAllByVisibleTrue(pageRequest);
        return PaginatedCollections.builder().collections(CollectionMapper.INSTANCE.toDto(collections.getContent())).count(collections.getContent().size()).hits(collections.getTotalElements()).page(pageRequest.getPageNumber()).perpage(pageRequest.getPageSize()).pages(collections.getTotalPages()).build();
    }

    private PaginatedCollections getUserPrivateCollections(PageCoords pageCoords) {
        if (LoggedInUserHolder.getLoggedInUser() == null) {
            return PaginatedCollections.builder().collections(Collections.emptyList()).count(0).hits(0).page(0).perpage(0).pages(0).build();
        }
        PageRequest pageRequest = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage());
        Page<Collection> collections =
                collectionRepository.findAllByOwnerAndVisible(LoggedInUserHolder.getLoggedInUser(), true, pageRequest);
        return PaginatedCollections.builder().collections(CollectionMapper.INSTANCE.toDto(collections.getContent())).count(collections.getContent().size()).hits(collections.getTotalElements()).page(pageRequest.getPageNumber()).perpage(pageRequest.getPageSize()).pages(collections.getTotalPages()).build();
    }

    public CollectionDto getCollection(long id) {
        Collection collection =
                collectionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(String.format(
                        "Collection with id %s not found", id)));

        return CollectionMapper.INSTANCE.toDto(collection);
    }

    public CollectionDto updateCollection(long collectionId, CollectionCreationDto collectionCreationDto) {
        Collection collection =
                collectionRepository.findById(collectionId).orElseThrow(() -> new EntityNotFoundException(String.format("Collection with id %s not found", collectionId)));

        if (collection.getOwner().equals(LoggedInUserHolder.getLoggedInUser())) {
            collection.setTitle(collectionCreationDto.getTitle());
            collection.setDescription(collectionCreationDto.getDescription());
            collection.setVisible(collectionCreationDto.isVisible());
            collection.setUpdatedAt(ZonedDateTime.now());

            return CollectionMapper.INSTANCE.toDto(collection);
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
            messageDto.setContent("New collection suggestion received");

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
        if (collection.isPresent() && collection.get().getOwner().equals(LoggedInUserHolder.getLoggedInUser())) {
            collectionRepository.deleteById(collectionId);
        }
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
}
