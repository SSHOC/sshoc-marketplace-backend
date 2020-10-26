package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.services.items.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ItemController {

    private final ItemsService itemService;


    @GetMapping(path = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemBasicDto>> getItems(@RequestParam(value = "sourceId", required = true) Long sourceId,
                                                       @RequestParam(value = "sourceItemId", required = true) String sourceItemId) {

        return ResponseEntity.ok(itemService.getItems(sourceId, sourceItemId));
    }
}
