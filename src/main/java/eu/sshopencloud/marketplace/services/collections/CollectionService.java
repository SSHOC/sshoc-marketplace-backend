package eu.sshopencloud.marketplace.services.collections;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.collections.*;
import eu.sshopencloud.marketplace.dto.inbox.MessageDto;
import eu.sshopencloud.marketplace.mappers.collections.CollectionListMapper;
import eu.sshopencloud.marketplace.mappers.collections.CollectionMapper;
import eu.sshopencloud.marketplace.model.collections.Collection;
import eu.sshopencloud.marketplace.model.collections.CollectionItem;
import eu.sshopencloud.marketplace.model.items.VersionedItem;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.repositories.collections.CollectionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.inbox.MessagesService;
import eu.sshopencloud.marketplace.services.search.IndexCollectionService;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.vocabularies.PropertyFactory;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@Transactional
@Slf4j
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final VersionedItemRepository versionedItemRepository;
    private final IndexCollectionService indexCollectionService;
    private final MessagesService messagesService;
    private final PropertyFactory propertyFactory;


    public CollectionService(CollectionRepository collectionRepository,
                             VersionedItemRepository versionedItemRepository, IndexCollectionService indexCollectionService, MessagesService messagesService, PropertyFactory propertyFactory) {
        this.collectionRepository = collectionRepository;
        this.versionedItemRepository = versionedItemRepository;
        this.indexCollectionService = indexCollectionService;
        this.messagesService = messagesService;
        this.propertyFactory = propertyFactory;
    }


    public CollectionDto createCollection(CollectionCreationDto collectionCore) {
        Collection collection = new Collection();
        collection.setTitle(collectionCore.getTitle());
        collection.setDescription(collectionCore.getDescription());
        collection.setVisible(collectionCore.isVisible());
        collection.setOwner(LoggedInUserHolder.getLoggedInUser());
        collection.setCreatedAt(ZonedDateTime.now(ZoneId.systemDefault()));
        collection.setUpdatedAt(ZonedDateTime.now(ZoneId.systemDefault()));

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(collectionCore, "CollectionDto");

        List<Property> properties = propertyFactory.create(collectionCore.getProperties(), null, errors, "properties");

        collection.setProperties(properties);

        collectionCore.getContainedItems().forEach(item -> {
            VersionedItem versionedItem =
                    versionedItemRepository.findById(item).orElseThrow(() -> new CollectionException("Item not found"));
            CollectionItem collectionItem = new CollectionItem();
            collectionItem.setCollection(collection);
            collectionItem.setItem(versionedItem.getCurrentVersion());
            collection.getCollectionItems().add(collectionItem);
        });

        if (errors.hasErrors())
            throw new ValidationException(errors);

        collectionRepository.save(collection);
        indexCollectionService.indexCollection(collection);

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

        if (collection.getOwner().equals(LoggedInUserHolder.getLoggedInUser())) {
            collection.setTitle(collectionCreationDto.getTitle());
            collection.setDescription(collectionCreationDto.getDescription());
            collection.setVisible(collectionCreationDto.isVisible());
            collection.setOwner(LoggedInUserHolder.getLoggedInUser());
            collection.setUpdatedAt(ZonedDateTime.now());
            collection.getCollectionItems().clear();

            collectionCreationDto.getContainedItems().forEach(item -> {
                VersionedItem versionedItem =
                        versionedItemRepository.findById(item).orElseThrow(() -> new CollectionException("Item not found"));
                CollectionItem collectionItem = new CollectionItem();
                collectionItem.setCollection(collection);
                collectionItem.setItem(versionedItem.getCurrentVersion());
                collection.getCollectionItems().add(collectionItem);
            });

            indexCollectionService.indexCollection(collection);

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
        if (collection.isPresent() && collection.get().getOwner().equals(LoggedInUserHolder.getLoggedInUser())) {
            collectionRepository.deleteById(collectionId);
            indexCollectionService.removeFromIndex(collection.get());
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

    public enum CollectionsReadMode {
        PRIVATE, PUBLIC, OWNED_BY_CALLER
    }
}
