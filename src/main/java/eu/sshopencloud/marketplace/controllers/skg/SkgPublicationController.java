package eu.sshopencloud.marketplace.controllers.skg;

import eu.sshopencloud.marketplace.model.skg.JsonLdDocument;
import eu.sshopencloud.marketplace.services.skg.SkgPublicationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides read-only access to the marketplace {@link eu.sshopencloud.marketplace.model.publications.Publication}s in SKG-IF compliant format
 */
@RestController("skgPublicationController")
@RequestMapping("/api/publications")
@RequiredArgsConstructor
public class SkgPublicationController {

    private final SkgPublicationService skgPublicationService;

    @Operation(summary = "Get single publication by its persistentId in SKG format")
    @GetMapping(path = "/{persistentId}", produces = "application/ld+json")
    public ResponseEntity<JsonLdDocument> getDataset(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(skgPublicationService.getPublication(persistentId));
    }

}
