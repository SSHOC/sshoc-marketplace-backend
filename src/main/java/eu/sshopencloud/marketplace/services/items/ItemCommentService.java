package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemCommentCore;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemComment;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemCommentRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.validators.items.ItemCommentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemCommentService {

    private final ItemRepository itemRepository;

    private final ItemCommentRepository itemCommentRepository;

    private final ItemCommentValidator itemCommentValidator;

    private final UserRepository userRepository;


    public ItemComment createItemComment(Long itemId, ItemCommentCore itemCommentCore) {
        ItemComment itemComment = itemCommentValidator.validate(itemCommentCore, null);

        Optional<Item> item = itemRepository.findById(itemId);
        if (!item.isPresent()) {
            throw new EntityNotFoundException("Unable to find " + Item.class.getName() + " with id " + itemId);
        }
        ZonedDateTime now = ZonedDateTime.now();
        itemComment.setDateCreated(now);
        itemComment.setDateLastUpdated(now);

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug(authentication.toString());
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            User user = userRepository.findUserByUsername(authentication.getName());
            itemComment.setCreator(user);
        }

        int size = 0;
        List<ItemComment> comments = new ArrayList<ItemComment>();
        if (item.get().getComments() != null) {
            size = item.get().getComments().size();
            comments = item.get().getComments();
        }
        comments.add(itemComment);
        Item modifiedItem = itemRepository.save(item.get());
        return modifiedItem.getComments().get(size);
    }

    public ItemComment updateItemComment(Long itemId, Long id, ItemCommentCore itemCommentCore) {
        checkExistsItemComment(itemId, id);
        ItemComment itemComment = itemCommentValidator.validate(itemCommentCore, id);

        ZonedDateTime now = ZonedDateTime.now();
        itemComment.setDateLastUpdated(now);

        Item item = itemRepository.findItemByCommentsId(id);
        int pos = getItemCommentIndex(item, id);
        item.getComments().set(pos, itemComment);
        Item modifiedItem = itemRepository.save(item);
        return modifiedItem.getComments().get(pos);
    }


    public void deleteItemComment(Long itemId, Long id) {
        checkExistsItemComment(itemId, id);

        Item item = itemRepository.findItemByCommentsId(id);
        int pos = getItemCommentIndex(item, id);
        item.getComments().remove(pos);
        itemRepository.save(item);
    }


    private void checkExistsItemComment(Long itemId, Long id) throws EntityNotFoundException {
        if (!itemCommentRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + ItemComment.class.getName() + " with id " + id + " for item " + itemId);
        }
        Item item = itemRepository.findItemByCommentsId(id);
        if (!item.getId().equals(itemId)) {
            throw new EntityNotFoundException("Unable to find " + ItemComment.class.getName() + " with id " + id + " for item " + itemId);
        }
    }


    private int getItemCommentIndex(Item item, Long id) {
        // TODO don't allow updating/deleting without authentication (in WebSecurityConfig)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        for (int i = 0; i < item.getComments().size(); i++) {
            if (item.getComments().get(i).getId().equals(id)) {
                ItemComment comment = item.getComments().get(i);
                // TODO allow updating/deleting comments for curators
                if (!(authentication instanceof AnonymousAuthenticationToken)) {
                    User user = userRepository.findUserByUsername(authentication.getName());
                    if (!comment.getCreator().getId().equals(user.getId())) {
                        throw new AccessDeniedException("No access to the comment.");
                    }
                }
                return i;
            }
        }
        return -1;
    }

}
