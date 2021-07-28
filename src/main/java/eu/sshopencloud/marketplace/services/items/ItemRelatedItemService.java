package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelatedItemDto;
import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.dto.items.RelatedItemCore;
import eu.sshopencloud.marketplace.dto.items.RelatedItemDto;
import eu.sshopencloud.marketplace.mappers.items.ItemRelatedItemMapper;
import eu.sshopencloud.marketplace.mappers.items.RelatedItemsConverter;
import eu.sshopencloud.marketplace.model.items.*;
import eu.sshopencloud.marketplace.repositories.items.DraftItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRelatedItemRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.services.items.exception.ItemsRelationAlreadyExistsException;
import eu.sshopencloud.marketplace.validators.items.ItemRelationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Service
@Transactional
@RequiredArgsConstructor
public class ItemRelatedItemService {

    private final ItemRelatedItemRepository itemRelatedItemRepository;
    private final ItemRelationFactory itemRelationFactory;
    private final ItemsService itemsService;
    private final ItemVisibilityService itemVisibilityService;
    private final DraftItemRepository draftItemRepository;
    private final VersionedItemRepository versionedItemRepository;
    private final RelatedItemsConverter relatedItemsConverter;


    public List<RelatedItemDto> getItemRelatedItems(Item item) {
        long itemId = item.getId();

        List<RelatedItemDto> relatedItems = item.isDraft() ? getDraftRelatedItems(itemId) : getRelatedItems(itemId);
        relatedItems.sort(new RelatedItemDtoComparator());

        return relatedItems;
    }

    private List<RelatedItemDto> getRelatedItems(long itemId) {
        List<RelatedItemDto> relatedItems = new ArrayList<>();

        List<RelatedItemDto> subjectRelations = itemRelatedItemRepository.findAllBySubjectId(itemId).stream()
                .filter(relatedItem -> itemVisibilityService.shouldCurrentUserSeeItem(relatedItem.getObject()))
                .map(relatedItemsConverter::convertRelatedItemFromSubject)
                .collect(Collectors.toList());

        List<RelatedItemDto> objectRelations = itemRelatedItemRepository.findAllByObjectId(itemId).stream()
                .filter(relatedItem -> itemVisibilityService.shouldCurrentUserSeeItem(relatedItem.getSubject()))
                .map(relatedItemsConverter::convertRelatedItemFromObject)
                .collect(Collectors.toList());

        relatedItems.addAll(subjectRelations);
        relatedItems.addAll(objectRelations);

        return relatedItems;
    }

    private List<RelatedItemDto> getDraftRelatedItems(long itemId) {
        DraftItem draftItem = draftItemRepository.findByItemId(itemId).get();

        return draftItem.getRelations().stream()
                .map(ItemRelatedItem::new)
                .map(relatedItemsConverter::convertRelatedItemFromSubject)
                .collect(Collectors.toList());
    }

    public void updateRelatedItems(List<RelatedItemCore> relatedItems, Item newVersion, Item prevItem, boolean draft) {
        if (draft) {
            updateDraftRelatedItems(relatedItems, newVersion);
        } else {
            updateRelatedItems(relatedItems, newVersion, prevItem);
        }
    }

