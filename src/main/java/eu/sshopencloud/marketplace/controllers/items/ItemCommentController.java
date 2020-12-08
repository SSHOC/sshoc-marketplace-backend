package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.dto.items.ItemCommentCore;
import eu.sshopencloud.marketplace.dto.items.ItemCommentDto;
import eu.sshopencloud.marketplace.services.items.ItemCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items/{itemId}")
@RequiredArgsConstructor
public class ItemCommentController {

    private final ItemCommentService itemCommentService;


    @GetMapping(path = "/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemCommentDto>> getComments(@PathVariable("itemId") String itemId) {
        return ResponseEntity.ok(itemCommentService.getComments(itemId));
    }

    @GetMapping(path = "/last-comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemCommentDto>> getLastComments(@PathVariable("itemId") String itemId) {
        return ResponseEntity.ok(itemCommentService.getLastComments(itemId));
    }

    @PostMapping(path = "/comments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemCommentDto> createItemComment(@PathVariable("itemId") String itemId,
                                                            @RequestBody ItemCommentCore newItemComment) {

        return ResponseEntity.ok(itemCommentService.createItemComment(itemId, newItemComment));
    }

    @PutMapping(path = "/comments/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemCommentDto> updateItemComment(@PathVariable("itemId") String itemId,
                                                            @PathVariable("id") long id,
                                                            @RequestBody ItemCommentCore updatedItemComment) {

        return ResponseEntity.ok(itemCommentService.updateItemComment(itemId, id, updatedItemComment));
    }

    @DeleteMapping("/comments/{id}")
    public void deleteItemComment(@PathVariable("itemId") String itemId, @PathVariable("id") long id) {
        itemCommentService.deleteItemComment(itemId, id);
    }
}
