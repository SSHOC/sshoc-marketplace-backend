package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.controllers.items.dto.ItemCommentCore;
import eu.sshopencloud.marketplace.model.items.ItemComment;
import eu.sshopencloud.marketplace.services.items.ItemCommentService;
import eu.sshopencloud.marketplace.services.items.OtherUserCommentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ItemCommentController {

    private final ItemCommentService itemCommentService;

    @PostMapping(path = "/item-comments/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemComment> createItemComment(@PathVariable("itemId") long itemId, @RequestBody ItemCommentCore newItemComment) {
        ItemComment itemComment = itemCommentService.createItemComment(itemId, ItemCommentConverter.convert(newItemComment));
        return ResponseEntity.ok(itemComment);
    }

    @PutMapping(path = "/item-comments/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemComment> updateItemComment(@PathVariable("id") long id, @RequestBody ItemCommentCore newItemComment)
            throws OtherUserCommentException {
        ItemComment itemComment = itemCommentService.updateItemComment(id, ItemCommentConverter.convert(newItemComment));
        return ResponseEntity.ok(itemComment);
    }

    @DeleteMapping("/item-comments/{id}")
    public void deleteItemComment(@PathVariable("id") long id)
            throws OtherUserCommentException {
        itemCommentService.deleteItemComment(id);
    }

}