    private void updateRelatedItems(List<RelatedItemCore> relatedItems, Item newVersion, Item prevItem) {
        validateNewRelatedItems(relatedItems);

        Map<String, Item> relatedVersions = new HashMap<>();
        Map<Long, ItemRelation> savedRelations = new HashMap<>();

        if (relatedItems == null || isAllNulls(relatedItems))
            relatedItems = new ArrayList<>();

        List<RelatedItemDto> prevRelations = (prevItem != null) ? getRelatedItems(prevItem.getId()) : new ArrayList<>();

        Map<String, Set<String>> existentRelations = new HashMap<>();
        prevRelations.forEach(relatedItem -> {
            String persistentId = relatedItem.getPersistentId();

            if (!existentRelations.containsKey(persistentId))
                existentRelations.put(persistentId, new HashSet<>());

            existentRelations.get(persistentId)
                    .add(relatedItem.getRelation().getCode());
        });

        Map<String, RelatedItemCore> toKeep = new HashMap<>();

        // Adding new relations to the item
        relatedItems.forEach(relatedItem -> {
            ItemRelation relationType = itemRelationFactory.create(relatedItem.getRelation());

            String persistentId = relatedItem.getPersistentId();
            String relationCode = relatedItem.getRelation().getCode();

            if (existentRelations.containsKey(persistentId) && existentRelations.get(persistentId).contains(relationCode)) {
                toKeep.put(persistentId, relatedItem);
                return;
            }

            if (!relatedVersions.containsKey(persistentId)) {
                Item objectVersion = itemsService.liftItemVersion(persistentId, false, false);
                relatedVersions.put(persistentId, objectVersion);
            }

            Item objectVersion = relatedVersions.get(persistentId);
            ItemRelatedItem itemsRelation = saveItemsRelationChecked(newVersion, objectVersion, relationType);

            savedRelations.put(itemsRelation.getObject().getId(), relationType);
        });

        if (prevItem == null)
            return;

        // Removing old relations that are not present in the form
        prevRelations.forEach(prevRelation -> {
            Long relObjectId = prevRelation.getId();
            String prevRelationCode = prevRelation.getRelation().getCode();

            if (savedRelations.containsKey(relObjectId) && savedRelations.get(relObjectId).getCode().equals(prevRelationCode)
                    || toKeep.containsKey(prevRelation.getPersistentId())) {
                return;
            }

            Item prevRelatedItem = itemsService.loadCurrentItem(prevRelation.getPersistentId());

            // previous item had a relation to the current target item and hasn't got anymore
            if (prevRelatedItem.getId().equals(prevRelation.getId())) {
                String objectId = prevRelation.getPersistentId();

                if (!relatedVersions.containsKey(objectId)) {
                    Item newObjectVersion = itemsService.liftItemVersion(objectId, false, false);
                    relatedVersions.put(objectId, newObjectVersion);

                    removeItemRelation(prevItem, newObjectVersion);
                }
            }
        });

        // Keep relations from previous version
        toKeep.values().forEach(relatedItem -> {
            String persistentId = relatedItem.getPersistentId();
            Item objectVersion = relatedVersions.containsKey(persistentId) ?
                    relatedVersions.get(persistentId) : itemsService.loadCurrentItem(persistentId);

            ItemRelation relationType = itemRelationFactory.create(relatedItem.getRelation());
            saveItemsRelationChecked(newVersion, objectVersion, relationType);
        });
    }

    private void validateNewRelatedItems(List<RelatedItemCore> relatedItems) {

        if (relatedItems == null || isAllNulls(relatedItems))
            return;

        Set<String> relatedPersistentIds = new HashSet<>();

        relatedItems.forEach(rel -> {
            if (relatedPersistentIds.contains(rel.getPersistentId()))
                throw new IllegalArgumentException(String.format("Duplicate relation to object with id %s", rel.getPersistentId()));

            relatedPersistentIds.add(rel.getPersistentId());
        });
    }

    private ItemRelatedItem saveItemsRelationChecked(Item subject, Item object, ItemRelation itemRelation) {
        try {
            return saveItemsRelation(subject, object, itemRelation);
        } catch (ItemsRelationAlreadyExistsException e) {
            throw new IllegalArgumentException(
                    String.format(
                            "A relation between objects with ids %s and %s already exists",
                            subject.getPersistentId(), object.getPersistentId()
                    )
            );
        }
    }

    private void updateDraftRelatedItems(List<RelatedItemCore> relatedItems, Item subject) {
        DraftItem draftSubject = draftItemRepository.findByItemId(subject.getId()).get();
        draftSubject.clearRelations();

        if (relatedItems == null)
            return;

        relatedItems.forEach(relatedItem -> {
            ItemRelation relationType = itemRelationFactory.create(relatedItem.getRelation());
            try {
                createDraftItemRelation(subject.getPersistentId(), relatedItem.getPersistentId(), relationType);
            } catch (ItemsRelationAlreadyExistsException e) {
                throw new IllegalArgumentException(
                        String.format("Repeated relation to object with id %s", relatedItem.getPersistentId())
                );
            }
        });
    }

    public ItemRelatedItemDto createItemRelatedItem(String subjectId, String objectId,
                                                    ItemRelationId itemRelationId, boolean draft)
            throws ItemsRelationAlreadyExistsException {

        ItemRelation itemRelation = itemRelationFactory.create(itemRelationId);

        if (draft) {
            DraftRelatedItem relatedDraft = createDraftItemRelation(subjectId, objectId, itemRelation);
            ItemRelatedItem relatedItem = new ItemRelatedItem(relatedDraft);

            return ItemRelatedItemMapper.INSTANCE.toDto(relatedItem);
        }

        Item subject = itemsService.liftItemVersion(subjectId, false, false);
        Item object = itemsService.liftItemVersion(objectId, false, false);

        ItemRelatedItem newItemRelatedItem = saveItemsRelation(subject, object, itemRelation);
        return ItemRelatedItemMapper.INSTANCE.toDto(newItemRelatedItem);
    }

