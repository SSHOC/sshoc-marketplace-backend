package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.services.items.ItemCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/items-categories")
@RequiredArgsConstructor
public class ItemCategoryController {


    private final ItemCategoryService itemCategoryService;

    @Operation(summary = "List item categories")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> getItemCategories() {
        return ResponseEntity.ok(itemCategoryService.getItemCategories());
    }


}
