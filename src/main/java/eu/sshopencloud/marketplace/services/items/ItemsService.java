package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.dto.items.ItemOrder;
import eu.sshopencloud.marketplace.dto.items.PaginatedItemsBasic;
import eu.sshopencloud.marketplace.dto.search.*;
import eu.sshopencloud.marketplace.mappers.items.ItemConverter;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.DraftItem;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.model.sources.Source;
import eu.sshopencloud.marketplace.repositories.items.DraftItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.repositories.search.SearchItemRepository;
import eu.sshopencloud.marketplace.repositories.sources.SourceRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.search.SearchConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.*;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleStringCriteria;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class ItemsService extends ItemVersionService<Item> {

    private final ItemRepository itemRepository;
    private final DraftItemRepository draftItemRepository;

    private final SourceRepository sourceRepository;
    private final SearchItemRepository searchItemRepository;

    private final ToolService toolService;
    private final TrainingMaterialService trainingMaterialService;
    private final PublicationService publicationService;
    private final DatasetService datasetService;
    private final WorkflowService workflowService;
    private final StepService stepService;



    public ItemsService(ItemRepository itemRepository, DraftItemRepository draftItemRepository, VersionedItemRepository versionedItemRepository,
                        ItemVisibilityService itemVisibilityService,
                        SourceRepository sourceRepository, SearchItemRepository searchItemRepository,
                        @Lazy ToolService toolService, @Lazy TrainingMaterialService trainingMaterialService,
                        @Lazy PublicationService publicationService, @Lazy DatasetService datasetService,
                        @Lazy WorkflowService workflowService, @Lazy StepService stepService) {

        super(versionedItemRepository, itemVisibilityService);

        this.itemRepository = itemRepository;
        this.draftItemRepository = draftItemRepository;
        this.sourceRepository = sourceRepository;
        this.searchItemRepository = searchItemRepository;
        this.toolService = toolService;
        this.trainingMaterialService = trainingMaterialService;
        this.publicationService = publicationService;
        this.datasetService = datasetService;
        this.workflowService = workflowService;
        this.stepService = stepService;
    }

    public PaginatedItemsBasic getMyDraftItems(ItemOrder order, PageCoords pageCoords) {
        if (order == null) order = ItemOrder.MODIFIED_ON;

        Page<DraftItem> draftItemsPage = draftItemRepository.findByOwner(LoggedInUserHolder.getLoggedInUser(),
                PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(getSortOrderByItemOrder(order))));

        List<ItemBasicDto> items = draftItemsPage.stream().map(draftItem -> ItemConverter.convertItem(draftItem.getItem())).collect(Collectors.toList());

        return PaginatedItemsBasic.builder().items(items)
                .count(draftItemsPage.getContent().size()).hits(draftItemsPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(draftItemsPage.getTotalPages())
                .build();
    }

    private Sort.Order getSortOrderByItemOrder(ItemOrder itemOrder) {
        switch (itemOrder) {
            case LABEL:
                if (itemOrder.isAsc()) {
                    return Sort.Order.asc("item.label");
                } else {
                    return Sort.Order.desc("item.label");
                }
            case MODIFIED_ON:
                if (itemOrder.isAsc()) {
                    return Sort.Order.asc("item.lastInfoUpdate");
                } else {
                    return Sort.Order.desc("item.lastInfoUpdate");
                }
            default:
                return Sort.Order.desc("item.lastInfoUpdate");
        }
    }


    public PaginatedSearchItemsBasic getItemsBySource(Long sourceId, PageCoords pageCoords) {
        return getItemsBySource(sourceId, null, pageCoords);
    }

    public PaginatedSearchItemsBasic getItemsBySource(Long sourceId, String sourceItemId, PageCoords pageCoords) {
        Source source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Source.class.getName() + " with id " + sourceId));

        return findLatestItemsForSource(source, sourceItemId, pageCoords);
    }


    public PaginatedSearchItemsBasic findLatestItemsForSource(Source source, String sourceItemId, PageCoords pageCoords) {
        Pageable pageable = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage()); // SOLR counts from page 0

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("{!parent which='doc_content_type:item'} doc_content_type:source AND source_label:");
        queryBuilder.append("\"").append(source.getLabel()).append("\"");
        if (StringUtils.isNotBlank(sourceItemId)) {
            queryBuilder.append(" AND source_item_id:");
            queryBuilder.append("\"").append(sourceItemId).append("\"");
        }
        Criteria queryCriteria = new SimpleStringCriteria(queryBuilder.toString());

        User currentUser = LoggedInUserHolder.getLoggedInUser();
        FacetPage<IndexItem> facetPage = searchItemRepository.findByQuery(queryCriteria, currentUser, SearchOrder.LABEL, pageable);

        PaginatedSearchItemsBasic result = PaginatedSearchItemsBasic.builder()
                .items(
                        facetPage.get()
                                .map(SearchConverter::convertIndexItemBasic)
                                .collect(Collectors.toList())
                )
                .hits(facetPage.getTotalElements()).count(facetPage.getNumberOfElements())
                .page(pageCoords.getPage())
                .perpage(pageCoords.getPerpage())
                .pages(facetPage.getTotalPages())
                .build();

        return result;
    }


    public Item liftItemVersion(String persistentId, boolean draft, boolean changeStatus) {
        Item currentItem = loadCurrentItem(persistentId);
        return resolveItemsService(currentItem.getCategory()).liftItemVersion(persistentId, draft, changeStatus);
    }

    private ItemCrudService<? extends Item, ?, ?, ?> resolveItemsService(ItemCategory category) {
        switch (category) {
            case TOOL_OR_SERVICE:
                return toolService;

            case TRAINING_MATERIAL:
                return trainingMaterialService;

            case PUBLICATION:
                return publicationService;

            case DATASET:
                return datasetService;

            case WORKFLOW:
                return workflowService;

            case STEP:
                return stepService;

            default:
                throw new IllegalStateException(String.format("Unexpected item type: %s", category));
        }
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
