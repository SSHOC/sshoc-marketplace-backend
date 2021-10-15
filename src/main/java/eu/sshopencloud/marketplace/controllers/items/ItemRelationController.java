package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.items.ItemRelatedItemDto;
import eu.sshopencloud.marketplace.dto.items.ItemRelationCore;
import eu.sshopencloud.marketplace.dto.items.ItemRelationDto;
import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemRelationService;
import eu.sshopencloud.marketplace.services.items.exception.ItemsRelationAlreadyExistsException;
import io.swagger.v3.oas.annotations.Operation;
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

    //Eliza
    @Operation(summary = "Get list of all itemRelations")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemRelationDto>> getAllItemRelations() {
        return ResponseEntity.ok(itemRelationService.getAllItemRelations());
    }

    @Operation(summary = "Get single itemRelation by its code")
    @GetMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemRelationDto> getItemRelation(@PathVariable("code") String code) {
        return ResponseEntity.ok(itemRelationService.getItemRelation(code));
    }

    @Operation(summary = "Create itemRelation")
    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemRelationDto> createItemRelation(@Parameter(
            description = "Created itemRelation",
            required = true,
            schema = @Schema(implementation = ItemRelationCore.class)) @RequestBody ItemRelationCore itemRelationCore,
                                                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {
        return ResponseEntity.ok(itemRelationService.createItemRelation(itemRelationCore));
    }

    @Operation(summary = "Update itemRelation")
    @PutMapping(path = "/code", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemRelationDto> updateItemRelation(@PathVariable("code") String code) {
        return null;
       // ResponseEntity.ok();
    }

    @Operation(summary = "Delete itemRelation by its code")
    @DeleteMapping( path = "/code", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteItemRelation(@PathVariable("code") String code){
        return;
    }

    //PUT

    //POST

    //DELETE - with flag force as property type


    @Operation(summary = "Create item related item object for given subjectId and objectId relation ")
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

    @Operation(summary = "Delete item related item object for given subjectId and objectId relation ")
    @DeleteMapping("/{subjectId}/{objectId}")
    public void deleteItemRelatedItem(@PathVariable("subjectId") String subjectId, @PathVariable("objectId") String objectId,
                                      @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        itemRelatedItemService.deleteItemRelatedItem(subjectId, objectId, draft);
    }
}