    private ItemRelatedItem saveItemsRelation(Item subject, Item object, ItemRelation itemRelation)
            throws ItemsRelationAlreadyExistsException {

        ItemRelatedItemId dirId = new ItemRelatedItemId(subject.getId(), object.getId());
        Optional<ItemRelatedItem> dirItemRelatedItem = itemRelatedItemRepository.findById(dirId);
        if (dirItemRelatedItem.isPresent()) {
            throw new ItemsRelationAlreadyExistsException(dirItemRelatedItem.get());
        }
        ItemRelatedItemId revId = new ItemRelatedItemId(object.getId(), subject.getId());
        Optional<ItemRelatedItem> revItemRelatedItem = itemRelatedItemRepository.findById(revId);
        if (revItemRelatedItem.isPresent()) {
            throw new ItemsRelationAlreadyExistsException(revItemRelatedItem.get());
        }

        ItemRelatedItem newItemRelatedItem = new ItemRelatedItem(subject, object, itemRelation);
        newItemRelatedItem = itemRelatedItemRepository.save(newItemRelatedItem);

        return newItemRelatedItem;
    }

    private DraftRelatedItem createDraftItemRelation(String subjectId, String objectId, ItemRelation relation)
            throws ItemsRelationAlreadyExistsException {

        Item subject = itemsService.loadItemDraftForCurrentUser(subjectId);
        VersionedItem object = versionedItemRepository.findById(objectId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Target object with id %s not found", objectId)));

        if (!object.isActive()) {
            throw new IllegalArgumentException(
                    String.format("Cannot add relation to a non-active (deleted/merged) item with id %s", objectId)
            );
        }

        DraftItem draftSubject = draftItemRepository.findByItemId(subject.getId()).get();
        return draftSubject.addRelation(object, relation);
    }

    void copyItemRelations(Item target, Item source) {
        List<ItemRelatedItem> subjectRelations = itemRelatedItemRepository.findAllBySubjectId(source.getId());
        List<ItemRelatedItem> objectRelations = itemRelatedItemRepository.findAllByObjectId(source.getId());

        for (ItemRelatedItem subjectRelation : subjectRelations) {
            ItemRelatedItem relationCopy = new ItemRelatedItem(target, subjectRelation.getObject(), subjectRelation.getRelation());
            itemRelatedItemRepository.save(relationCopy);
        }

        for (ItemRelatedItem objectRelation : objectRelations) {
            ItemRelatedItem relationCopy = new ItemRelatedItem(objectRelation.getSubject(), target, objectRelation.getRelation());
            itemRelatedItemRepository.save(relationCopy);
        }
    }

    void commitDraftRelations(DraftItem draftItem) {
        List<RelatedItemCore> relatedItems = draftItem.getRelations().stream()
                .map(rel ->
                        RelatedItemCore.builder()
                                .persistentId(rel.getObject().getPersistentId())
                                .relation(rel.getRelation().getId())
                                .build()
                )
                .collect(Collectors.toList());

        Item item = draftItem.getItem();
        Item prevPublicItem = item.getVersionedItem().getCurrentVersion();

        updateRelatedItems(relatedItems, item, prevPublicItem);
    }

    @Deprecated(since = "This method does not upgrade versions of related items, which should be done since we modify relations between items")
    public void deleteRelationsForItem(Item item) {
        List<ItemRelatedItem> subjectRelations = itemRelatedItemRepository.findAllBySubjectId(item.getId());
        itemRelatedItemRepository.deleteAll(subjectRelations);

        List<ItemRelatedItem> objectRelations = itemRelatedItemRepository.findAllByObjectId(item.getId());
        itemRelatedItemRepository.deleteAll(objectRelations);
    }

    public void deleteItemRelatedItem(String subjectId, String objectId, boolean draft) {
        if (draft) {
            removeDraftItemRelation(subjectId, objectId);
            return;
        }

        Item subject = itemsService.liftItemVersion(subjectId, false, false);
        Item object = itemsService.liftItemVersion(objectId, false, false);

        removeItemRelation(subject, object);
        removeItemRelation(object, subject);
    }

    private void removeDraftItemRelation(String subjectId, String objectId) {
        Item subject = itemsService.loadItemDraftForCurrentUser(subjectId);
        DraftItem draftItem = draftItemRepository.findByItemId(subject.getId()).get();

        draftItem.removeRelation(objectId);
    }

    private void removeItemRelation(Item subject, Item object) {
        ItemRelatedItemId relationId = new ItemRelatedItemId(subject.getId(), object.getId());
        // we have to check if relation exists, because we try to delete this relation in two directions, but the relation is saved in only one of them
        if (itemRelatedItemRepository.existsById(relationId)) {
            itemRelatedItemRepository.deleteById(relationId);
        }
    }

    // TODO Eliza move this to the utility class eu.sshopencloud.marketplace.validators.CollectionUtils and reuse where needed:
    // - src/main/java/eu/sshopencloud/marketplace/validators/actors/ActorFactory.java 70
    // - src/main/java/eu/sshopencloud/marketplace/validators/items/ItemContributorFactory.java 29
    // - src/main/java/eu/sshopencloud/marketplace/validators/vocabularies/PropertyFactory.java 32
    public static boolean isAllNulls(Iterable<?> array) {
        return StreamSupport.stream(array.spliterator(), true).allMatch(o -> Objects.isNull(o));
    }

}
