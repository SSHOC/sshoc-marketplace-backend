package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PaginatedResult;
import eu.sshopencloud.marketplace.dto.items.ItemCore;
import eu.sshopencloud.marketplace.dto.items.ItemDto;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import org.springframework.data.domain.Page;

import java.util.List;


interface ItemCrudProvider<I extends Item, D extends ItemDto, C extends ItemCore, P extends PaginatedResult<D>> {

    ItemVersionRepository<I> getItemRepository();

    I makeItem(C itemCore, I prevItem);
    P wrapPage(Page<I> resultsPage, List<D> convertedDtos);

    D convertItemToDto(I item);

    String getEntityName();
}
