package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemComment;
import eu.sshopencloud.marketplace.repositories.items.ItemCommentRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemCommentService {

    private final ItemRepository itemRepository;

    private final ItemCommentRepository itemCommentRepository;

    public ItemComment createItemComment(Long itemId, ItemComment newItemComment) {
        Item item = itemRepository.getOne(itemId);
        int size = item.getComments().size();
        item.getComments().add(newItemComment);
        item = itemRepository.save(item);
        return item.getComments().get(size);
    }

    public ItemComment updateItemComment(Long id, ItemComment newItemComment) {
        // TODO check ID
        newItemComment.setId(id);
        ItemComment itemComment = itemCommentRepository.save(newItemComment);
        return itemComment;
    }

    public void deleteItemComment(Long id) {
        itemCommentRepository.deleteById(id);
    }

}
