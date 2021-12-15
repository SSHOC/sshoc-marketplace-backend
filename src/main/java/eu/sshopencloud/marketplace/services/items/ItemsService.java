package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.dto.items.ItemOrder;
import eu.sshopencloud.marketplace.dto.items.PaginatedItemsBasic;
import eu.sshopencloud.marketplace.dto.search.*;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.mappers.items.ItemContributorMapper;
import eu.sshopencloud.marketplace.mappers.items.ItemConverter;
import eu.sshopencloud.marketplace.mappers.vocabularies.PropertyMapper;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.DraftItem;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.model.sources.Source;
import eu.sshopencloud.marketplace.repositories.items.DraftItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.repositories.search.SearchItemRepository;
import eu.sshopencloud.marketplace.repositories.sources.SourceRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.search.IllegalFilterException;
import eu.sshopencloud.marketplace.services.search.SearchConverter;
import eu.sshopencloud.marketplace.services.search.SearchService;
import eu.sshopencloud.marketplace.services.search.filter.IndexType;
import eu.sshopencloud.marketplace.services.search.filter.SearchExpressionCriteria;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilterCriteria;
import eu.sshopencloud.marketplace.services.search.query.ItemSearchQueryPhrase;
import eu.sshopencloud.marketplace.services.search.query.SearchQueryCriteria;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.*;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleStringCriteria;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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


    public PaginatedItemsBasic getItemsBySource(Long sourceId, boolean approved, PageCoords pageCoords) {
        return getItemsBySource(sourceId, null, approved, pageCoords);
    }

    public PaginatedItemsBasic getItemsBySource(Long sourceId, String sourceItemId, boolean approved, PageCoords pageCoords) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();
        Source source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Source.class.getName() + " with id " + sourceId));

        Page<Item> itemsPage = loadLatestItemsForSource(pageCoords, source, sourceItemId, currentUser, approved);

        List<ItemBasicDto> items = itemsPage.stream().map(ItemConverter::convertItem).collect(Collectors.toList());

        return PaginatedItemsBasic.builder()
                .items(items)
                .count(itemsPage.getContent().size()).hits(itemsPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(itemsPage.getTotalPages())
                .build();
    }

    private Page<Item> loadLatestItemsForSource(PageCoords pageCoords, Source source, String sourceItemId, User user, boolean approved) {
        PageRequest pageRequest = PageRequest.of(
                pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))
        );

        // TODO change to SOLR query
        //searchService.searchItems()

        if (approved || user == null) {
            if (sourceItemId == null) {
                return itemRepository.findAllLatestApprovedItemsForSource(source, pageRequest);
            } else {
                return itemRepository.findAllLatestApprovedItemsForSource(source, sourceItemId, pageRequest);
            }
        }

        if (user.isModerator()) {
            if (sourceItemId == null) {
                return itemRepository.findAllLatestItemsForSource(source, pageRequest);
            } else {
                return itemRepository.findAllLatestItemsForSource(source, sourceItemId, pageRequest);
            }
        }

        if (sourceItemId == null) {
            return itemRepository.findUserLatestItemsForSource(source, user, pageRequest);
        } else {
            return itemRepository.findUserLatestItemsForSource(source, sourceItemId, user, pageRequest);
        }
    }



    public PaginatedSearchItems LatestItemsForSource(Source source, String sourceItemId, User user, boolean approved, PageCoords pageCoords)
            throws IllegalFilterException {
        Pageable pageable = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage()); // SOLR counts from page 0

        Criteria queryCriteria = new SimpleStringCriteria("q={!parent which='doc_content_type:item'} doc_content_type:source AND source_label:TAPoR");

        SearchOrder order = SearchOrder.LABEL;

        User currentUser = LoggedInUserHolder.getLoggedInUser();

        FacetPage<IndexItem> facetPage = searchItemRepository.findByQuery(queryCriteria, currentUser, order, pageable);


        PaginatedSearchItems result = PaginatedSearchItems.builder()
                .q("TODO")
                .items(
                        facetPage.get()
                                .map(SearchConverter::convertIndexItem)
                                .collect(Collectors.toList())
                )
                .hits(facetPage.getTotalElements()).count(facetPage.getNumberOfElements())
                .page(pageCoords.getPage())
                .perpage(pageCoords.getPerpage())
                .pages(facetPage.getTotalPages())
                .build();

        // TODO index contributors and properties directly in SOLR in nested docs (?) -
        // TODO in a similar way add external identifiers to the result
        for (SearchItem searchItem : result.getItems()) {
            //searchItem.setContributors(ItemContributorMapper.INSTANCE.toDto(itemContributorService.getItemContributors(searchItem.getId())));
            //searchItem.setProperties(PropertyMapper.INSTANCE.toDto(propertyService.getItemProperties(searchItem.getId())));
            //searchItem.getProperties().stream().map(PropertyDto::getType).forEach(propertyTypeService::completePropertyType);
        }

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
