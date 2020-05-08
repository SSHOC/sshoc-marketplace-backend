package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelatedItemDto;
import eu.sshopencloud.marketplace.dto.items.ItemRelationDto;
import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemRelationService;
import eu.sshopencloud.marketplace.services.items.ItemsRelationAlreadyExistsException;
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
    public ResponseEntity<ItemRelatedItemDto> createItemRelatedItem(@PathVariable("subjectId") long subjectId,
                                                                    @PathVariable("objectId") long objectId,
                                                                    @RequestBody ItemRelationId itemRelation)
            throws ItemsRelationAlreadyExistsException {
        return ResponseEntity.ok(itemRelatedItemService.createItemRelatedItem(subjectId, objectId, itemRelation));
    }

    @DeleteMapping("/{subjectId}/{objectId}")
    public void deleteItemRelatedItem(@PathVariable("subjectId") long subjectId, @PathVariable("objectId") long objectId) {
        itemRelatedItemService.deleteItemRelatedItem(subjectId, objectId);
    }

}
