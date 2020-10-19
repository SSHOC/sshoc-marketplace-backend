package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.mappers.items.ItemConverter;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class ItemsService {

    private final ItemRepository itemRepository;

    public List<ItemBasicDto> getItems(Long sourceId, String sourceItemId) {
        List<Item> items = itemRepository.findBySourceIdAndSourceItemId(sourceId, sourceItemId);
        return items.stream().map(ItemConverter::convertItem).collect(Collectors.toList());
    }
}
