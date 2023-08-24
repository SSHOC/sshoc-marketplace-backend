package eu.sshopencloud.marketplace.repositories.oaipmh;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import io.gdcc.xoai.dataprovider.exceptions.handler.HandlerException;
import io.gdcc.xoai.dataprovider.exceptions.handler.IdDoesNotExistException;
import io.gdcc.xoai.dataprovider.filter.ScopedFilter;
import io.gdcc.xoai.dataprovider.model.Item;
import io.gdcc.xoai.dataprovider.model.ItemIdentifier;
import io.gdcc.xoai.dataprovider.model.MetadataFormat;
import io.gdcc.xoai.dataprovider.repository.ItemRepository;
import io.gdcc.xoai.dataprovider.repository.ResultsPage;
import io.gdcc.xoai.model.oaipmh.ResumptionToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OaiPmhItemRepository implements ItemRepository {

    public static final String OAIPMH_SPLIT_CHARACTER = ":";
    private final eu.sshopencloud.marketplace.repositories.items.ItemRepository itemRepository;
    private final ItemRelatedItemService itemRelatedItemService;

    @Override
    public ItemIdentifier getItemIdentifier(String oaiId) throws IdDoesNotExistException {
        eu.sshopencloud.marketplace.model.items.Item item = itemRepository.findActiveByPersistentIdAndStatusAndCategoryNot(
                        extractPersistentId(oaiId), ItemStatus.APPROVED, ItemCategory.STEP)
                .orElseThrow(IdDoesNotExistException::new);
        return OaiItemIdentifier.fromItem(item);
    }

    private String extractPersistentId(String oaiId) {
        String[] elements = oaiId.split(OAIPMH_SPLIT_CHARACTER);
        return elements[elements.length - 1];
    }

    @Override
    public Item getItem(String oaiId, MetadataFormat metadataFormat) throws HandlerException {
        eu.sshopencloud.marketplace.model.items.Item item = itemRepository.findActiveByPersistentIdAndStatusAndCategoryNot(
                        extractPersistentId(oaiId), ItemStatus.APPROVED, ItemCategory.STEP)
                .orElseThrow(IdDoesNotExistException::new);
        return new OaiItem(item, itemRelatedItemService.getItemRelatedItems(item));
    }

    @Override
    public ResultsPage<ItemIdentifier> getItemIdentifiers(List<ScopedFilter> list, MetadataFormat metadataFormat, int i,
            ResumptionToken.Value value) {
        Page<eu.sshopencloud.marketplace.model.items.Item> items = getItems(i, value);
        return pageToIdentifiers(items, value);
    }

    @Override
    public ResultsPage<Item> getItems(List<ScopedFilter> list, MetadataFormat metadataFormat, int i,
            ResumptionToken.Value value) {
        Page<eu.sshopencloud.marketplace.model.items.Item> items = getItems(i, value);
        return pageToItems(items, value);
    }

    private Page<eu.sshopencloud.marketplace.model.items.Item> getItems(int i, ResumptionToken.Value value) {
        long page = Math.floorDiv(value.getOffset(), i);
        if (page > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Cannot handle page greater than maximum value of int");
        }
        Page<eu.sshopencloud.marketplace.model.items.Item> items;
        if (value.hasFrom() && value.hasUntil()) {
            items = itemRepository.findAllActiveByStatusAndCategoryNotAndLastInfoUpdateGreaterThanEqualAndLastInfoUpdateLessThanEqualOrderByLastInfoUpdateDesc(
                    ItemStatus.APPROVED, ItemCategory.STEP, value.getFrom().atZone(ZoneId.systemDefault()),
                    value.getUntil().atZone(ZoneId.systemDefault()), PageRequest.of((int) page, i, Sort.by("id")));
        } else if (value.hasFrom()) {
            items = itemRepository.findAllActiveByStatusAndCategoryNotAndLastInfoUpdateGreaterThanEqualOrderByLastInfoUpdateDesc(
                    ItemStatus.APPROVED, ItemCategory.STEP, value.getFrom().atZone(ZoneId.systemDefault()),
                    PageRequest.of((int) page, i, Sort.by("id")));
        } else if (value.hasUntil()) {
            items = itemRepository.findAllActiveByStatusAndCategoryNotAndLastInfoUpdateLessThanEqualOrderByLastInfoUpdateDesc(
                    ItemStatus.APPROVED, ItemCategory.STEP, value.getUntil().atZone(ZoneId.systemDefault()),
                    PageRequest.of((int) page, i, Sort.by("id")));
        } else {
            items = itemRepository.findAllActiveByStatusAndCategoryNotOrderByLastInfoUpdateDesc(ItemStatus.APPROVED,
                    ItemCategory.STEP, PageRequest.of((int) page, i, Sort.by("id")));
        }
        return items;
    }

    public Optional<Instant> getMinDate() {
        return itemRepository.getMinLastUpdateDateOfActiveItemByStatusAndNotCategory(ItemStatus.APPROVED,
                ItemCategory.STEP).map(ZonedDateTime::toInstant);
    }

    private ResultsPage<ItemIdentifier> pageToIdentifiers(Page<eu.sshopencloud.marketplace.model.items.Item> page,
            ResumptionToken.Value value) {
        List<ItemIdentifier> itemIdentifiers = page.toList().stream().map(OaiItemIdentifier::fromItem)
                .collect(Collectors.toList());
        return new ResultsPage<>(value, page.hasNext(), itemIdentifiers, (int) page.getTotalElements());
    }

    private ResultsPage<Item> pageToItems(Page<eu.sshopencloud.marketplace.model.items.Item> page,
            ResumptionToken.Value value) {
        List<Item> oaiItems = page.toList().stream()
                .map(i -> new OaiItem(i, itemRelatedItemService.getItemRelatedItems(i))).collect(Collectors.toList());
        return new ResultsPage<>(value, page.hasNext(), oaiItems, (int) page.getTotalElements());
    }
}
