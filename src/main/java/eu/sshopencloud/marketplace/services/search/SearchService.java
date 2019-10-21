package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.search.PaginatedSearchResult;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SearchService {

    public PaginatedSearchResult searchItems(String q, List<ItemCategory> categories, String order, int page, int perpage) {
        // TODO
        return new PaginatedSearchResult(Collections.emptyList(), 0, page, perpage, 0);
    }


}
