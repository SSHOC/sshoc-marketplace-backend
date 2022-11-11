package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.items.ItemCommentCore;
import eu.sshopencloud.marketplace.dto.items.ItemCommentDto;
import eu.sshopencloud.marketplace.services.items.ItemCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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


    @Operation(summary = "Get all comments for given item")
    @GetMapping(path = "/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemCommentDto>> getComments(@PathVariable("itemId") String itemId) {
        return ResponseEntity.ok(itemCommentService.getComments(itemId));
    }

    @Operation(summary = "Get last updated comment of item")
    @GetMapping(path = "/last-comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemCommentDto>> getLastComments(@PathVariable("itemId") String itemId) {
        return ResponseEntity.ok(itemCommentService.getLastComments(itemId));
    }

    @Operation(summary = "Create comment for given itemId")
    @PostMapping(path = "/comments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemCommentDto> createItemComment(@PathVariable("itemId") String itemId,
                                                            @Parameter(
                                                                    description = "Created comment",
                                                                    required = true,
                                                                    schema = @Schema(implementation = ItemCommentCore.class)) @RequestBody ItemCommentCore newItemComment) {

        return ResponseEntity.ok(itemCommentService.createItemComment(itemId, newItemComment));
    }

    @Operation(summary = "Update comment for given comment id and itemId")
    @PutMapping(path = "/comments/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemCommentDto> updateItemComment(@PathVariable("itemId") String itemId,
                                                            @PathVariable("id") long id,
                                                            @Parameter(
                                                                    description = "Updated comment",
                                                                    required = true,
                                                                    schema = @Schema(implementation = ItemCommentCore.class)) @RequestBody ItemCommentCore updatedItemComment) {

        return ResponseEntity.ok(itemCommentService.updateItemComment(itemId, id, updatedItemComment));
    }

    @Operation(summary = "Delete comment for given comment id and itemId")
    @DeleteMapping("/comments/{id}")
    public void deleteItemComment(@PathVariable("itemId") String itemId, @PathVariable("id") long id) {
        itemCommentService.deleteItemComment(itemId, id);
    }
}
