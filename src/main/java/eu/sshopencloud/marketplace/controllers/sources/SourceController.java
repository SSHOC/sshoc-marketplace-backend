package eu.sshopencloud.marketplace.controllers.sources;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.items.PaginatedItemsBasic;
import eu.sshopencloud.marketplace.dto.sources.PaginatedSources;
import eu.sshopencloud.marketplace.dto.sources.SourceCore;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.dto.sources.SourceOrder;
import eu.sshopencloud.marketplace.services.items.ItemsService;
import eu.sshopencloud.marketplace.services.sources.SourceService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class SourceController {

    private final PageCoordsValidator pageCoordsValidator;

    private final SourceService sourceService;

    private final ItemsService itemService;


    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedSources> getSources(@RequestParam(value = "order", required = false) SourceOrder order,
                                                       @RequestParam(value = "q", required = false) String q,
                                                       @RequestParam(value = "page", required = false) Integer page,
                                                       @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(sourceService.getSources(order, q, pageCoordsValidator.validate(page, perpage)));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SourceDto> getSource(@PathVariable("id") Long id) {
        return ResponseEntity.ok(sourceService.getSource(id));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SourceDto> createSource(@Parameter(
            description = "Created source",
            required = true,
            schema = @Schema(implementation = SourceCore.class)) @RequestBody SourceCore newSource) {
        return ResponseEntity.ok(sourceService.createSource(newSource));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SourceDto> updateSource(@PathVariable("id") long id,
                                                  @Parameter(
                                                          description = "Updated source",
                                                          required = true,
                                                          schema = @Schema(implementation = SourceCore.class)) @RequestBody SourceCore updatedSource) {
        return ResponseEntity.ok(sourceService.updateSource(id, updatedSource));
    }

    @DeleteMapping(path = "/{id}")
    public void deleteSource(@PathVariable("id") long id) {
        sourceService.deleteSource(id);
    }



    @Operation(summary = "Get list of items for given source")
    @GetMapping(path = "/{sourceId}/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedItemsBasic> getItemsForSource(@PathVariable("sourceId") Long sourceId,
                                                                 @RequestParam(value = "page", required = false) Integer page,
                                                                 @RequestParam(value = "perpage", required = false) Integer perpage,
                                                                 @RequestParam(value = "approved", defaultValue = "true") boolean approved)
            throws PageTooLargeException {
        return ResponseEntity.ok(itemService.getItemsBySource(sourceId, approved, pageCoordsValidator.validate(page, perpage)));
    }

    @Operation(summary = "Get list of items for given source and id of an item in this source")
    @GetMapping(path = "/{sourceId}/items/{sourceItemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedItemsBasic> getItemSource(@PathVariable("sourceId") Long sourceId,
                                                             @PathVariable("sourceItemId") String sourceItemId,
                                                             @RequestParam(value = "page", required = false) Integer page,
                                                             @RequestParam(value = "perpage", required = false) Integer perpage,
                                                             @RequestParam(value = "approved", defaultValue = "true") boolean approved)
            throws PageTooLargeException {

        return ResponseEntity.ok(itemService.getItemsBySource(sourceId, sourceItemId, approved, pageCoordsValidator.validate(page, perpage)));
    }



}
