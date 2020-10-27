package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.mappers.items.ItemConverter;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class ItemsService extends ItemVersionService<Item> {

    private final ItemRepository itemRepository;

    public ItemsService(ItemRepository itemRepository, VersionedItemRepository versionedItemRepository) {
        super(versionedItemRepository);
        this.itemRepository = itemRepository;
    }


    public List<ItemBasicDto> getItems(Long sourceId, String sourceItemId) {
        List<Item> items = itemRepository.findBySourceIdAndSourceItemId(sourceId, sourceItemId);
        return items.stream().map(ItemConverter::convertItem).collect(Collectors.toList());
    }

    @Override
    protected Item loadLatestItem(String persistentId) {
        return super.loadLatestItem(persistentId);
    }

    @Override
    protected ItemVersionRepository<Item> getItemRepository() {
        return itemRepository;
    }

    @Override
    protected String getItemTypeName() {
        return Item.class.getName();
    }
}
