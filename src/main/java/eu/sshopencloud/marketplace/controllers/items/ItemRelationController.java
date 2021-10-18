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

import java.util.List;

@RestController
@RequestMapping("/api/items-relations")
@RequiredArgsConstructor
public class ItemRelationController {

    private final ItemRelationService itemRelationService;
    private final ItemRelatedItemService itemRelatedItemService;
    private final PageCoordsValidator pageCoordsValidator;


    @Operation(summary = "Retrieve all itemRelations in pages")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedItemRelation> getItemRelations(@RequestParam(value = "page", required = false) Integer page,
                                                                  @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {

        return ResponseEntity.ok(itemRelationService.getItemRelations(pageCoordsValidator.validate(page, perpage)));
    }

/*
    @Operation(summary = "Get list of all itemRelations")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemRelationDto>> getAllItemRelations() {
        return ResponseEntity.ok(itemRelationService.getAllItemRelations());
    }
*/

    //Good - czy nie potrzeba order i inverse of w ItemRelationDto ??
    @Operation(summary = "Get single itemRelation by its code")
    @GetMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemRelationDto> getItemRelation(@PathVariable("code") String code) {
        return ResponseEntity.ok(itemRelationService.getItemRelation(code));
    }

    //Eliza
    @Operation(summary = "Create itemRelation")
    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemRelationDto> createItemRelation(@Parameter(
            description = "Created itemRelation",
            required = true,
            schema = @Schema(implementation = ItemRelationCore.class)) @RequestBody ItemRelationCore itemRelationCore) {

        return ResponseEntity.ok(itemRelationService.createItemRelation(itemRelationCore));
    }

    @Operation(summary = "Update itemRelation")
    @PutMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemRelationDto> updateItemRelation(@PathVariable("code") String code,
                                                              @Parameter(
                                                                      description = "Updated itemRelation",
                                                                      required = true,
                                                                      schema = @Schema(implementation = ItemRelationCore.class)) @RequestBody ItemRelationCore itemRelationCore) {

        return ResponseEntity.ok(itemRelationService.updateItemRelation(code, itemRelationCore));
    }

    @Operation(summary = "Delete itemRelation by its code")
    @DeleteMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteItemRelation(@PathVariable("code") String code,
                                   @RequestParam(value = "force", required = false, defaultValue = "false") boolean force) {
        itemRelationService.deleteItemRelation(code, force);
    }

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
