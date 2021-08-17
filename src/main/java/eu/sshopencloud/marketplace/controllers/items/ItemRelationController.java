package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.items.ItemRelatedItemDto;
import eu.sshopencloud.marketplace.dto.items.ItemRelationDto;
import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemRelationService;
import eu.sshopencloud.marketplace.services.items.exception.ItemsRelationAlreadyExistsException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items-relations")
@RequiredArgsConstructor
public class ItemRelationController {

    private final ItemRelationService itemRelationService;
    private final ItemRelatedItemService itemRelatedItemService;


    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemRelationDto>> getAllItemRelations() {
        return ResponseEntity.ok(itemRelationService.getAllItemRelations());
    }

    @PostMapping(path = "/{subjectId}/{objectId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemRelatedItemDto> createItemRelatedItem(@PathVariable("subjectId") String subjectId,
                                                                    @PathVariable("objectId") String objectId,
                                                                    @Parameter(
                                                                            description = "Created item related item object",
                                                                            required = true,
                                                                            schema = @Schema(implementation = ItemRelationId.class)) @RequestBody ItemRelationId itemRelation,
                                                                    @RequestParam(value = "draft", defaultValue = "false") boolean draft)
            throws ItemsRelationAlreadyExistsException {

        return ResponseEntity.ok(itemRelatedItemService.createItemRelatedItem(subjectId, objectId, itemRelation, draft));
    }

    @DeleteMapping("/{subjectId}/{objectId}")
    public void deleteItemRelatedItem(@PathVariable("subjectId") String subjectId, @PathVariable("objectId") String objectId,
                                      @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        itemRelatedItemService.deleteItemRelatedItem(subjectId, objectId, draft);
    }
}
