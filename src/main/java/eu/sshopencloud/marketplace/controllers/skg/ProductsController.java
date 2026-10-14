package eu.sshopencloud.marketplace.controllers.skg;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.model.skg.JsonLdDocument;
import eu.sshopencloud.marketplace.services.skg.SkgResearchProductService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Provides read-only access to the marketplace {@link eu.sshopencloud.marketplace.model.tools.Tool}s
 * wrapped in SKG-IF Research Product entity: <a href="https://skg-if.github.io/interoperability-framework/docs/research-product.html">Research product</a>
 */
@RestController("skgResearchProductsController")
@RequestMapping("/skg/products")
@RequiredArgsConstructor
public class ProductsController {

    private final SkgResearchProductService researchProductService;
    private final PageCoordsValidator pageCoordsValidator;

    @Operation(summary = "Get all tools or services by its persistentId in SKG format")
    @GetMapping(produces = "application/ld+json")
    public ResponseEntity<JsonLdDocument> getProducts(@RequestParam(value = "page", required = false) Integer page,
                                                      @RequestParam(value = "perpage", required = false) Integer perpage) throws PageTooLargeException {
        PageCoords pageData = pageCoordsValidator.validate(page, perpage);

        return ResponseEntity.ok(researchProductService.getResearchProducts(pageData));
    }

    @Operation(summary = "Get single tool or service by its persistentId in SKG format")
    @GetMapping(path = "/{*identifier}", produces = "application/ld+json")
    public ResponseEntity<JsonLdDocument> getProduct(@PathVariable("identifier") String identifier) {
        return ResponseEntity.ok(researchProductService.getResearchProduct(identifier));
    }



}
