package eu.sshopencloud.marketplace.controllers.items;


import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.items.*;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemRelationService;
import eu.sshopencloud.marketplace.services.items.exception.ItemsRelationAlreadyExistsException;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items-relations")
@RequiredArgsConstructor
public class ItemRelationController {

    private final ItemRelationService itemRelationService;
    private final ItemRelatedItemService itemRelatedItemService;
    private final PageCoordsValidator pageCoordsValidator;


    @Operation(summary = "Retrieve all types of relations between items")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedItemRelation> getItemRelations(@RequestParam(value = "page", required = false) Integer page,
                                                                  @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {

        return ResponseEntity.ok(itemRelationService.getItemRelations(pageCoordsValidator.validate(page, perpage)));
    }

    @Operation(summary = "Get single type of relation between items by its code")
    @GetMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemRelationDto> getItemRelation(@PathVariable("code") String code) {
        return ResponseEntity.ok(itemRelationService.getItemRelation(code));
    }

    @Operation(summary = "Create new type of relation between items")
    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemRelationDto> createItemRelation(@Parameter(
            description = "New type of relation",
            required = true,
            schema = @Schema(implementation = ItemRelationCore.class)) @RequestBody ItemRelationCore itemRelationCore) {

        return ResponseEntity.ok(itemRelationService.createItemRelation(itemRelationCore));
    }

    @Operation(summary = "Update a type of relation between items")
    @PutMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemRelationDto> updateItemRelation(@PathVariable("code") String code,
                                                              @Parameter(
                                                                      description = "Updated type of relation",
                                                                      required = true,
                                                                      schema = @Schema(implementation = ItemRelationCore.class)) @RequestBody ItemRelationCore itemRelationCore) {

        return ResponseEntity.ok(itemRelationService.updateItemRelation(code, itemRelationCore));
    }

    @Operation(summary = "Delete a type of relation between items by its code")
    @DeleteMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteItemRelation(@PathVariable("code") String code,
                                   @RequestParam(value = "force", required = false, defaultValue = "false") boolean force) {

        itemRelationService.deleteItemRelation(code, force);
    }

    @Operation(summary = "Create an instance of relation between items specified by subject id and object id")
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

    @Operation(summary = "Delete an instance of relation between items specified by subject id and object id")
    @DeleteMapping("/{subjectId}/{objectId}")
    public void deleteItemRelatedItem(@PathVariable("subjectId") String subjectId, @PathVariable("objectId") String objectId,
                                      @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        itemRelatedItemService.deleteItemRelatedItem(subjectId, objectId, draft);
    }
}
