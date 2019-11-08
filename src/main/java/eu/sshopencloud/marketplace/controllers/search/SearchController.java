package eu.sshopencloud.marketplace.controllers.search;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.search.SearchOrder;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.services.search.PaginatedSearchItems;
import eu.sshopencloud.marketplace.services.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class SearchController {

    @Value("${marketplace.pagination.default-perpage}")
    private Integer defualtPerpage;

    @Value("${marketplace.pagination.maximal-perpage}")
    private Integer maximalPerpage;

    private final SearchService searchService;

    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedSearchItems> searchItems(@RequestParam(value = "q", required = false) String q,
                                                            @RequestParam(value = "categories", required = false) List<ItemCategory> categories,
                                                            @RequestParam(value = "order", required = false) List<SearchOrder> order,
                                                            @RequestParam(value = "page", required = false) Integer page,
                                                            @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        perpage = perpage == null ? defualtPerpage : perpage;
        if (perpage > maximalPerpage) {
            throw new PageTooLargeException(maximalPerpage);
        }
        page = page == null ? 1 : page;

        PaginatedSearchItems items = searchService.searchItems(q, categories, order, page, perpage);
        return ResponseEntity.ok(items);
    }

}
