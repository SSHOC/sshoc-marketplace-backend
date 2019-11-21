package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.repositories.search.IndexItemRepository;
import eu.sshopencloud.marketplace.services.items.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class IndexService {

    private final IndexItemRepository indexItemRepository;

    private final ItemService itemService;

    public IndexItem indexItem(Item item) {
        if (itemService.isNewestVersion(item)) {
            if (item.getPrevVersion() != null) {
                removeItem(item.getPrevVersion());
            }
            IndexItem indexItem = IndexConverter.covertItem(item);
            return indexItemRepository.save(indexItem);
        }
        return null;
    }

    public void removeItem(Item item) {
        indexItemRepository.deleteById(item.getId());
    }

    public void clearIndex() {
        indexItemRepository.deleteAll();
    }

}
