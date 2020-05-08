package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.dto.items.ItemCommentCore;
import eu.sshopencloud.marketplace.dto.items.ItemCommentDto;
import eu.sshopencloud.marketplace.services.items.ItemCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items/{itemId}/comments")
@RequiredArgsConstructor
public class ItemCommentController {

    private final ItemCommentService itemCommentService;

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemCommentDto> createItemComment(@PathVariable("itemId") long itemId, @RequestBody ItemCommentCore newItemComment) {
        return ResponseEntity.ok(itemCommentService.createItemComment(itemId, newItemComment));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemCommentDto> updateItemComment(@PathVariable("itemId") long itemId, @PathVariable("id") long id,
                                                         @RequestBody ItemCommentCore updatedItemComment) {
        return ResponseEntity.ok(itemCommentService.updateItemComment(itemId, id, updatedItemComment));
    }

    @DeleteMapping("/{id}")
    public void deleteItemComment(@PathVariable("itemId") long itemId, @PathVariable("id") long id) {
        itemCommentService.deleteItemComment(itemId, id);
    }

}
