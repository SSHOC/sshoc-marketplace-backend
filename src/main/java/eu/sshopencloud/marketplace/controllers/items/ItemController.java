package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.services.items.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping(path = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemBasicDto>> getItems(@RequestParam(value = "sourceId", required = true) Long sourceId,
                                                      @RequestParam(value = "sourceItemId", required = true) String sourceItemId) {
        return ResponseEntity.ok(itemService.getItems(sourceId, sourceItemId));
    }

    //TEMPORARY
    @GetMapping(path = "/rewrite-sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public void rewriteSources() {
        itemService.rewriteSources();
    }

    //TEMPORARY
    @GetMapping(path = "/rewrite-sources-ids", produces = MediaType.APPLICATION_JSON_VALUE)
    public void rewriteSourcesIds() {
        itemService.rewriteSourcesIds();
    }


}
