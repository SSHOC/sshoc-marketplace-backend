package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.dto.items.ItemSourceCore;
import eu.sshopencloud.marketplace.dto.items.ItemSourceDto;
import eu.sshopencloud.marketplace.services.items.ItemSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Operation(summary = "Get list of all item sources")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ItemSourceDto>> getAllItemSources() {
        return ResponseEntity.ok(itemSourceService.getAllItemSources());
    }

    @Operation(summary = "Get item sources for given source code")
    @GetMapping(path = "/{sourceCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ItemSourceDto> getItemSource(@PathVariable("sourceCode") String itemSourceCode) {
        return ResponseEntity.ok(itemSourceService.getItemSource(itemSourceCode));
    }

    @Operation(summary = "Create item sources")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ItemSourceDto> createItemSource(@Parameter(
            description = "Created item source",
            required = true,
            schema = @Schema(implementation = ItemSourceCore.class)) @RequestBody ItemSourceCore itemSourceCore) {
        return ResponseEntity.ok(itemSourceService.createItemSource(itemSourceCore));
    }

    @Operation(summary = "Update item sources for given source code")
    @PutMapping(path = "/{sourceCode}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ItemSourceDto> updateItemSource(@PathVariable("sourceCode") String itemSourceCode,
                                                   @Parameter(
                                                           description = "Update item source",
                                                           required = true,
                                                           schema = @Schema(implementation = ItemSourceCore.class)) @RequestBody ItemSourceCore itemSourceCore) {

        return ResponseEntity.ok(itemSourceService.updateItemSource(itemSourceCode, itemSourceCore));
    }

    @Operation(summary = "Delete item sources for given source code")
    @DeleteMapping("/{sourceCode}")
    ResponseEntity<Void> deleteItemSource(@PathVariable("sourceCode") String itemSourceCode) {
        itemSourceService.deleteItemSource(itemSourceCode);
        return ResponseEntity.ok().build();
    }
}
