package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemRelationService;
import eu.sshopencloud.marketplace.services.items.ItemsRelationAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ItemRelationController {

    private final ItemRelationService itemRelationService;

    private final ItemRelatedItemService itemRelatedItemService;

    @GetMapping(path = "/item-relations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemRelation>> getAllItemRelations() {
        return ResponseEntity.ok(itemRelationService.getAllItemRelations());
    }

    @PostMapping(path = "/items-relations/{subjectId}/{objectId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemRelatedItem> createItemRelatedItem(@PathVariable("subjectId") long subjectId,
                                                                 @PathVariable("objectId") long objectId,
                                                                 @RequestBody ItemRelationId itemRelation)
            throws ItemsRelationAlreadyExistsException {
        return ResponseEntity.ok(itemRelatedItemService.createItemRelatedItem(subjectId, objectId, itemRelation));
    }

    @DeleteMapping("/items-relations/{subjectId}/{objectId}")
    public void deleteItemRelatedItem(@PathVariable("subjectId") long subjectId, @PathVariable("objectId") long objectId) {
        itemRelatedItemService.deleteItemRelatedItem(subjectId, objectId);
    }

}
