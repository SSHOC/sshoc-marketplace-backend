package eu.sshopencloud.marketplace.model.search;

import eu.sshopencloud.marketplace.services.PaginatedResult;
import lombok.Data;

import java.util.List;

@Data
public class PaginatedSearchResult extends PaginatedResult {

    public PaginatedSearchResult(List<SearchResultItem> items, long hits, int page, int perpage, int pages) {
        this.setItems(items);
        this.setHits(hits);
        this.setCount(items.size());
        this.setPage(page);
        this.setPerpage(perpage);
        this.setPages(pages);
    }

    private List<SearchResultItem> items;

}
