package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.dto.items.ItemSourceCore;
import eu.sshopencloud.marketplace.dto.items.ItemSourceDto;
import eu.sshopencloud.marketplace.services.items.ItemSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/item-sources")
@RequiredArgsConstructor
public class ItemSourceController {

    private final ItemSourceService itemSourceService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ItemSourceDto>> getAllItemSources() {
        return ResponseEntity.ok(itemSourceService.getAllItemSources());
    }

    @GetMapping(path = "/{sourceCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ItemSourceDto> getItemSource(@PathVariable("sourceCode") String itemSourceCode) {
        return ResponseEntity.ok(itemSourceService.getItemSource(itemSourceCode));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ItemSourceDto> createItemSource(@RequestBody ItemSourceCore itemSourceCore) {
        return ResponseEntity.ok(itemSourceService.createItemSource(itemSourceCore));
    }

    @PutMapping(path = "/{sourceCode}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ItemSourceDto> updateItemSource(@PathVariable("sourceCode") String itemSourceCode,
                                                   @RequestBody ItemSourceCore itemSourceCore) {

        return ResponseEntity.ok(itemSourceService.updateItemSource(itemSourceCode, itemSourceCore));
    }

    @DeleteMapping("/{sourceCode}")
    ResponseEntity<Void> deleteItemSource(@PathVariable("sourceCode") String itemSourceCode) {
        itemSourceService.deleteItemSource(itemSourceCode);
        return ResponseEntity.ok().build();
    }
}
