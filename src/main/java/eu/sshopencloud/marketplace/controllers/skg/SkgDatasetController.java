package eu.sshopencloud.marketplace.controllers.skg;

import eu.sshopencloud.marketplace.model.skg.JsonLdDocument;
import eu.sshopencloud.marketplace.services.skg.SkgDatasetService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Provides read-only access to the marketplace {@link eu.sshopencloud.marketplace.model.datasets.Dataset}s in SKG-IF compliant format
 */
@RestController("skgDatasetController")
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class SkgDatasetController {

    private final SkgDatasetService skgDatasetService;

    @Operation(summary = "Get single dataset by its persistentId")
    @GetMapping(path = "/{persistentId}", produces = "application/ld+json")
    public ResponseEntity<JsonLdDocument> getDataset(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(skgDatasetService.getDataset(persistentId));
    }

}