package eu.sshopencloud.marketplace.controllers.search;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.search.*;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.services.search.IllegalFilterException;
import eu.sshopencloud.marketplace.services.search.SearchService;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilter;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class SearchController {

    private final PageCoordsValidator pageCoordsValidator;

    private final SearchService searchService;


    @GetMapping("/item-search")
    @Operation(description = "Search among items.")
    public ResponseEntity<PaginatedSearchItems> searchItems(
            @RequestParam(value = "q", required = false) String q,
            @Parameter(
                    description = "Dynamic property filter parameters should be provided with putting multiple d.{property}={expression} as request parameters. Allowed property codes: "
                            + "contributor, external-identifier and those codes returned by GET /api/property-types .", schema = @Schema(type = "string"))
            @RequestParam(required = false) MultiValueMap<String, String> d,
            @RequestParam(value = "categories", required = false) List<ItemCategory> categories,
            @RequestParam(value = "order", required = false) List<SearchOrder> order,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "perpage", required = false) Integer perpage,
            @RequestParam(value = "advanced", defaultValue = "false") boolean advanced,
            @Parameter(
                    description = "Facets parameters should be provided with putting multiple f.{filter-name}={value} as request parameters. Allowed filter names: "
                            + SearchFilter.ITEMS_INDEX_TYPE_FILTERS + ".", schema = @Schema(type = "string"))
            @RequestParam(required = false) MultiValueMap<String, String> f) throws PageTooLargeException, IllegalFilterException {

        Map<String, String> expressionParams = UrlParamsExtractor.extractExpressionParams(d);
        Map<String, List<String>> filterParams = UrlParamsExtractor.extractFilterParams(f);
        return ResponseEntity.ok(searchService.searchItems(q, advanced, expressionParams, categories, filterParams, order,
                pageCoordsValidator.validate(page, perpage)));
    }

    @GetMapping("/concept-search")
    @Operation(description = "Search among concepts.")
    public ResponseEntity<PaginatedSearchConcepts> searchConcepts(@RequestParam(value = "q", required = false) String q,
                                                                  @RequestParam(value = "types", required = false) List<String> types,
                                                                  @RequestParam(value = "page", required = false) Integer page,
                                                                  @RequestParam(value = "perpage", required = false) Integer perpage,
                                                                  @RequestParam(value = "advanced", defaultValue = "false") boolean advanced)
            throws PageTooLargeException {

        return ResponseEntity.ok(searchService.searchConcepts(q, advanced, types, pageCoordsValidator.validate(page, perpage)));
    }

    @GetMapping("/item-search/autocomplete")
    @Operation(description = "Autocomplete for items search.")
    public ResponseEntity<SuggestedSearchPhrases> autocompleteItems(@RequestParam("q") String query) {
        SuggestedSearchPhrases suggestions = searchService.autocompleteItemsSearch(query);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/actor-search")
    @Operation(description = "Search among actors.")
    public ResponseEntity<PaginatedSearchActor> searchActors(@RequestParam(value = "q", required = false) String q,
                                                             @RequestParam(value = "page", required = false) Integer page,
                                                             @RequestParam(value = "perpage", required = false) Integer perpage,
                                                             @Parameter(
                                                                     description = "Dynamic property filter parameters should be provided with putting multiple d.{property}={expression} as request parameters. Allowed property codes: "
                                                                             + " name, email, website, external-identifier .", schema = @Schema(type = "string"))
                                                             @RequestParam(required = false) MultiValueMap<String, String> d,
                                                             @RequestParam(value = "advanced", defaultValue = "false") boolean advanced)

            throws PageTooLargeException {

        Map<String, String> expressionParams = UrlParamsExtractor.extractExpressionParams(d);
        return ResponseEntity.ok(searchService.searchActors(q, advanced, expressionParams, pageCoordsValidator.validate(page, perpage)));
    }

}
