package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.model.items.*;
import eu.sshopencloud.marketplace.repositories.items.ItemRelatedItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemRelatedItemService {

    private final ItemRelatedItemRepository itemRelatedItemRepository;

    private final ItemRepository itemRepository;

    private final ItemRelationService itemRelationService;

    public List<ItemRelatedItemInline> getItemRelatedItems(Long itemId) {
        List<ItemRelatedItemInline> relatedItems = new ArrayList<ItemRelatedItemInline>();

        List<ItemRelatedItem> subjectRelatedItems = itemRelatedItemRepository.findItemRelatedItemBySubjectId(itemId);
        for (ItemRelatedItem subjectRelatedItem : subjectRelatedItems) {
            ItemRelatedItemInline relatedItem = new ItemRelatedItemInline();
            relatedItem.setId(subjectRelatedItem.getObject().getId());
            relatedItem.setRelation(subjectRelatedItem.getRelation());
            Item item = itemRepository.getOne(subjectRelatedItem.getObject().getId());
            relatedItem.setCategory(item.getCategory());
            relatedItem.setLabel(item.getLabel());
            relatedItem.setDescription(item.getDescription());
            relatedItems.add(relatedItem);
        }

        List<ItemRelatedItem> objectRelatedItems = itemRelatedItemRepository.findItemRelatedItemByObjectId(itemId);
        for (ItemRelatedItem objectRelatedItem : objectRelatedItems) {
            ItemRelatedItemInline relatedItem = new ItemRelatedItemInline();
            relatedItem.setId(objectRelatedItem.getSubject().getId());
            relatedItem.setRelation(objectRelatedItem.getRelation().getInverseOf());
            Item item = itemRepository.getOne(objectRelatedItem.getSubject().getId());
            relatedItem.setCategory(item.getCategory());
            relatedItem.setLabel(item.getLabel());
            relatedItem.setDescription(item.getDescription());
            relatedItems.add(relatedItem);
        }

        return relatedItems;
    }

    public ItemRelatedItem createItemRelatedItem(long subjectId, long objectId, ItemRelationId itemRelation) throws DataViolationException, ItemsRelationAlreadyExistsException {
        Optional<Item> subject = itemRepository.findById(subjectId);
        if (!subject.isPresent()) {
            throw new EntityNotFoundException("Unable to find " + Item.class.getName() + " with id " + subjectId);
        }
        Optional<Item> object = itemRepository.findById(objectId);
        if (!object.isPresent()) {
            throw new EntityNotFoundException("Unable to find " + Item.class.getName() + " with id " + objectId);
        }

        ItemRelatedItemId dirId = new ItemRelatedItemId();
        dirId.setSubject(subjectId);
        dirId.setObject(objectId);
        Optional<ItemRelatedItem> dirItemRelatedItem = itemRelatedItemRepository.findById(dirId);
        if (dirItemRelatedItem.isPresent()) {
            throw new ItemsRelationAlreadyExistsException(dirItemRelatedItem.get());
        }
        ItemRelatedItemId revId = new ItemRelatedItemId();
        revId.setSubject(objectId);
        revId.setObject(subjectId);
        Optional<ItemRelatedItem> revItemRelatedItem = itemRelatedItemRepository.findById(revId);
        if (revItemRelatedItem.isPresent()) {
            throw new ItemsRelationAlreadyExistsException(revItemRelatedItem.get());
        }

        ItemRelatedItem newItemRelatedItem = new ItemRelatedItem();
        newItemRelatedItem.setSubject(subject.get());
        newItemRelatedItem.setObject(object.get());
        newItemRelatedItem.setRelation(itemRelationService.validate("", itemRelation));
        ItemRelatedItem itemRelatedItem = itemRelatedItemRepository.save(newItemRelatedItem);
        return itemRelatedItem;
    }

    public void deleteRelationsForItem(Item item) {
        List<ItemRelatedItem> subjectRelatedItems = itemRelatedItemRepository.findItemRelatedItemBySubjectId(item.getId());
        itemRelatedItemRepository.deleteAll(subjectRelatedItems);
        List<ItemRelatedItem> objectRelatedItems = itemRelatedItemRepository.findItemRelatedItemByObjectId(item.getId());
        itemRelatedItemRepository.deleteAll(objectRelatedItems);
    }

    public void deleteItemRelatedItem(long subjectId, long objectId) {
        Optional<Item> subject = itemRepository.findById(subjectId);
        if (!subject.isPresent()) {
            throw new EntityNotFoundException("Unable to find " + Item.class.getName() + " with id " + subjectId);
        }
        Optional<Item> object = itemRepository.findById(objectId);
        if (!object.isPresent()) {
            throw new EntityNotFoundException("Unable to find " + Item.class.getName() + " with id " + objectId);
        }

        ItemRelatedItemId dirId = new ItemRelatedItemId();
        dirId.setSubject(subjectId);
        dirId.setObject(objectId);
        if (itemRelatedItemRepository.existsById(dirId)) {
            itemRelatedItemRepository.deleteById(dirId);
        }
        ItemRelatedItemId revId = new ItemRelatedItemId();
        revId.setSubject(objectId);
        revId.setObject(subjectId);
        if (itemRelatedItemRepository.existsById(revId)) {
            itemRelatedItemRepository.deleteById(revId);
        }
    }

}

