package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;
import eu.sshopencloud.marketplace.model.items.ItemRelatedItemInline;
import eu.sshopencloud.marketplace.repositories.items.ItemRelatedItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemRelatedItemService {

    private final ItemRelatedItemRepository itemRelatedItemRepository;


    private final ItemRepository itemRepository;

    public List<ItemRelatedItemInline> getItemRelatedItems(Long itemId) {
        List<ItemRelatedItemInline> relatedItems = new ArrayList<ItemRelatedItemInline>();

        List<ItemRelatedItem> subjectRelatedItems = itemRelatedItemRepository.findItemRelatedItemBySubjectId(itemId);
        for (ItemRelatedItem subjectRelatedItem : subjectRelatedItems) {
            ItemRelatedItemInline relatedItem = new ItemRelatedItemInline();
            relatedItem.setId(subjectRelatedItem.getObjectId());
            relatedItem.setRelation(subjectRelatedItem.getRelation());
            Item item = itemRepository.getOne(subjectRelatedItem.getObjectId());
            relatedItem.setCategory(item.getCategory());
            relatedItem.setLabel(item.getLabel());
            relatedItem.setDescription(item.getDescription());
            relatedItems.add(relatedItem);
        }

        List<ItemRelatedItem> objectRelatedItems = itemRelatedItemRepository.findItemRelatedItemByObjectId(itemId);
        for (ItemRelatedItem objectRelatedItem : objectRelatedItems) {
            ItemRelatedItemInline relatedItem = new ItemRelatedItemInline();
            relatedItem.setId(objectRelatedItem.getSubjectId());
            relatedItem.setRelation(objectRelatedItem.getRelation());
            Item item = itemRepository.getOne(objectRelatedItem.getSubjectId());
            relatedItem.setCategory(item.getCategory());
            relatedItem.setLabel(item.getLabel());
            relatedItem.setDescription(item.getDescription());
            relatedItems.add(relatedItem);
        }

        return relatedItems;
    }

}

