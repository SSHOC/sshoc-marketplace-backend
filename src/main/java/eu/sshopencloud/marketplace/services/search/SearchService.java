package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.search.SearchItem;
import eu.sshopencloud.marketplace.repositories.search.SearchItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SearchService {

    private final SearchItemRepository searchItemRepository;

    public PaginatedSearchItems searchItems(String q, List<ItemCategory> categories, String order, int page, int perpage) {
        // TODO
        return new PaginatedSearchItems(Collections.emptyList(), 0, page, perpage, 0);
    }


    public SearchItem indexItem(Item item) {
        SearchItem searchItem = SearchItemConverter.covert(item);
        return searchItemRepository.save(searchItem);
    }

    public void removeItem(Item item) {
        searchItemRepository.deleteById(item.getId());
    }

}
