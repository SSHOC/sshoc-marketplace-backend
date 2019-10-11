package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.model.items.ItemRelation;
import eu.sshopencloud.marketplace.services.items.ItemRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ItemRelationController {

    private final ItemRelationService itemRelationService;

    @GetMapping("/item-relations")
    public ResponseEntity<List<ItemRelation>> getAllItemRelations() {
        List<ItemRelation> itemRelations = itemRelationService.getAllItemRelations();
        return ResponseEntity.ok(itemRelations);
    }

}
