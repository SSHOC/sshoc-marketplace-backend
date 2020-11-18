package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelatedItemDto;
import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.dto.items.RelatedItemDto;
import eu.sshopencloud.marketplace.mappers.items.ItemConverter;
import eu.sshopencloud.marketplace.mappers.items.ItemRelatedItemMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class ItemRelatedItemService {

    private final ItemRelatedItemRepository itemRelatedItemRepository;
    private final ItemRelationFactory itemRelationFactory;
    private final ItemsService itemsService;
    private final DraftItemRepository draftItemRepository;
    private final VersionedItemRepository versionedItemRepository;


    public List<RelatedItemDto> getItemRelatedItems(Item item) {
        long itemId = item.getId();

        List<RelatedItemDto> relatedItems = item.isDraft() ? getDraftRelatedItems(itemId) : getRelatedItems(itemId);
        relatedItems.sort(new RelatedItemDtoComparator());

        return relatedItems;
    }

    private List<RelatedItemDto> getRelatedItems(long itemId) {
        List<RelatedItemDto> relatedItems = new ArrayList<>();

        List<ItemRelatedItem> subjectRelations = itemRelatedItemRepository.findBySubjectIdAndObjectStatus(itemId, ItemStatus.APPROVED);
        for (ItemRelatedItem subjectRelatedItem : subjectRelations) {
            relatedItems.add(ItemConverter.convertRelatedItemFromSubject(subjectRelatedItem));
        }

        List<ItemRelatedItem> objectRelations = itemRelatedItemRepository.findByObjectIdAndSubjectStatus(itemId, ItemStatus.APPROVED);
        for (ItemRelatedItem objectRelatedItem : objectRelations) {
            relatedItems.add(ItemConverter.convertRelatedItemFromObject(objectRelatedItem));
        }

        return relatedItems;
    }

    private List<RelatedItemDto> getDraftRelatedItems(long itemId) {
        DraftItem draftItem = draftItemRepository.findByItemId(itemId).get();

        return draftItem.getRelations().stream()
                .map(ItemRelatedItem::new)
                .map(ItemConverter::convertRelatedItemFromSubject)
                .collect(Collectors.toList());
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

        Item subject = itemsService.liftItemVersion(subjectId, false);
        Item object = itemsService.liftItemVersion(objectId, false);

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

        return ItemRelatedItemMapper.INSTANCE.toDto(newItemRelatedItem);
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
        for (DraftRelatedItem draftRelation : draftItem.getRelations()) {
            Item subject = draftItem.getItem();
            Item object = itemsService.liftItemVersion(draftRelation.getObject().getPersistentId(), false);

            ItemRelatedItem itemRelation = new ItemRelatedItem(subject, object, draftRelation.getRelation());
            itemRelatedItemRepository.save(itemRelation);
        }
    }

    @Deprecated
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

        Item subject = itemsService.liftItemVersion(subjectId, false);
        Item object = itemsService.liftItemVersion(objectId, false);

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

}
