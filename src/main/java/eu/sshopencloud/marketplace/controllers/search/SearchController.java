package eu.sshopencloud.marketplace.controllers.search;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.search.SearchOrder;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.services.search.IllegalFilterException;
import eu.sshopencloud.marketplace.services.search.PaginatedSearchConcepts;
import eu.sshopencloud.marketplace.services.search.PaginatedSearchItems;
import eu.sshopencloud.marketplace.services.search.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
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
public class SearchController {

    @Value("${marketplace.pagination.default-perpage}")
    private Integer defualtPerpage;

    @Value("${marketplace.pagination.maximal-perpage}")
    private Integer maximalPerpage;

    private final SearchService searchService;

    @GetMapping(path = "/item-search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Search among items.")
    public ResponseEntity<PaginatedSearchItems> searchItems(@RequestParam(value = "q", required = false) String q,
                                                            @RequestParam(value = "categories", required = false) List<ItemCategory> categories,
                                                            @RequestParam(value = "order", required = false) List<SearchOrder> order,
                                                            @RequestParam(value = "page", required = false) Integer page,
                                                            @RequestParam(value = "perpage", required = false) Integer perpage,
                                                            @Parameter(description = "Facets parameters should provided with putting multiple f.{filter-name}={value} as request parameters. Allowed filter names: object-type, activity, keyword.")
                                                            @RequestParam(required = false) MultiValueMap<String, String> f)
            throws PageTooLargeException, IllegalFilterException {
        // TODO #13 use enum FilterName to tell swagger the possible filterNames via io.swagger.v3.oas.annotations.media.Schema
        perpage = perpage == null ? defualtPerpage : perpage;
        if (perpage > maximalPerpage) {
            throw new PageTooLargeException(maximalPerpage);
        }
        page = page == null ? 1 : page;

        Map<String, List<String>> filterParams = FilterParamsExtractor.extractFilterParams(f);
        PaginatedSearchItems items = searchService.searchItems(q, categories, filterParams, order, page, perpage);
        return ResponseEntity.ok(items);
    }

    @GetMapping(path = "/concept-search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedSearchConcepts> searchItems(@RequestParam(value = "q", required = false) String q,
                                                               @RequestParam(value = "types", required = false) List<String> types,
                                                               @RequestParam(value = "page", required = false) Integer page,
                                                               @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        perpage = perpage == null ? defualtPerpage : perpage;
        if (perpage > maximalPerpage) {
            throw new PageTooLargeException(maximalPerpage);
        }
        page = page == null ? 1 : page;

        PaginatedSearchConcepts concepts = searchService.searchConcepts(q, types, page, perpage);
        return ResponseEntity.ok(concepts);
    }

}
