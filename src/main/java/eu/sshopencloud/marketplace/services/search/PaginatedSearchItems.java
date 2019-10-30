package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.search.SearchItem;
import eu.sshopencloud.marketplace.services.PaginatedResult;
import lombok.Data;

import java.util.List;

@Data
public class PaginatedSearchItems extends PaginatedResult {

    public PaginatedSearchItems(List<SearchItem> items, long hits, int page, int perpage, int pages) {
        this.setItems(items);
        this.setHits(hits);
        this.setCount(items.size());
        this.setPage(page);
        this.setPerpage(perpage);
        this.setPages(pages);
    }

    private List<SearchItem> items;

}
