package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemCommentCore;
import eu.sshopencloud.marketplace.dto.items.ItemCommentDto;
import eu.sshopencloud.marketplace.mappers.items.ItemCommentMapper;
import eu.sshopencloud.marketplace.model.auth.Authority;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemComment;
import eu.sshopencloud.marketplace.repositories.items.ItemCommentRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.auth.UserService;
import eu.sshopencloud.marketplace.validators.items.ItemCommentFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemCommentService {

    private final ItemsService itemsService;
    private final ItemRepository itemRepository;
    private final ItemCommentRepository itemCommentRepository;
    private final ItemCommentFactory itemCommentFactory;
    private final UserService userService;


    public List<ItemCommentDto> getLastComments(String itemId) {
        Item item = itemsService.loadLatestItem(itemId);
        PageRequest pageRequest = PageRequest.of(0, 2);

        List<ItemComment> lastComments = itemCommentRepository.findLastComments(item.getId(), pageRequest);

        return ItemCommentMapper.INSTANCE.toDto(lastComments);
    }

    public List<ItemCommentDto> getComments(String itemId) {
        Item item = itemsService.loadLatestItem(itemId);
        List<ItemComment> comments = item.getComments();

        return ItemCommentMapper.INSTANCE.toDto(comments);
    }

    public ItemCommentDto createItemComment(String itemId, ItemCommentCore itemCommentCore) {
        User creator = userService.loadLoggedInUser();

        ItemComment itemComment = itemCommentFactory.create(itemCommentCore, creator);
        Item item = itemsService.loadLatestItem(itemId);

        item.getComments().add(itemComment);
        item = itemRepository.save(item);

        itemComment = item.getLatestComment();

        return ItemCommentMapper.INSTANCE.toDto(itemComment);
    }

    public ItemCommentDto updateItemComment(String itemId, Long commentId, ItemCommentCore itemCommentCore) {
        Item item = itemsService.loadLatestItem(itemId);
        ItemComment comment = loadItemComment(item, commentId);

        validateCommentPrivileges(comment);
        comment = itemCommentFactory.update(itemCommentCore, comment);

        return ItemCommentMapper.INSTANCE.toDto(comment);
    }

    public void deleteItemComment(String itemId, long commentId) {
        Item item = itemsService.loadLatestItem(itemId);
        ItemComment comment = loadItemComment(item, commentId);

        validateCommentPrivileges(comment);
        item.removeComment(comment.getId());
    }


    private ItemComment loadItemComment(Item item, long commentId) {
        return item.findComment(commentId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format(
                                        "Unable to find %s with id %d for item %s",
                                        ItemComment.class.getName(), commentId, item.getVersionedItem().getPersistentId()
                                )
                        )
                );
    }

    private void validateCommentPrivileges(ItemComment comment) {
        User loggedInUser = LoggedInUserHolder.getLoggedInUser();

        boolean isModerator = loggedInUser.isModerator();
        boolean isCreator = comment.getCreator().getUsername().equals(loggedInUser.getUsername());

        if (!isModerator && !isCreator)
            throw new AccessDeniedException("No write/delete access to the comment.");
    }
}
