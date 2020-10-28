package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelatedItemDto;
import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.dto.items.RelatedItemDto;
import eu.sshopencloud.marketplace.mappers.items.ItemConverter;
import eu.sshopencloud.marketplace.mappers.items.ItemRelatedItemMapper;
import eu.sshopencloud.marketplace.model.items.*;
import eu.sshopencloud.marketplace.repositories.items.ItemRelatedItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.services.items.exception.ItemsRelationAlreadyExistsException;
import eu.sshopencloud.marketplace.validators.items.ItemRelationValidator;
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

    private final ItemRelationValidator itemRelationValidator;


    public List<RelatedItemDto> getItemRelatedItems(Long itemId) {
        List<RelatedItemDto> relatedItems = new ArrayList<>();

        List<ItemRelatedItem> subjectRelatedItems = itemRelatedItemRepository.findBySubjectId(itemId);
        for (ItemRelatedItem subjectRelatedItem : subjectRelatedItems) {
            relatedItems.add(ItemConverter.convertRelatedItemFromSubject(subjectRelatedItem));
        }

        List<ItemRelatedItem> objectRelatedItems = itemRelatedItemRepository.findByObjectId(itemId);
        for (ItemRelatedItem objectRelatedItem : objectRelatedItems) {
            relatedItems.add(ItemConverter.convertRelatedItemFromObject(objectRelatedItem));
        }

        return relatedItems;
    }

    public ItemRelatedItemDto createItemRelatedItem(long subjectId, long objectId, ItemRelationId itemRelationId) throws ItemsRelationAlreadyExistsException {
        Item subject = itemRepository.findById(subjectId).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Item.class.getName() + " with id " + subjectId));
        Item object = itemRepository.findById(objectId).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Item.class.getName() + " with id " + objectId));

        ItemRelation itemRelation = itemRelationValidator.validate(itemRelationId);

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
        newItemRelatedItem.setSubject(subject);
        newItemRelatedItem.setObject(object);
        newItemRelatedItem.setRelation(itemRelation);
        ItemRelatedItem itemRelatedItem = itemRelatedItemRepository.save(newItemRelatedItem);
        return ItemRelatedItemMapper.INSTANCE.toDto(itemRelatedItem);
    }

    @Deprecated
    public void deleteRelationsForItem(Item item) {
        List<ItemRelatedItem> subjectRelatedItems = itemRelatedItemRepository.findBySubjectId(item.getId());
        itemRelatedItemRepository.deleteAll(subjectRelatedItems);
        List<ItemRelatedItem> objectRelatedItems = itemRelatedItemRepository.findByObjectId(item.getId());
        itemRelatedItemRepository.deleteAll(objectRelatedItems);
    }

    public void deleteItemRelatedItem(long subjectId, long objectId) {
        if (!itemRepository.existsById(subjectId)) {
            throw new EntityNotFoundException("Unable to find " + Item.class.getName() + " with id " + subjectId);
        }
        if (!itemRepository.existsById(objectId)) {
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

