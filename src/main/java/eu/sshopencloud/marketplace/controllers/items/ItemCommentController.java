package eu.sshopencloud.marketplace.controllers.items;

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

    @PostMapping(path = "/item-comments/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemComment> createItemComment(@PathVariable("itemId") long itemId, @RequestBody ItemComment newItemComment) {
        ItemComment itemComment = itemCommentService.createItemComment(itemId, newItemComment);
        return ResponseEntity.ok(itemComment);
    }

    @PutMapping(path = "/item-comments/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemComment> updateItemComment(@PathVariable("id") long id, @RequestBody ItemComment newItemComment) {
        ItemComment itemComment = itemCommentService.updateItemComment(id, newItemComment);
        return ResponseEntity.ok(itemComment);
    }

    @DeleteMapping("/item-comments/{id}")
    public void deleteItemComment(@PathVariable("id") long id) {
        itemCommentService.deleteItemComment(id);
    }

}
