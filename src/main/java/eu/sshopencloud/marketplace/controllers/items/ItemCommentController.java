package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.dto.items.ItemCommentCore;
import eu.sshopencloud.marketplace.model.items.ItemComment;
import eu.sshopencloud.marketplace.services.items.ItemCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ItemCommentController {

    private final ItemCommentService itemCommentService;

    @PostMapping(path = "/item/{itemId}/comments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemComment> createItemComment(@PathVariable("itemId") long itemId, @RequestBody ItemCommentCore newItemComment) {
        return ResponseEntity.ok(itemCommentService.createItemComment(itemId, newItemComment));
    }

    @PutMapping(path = "/item/{itemId}/comments/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemComment> updateItemComment(@PathVariable("itemId") long itemId, @PathVariable("id") long id,
                                                         @RequestBody ItemCommentCore updatedItemComment) {
        ItemComment itemComment = itemCommentService.updateItemComment(itemId, id, updatedItemComment);
        return ResponseEntity.ok(itemComment);
    }

    @DeleteMapping("/item/{itemId}/comments/{id}")
    public void deleteItemComment(@PathVariable("itemId") long itemId, @PathVariable("id") long id) {
        itemCommentService.deleteItemComment(itemId, id);
    }

}
