package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.model.items.ItemComment;
import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ItemRelationController {

    private final ItemRelationService itemRelationService;

    private final ItemRelatedItemService itemRelatedItemService;

    @GetMapping("/item-relations")
    public ResponseEntity<List<ItemRelation>> getAllItemRelations() {
        List<ItemRelation> itemRelations = itemRelationService.getAllItemRelations();
        return ResponseEntity.ok(itemRelations);
    }

    @PostMapping("/items-relations/{subjectId}/{objectId}")
    public ResponseEntity<ItemRelatedItem> createItemComment(@PathVariable("subjectId") long subjectId, @PathVariable("objectId") long objectId,
                                                             @RequestBody ItemRelation itemRelation) {
        ItemRelatedItem itemRelatedItem = itemRelatedItemService.createItemRelatedItem(subjectId, objectId, itemRelation);
        return ResponseEntity.ok(itemRelatedItem);
    }

    @DeleteMapping("/items-relations/{subjectId}/{objectId}")
    public void deleteTool(@PathVariable("subjectId") long subjectId, @PathVariable("objectId") long objectId) {
        itemRelatedItemService.deleteItemRelatedItem(subjectId, objectId);
    }


}
