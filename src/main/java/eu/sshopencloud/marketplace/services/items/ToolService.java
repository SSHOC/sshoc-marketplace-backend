package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.items.ItemsDifferencesDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.dto.tools.PaginatedTools;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.mappers.tools.ToolMapper;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.repositories.items.*;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.auth.UserService;
import eu.sshopencloud.marketplace.services.items.exception.ItemIsAlreadyMergedException;
import eu.sshopencloud.marketplace.services.items.exception.VersionNotChangedException;
import eu.sshopencloud.marketplace.services.search.IndexItemService;
import eu.sshopencloud.marketplace.services.sources.SourceService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.services.vocabularies.VocabularyService;
import eu.sshopencloud.marketplace.validators.tools.ToolFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@Slf4j
public class ToolService extends ItemCrudService<Tool, ToolDto, PaginatedTools, ToolCore> {

    private final ToolRepository toolRepository;
    private final ToolFactory toolFactory;


    public ToolService(ToolRepository toolRepository, ToolFactory toolFactory,
                       ItemRepository itemRepository, VersionedItemRepository versionedItemRepository,
                       ItemVisibilityService itemVisibilityService, ItemUpgradeRegistry<Tool> itemUpgradeRegistry,
                       DraftItemRepository draftItemRepository, ItemRelatedItemService itemRelatedItemService,
                       PropertyTypeService propertyTypeService, IndexItemService indexItemService, UserService userService,
                       MediaStorageService mediaStorageService, SourceService sourceService, ApplicationEventPublisher eventPublisher,
                       VocabularyService vocabularyService) {

        super(
                itemRepository, versionedItemRepository, itemVisibilityService, itemUpgradeRegistry, draftItemRepository,
                itemRelatedItemService, propertyTypeService, indexItemService, userService, mediaStorageService, sourceService,
                eventPublisher, vocabularyService
        );

        this.toolRepository = toolRepository;
        this.toolFactory = toolFactory;
    }


    public PaginatedTools getTools(PageCoords pageCoords, boolean approved) {
        return getItemsPage(pageCoords, approved);
    }

    public ToolDto getToolVersion(String persistentId, long versionId) {
        return getItemVersion(persistentId, versionId);
    }

    public ToolDto getLatestTool(String persistentId, boolean draft, boolean approved, boolean redirect) {
        return getLatestItem(persistentId, draft, approved, redirect);
    }

    public ToolDto createTool(ToolCore toolCore, boolean draft) {
        Tool tool = createItem(toolCore, draft);
        return prepareItemDto(tool);
    }

    public ToolDto updateTool(String persistentId, ToolCore toolCore, boolean draft, boolean approved) throws VersionNotChangedException {
        return updateTool(persistentId, toolCore, draft, approved, false);
    }

    public ToolDto updateTool(String persistentId, ToolCore toolCore, boolean draft, boolean approved, boolean patchMode) throws VersionNotChangedException {
        Tool tool = updateItem(persistentId, toolCore, draft, approved, patchMode);
        return prepareItemDto(tool);
    }

    public ToolDto revertTool(String persistentId, long versionId) {
        Tool tool = revertItemVersion(persistentId, versionId);
        return prepareItemDto(tool);
    }

    public ToolDto revertTool(String persistentId) {
        Tool tool = revertItemVersion(persistentId);
        return prepareItemDto(tool);
    }

    public ToolDto commitDraftTool(String persistentId) {
        Tool tool = publishDraftItem(persistentId);
        return prepareItemDto(tool);
    }

    public void deleteTool(String persistentId, boolean draft) {
        deleteItem(persistentId, draft);
    }

    public void deleteTool(String persistentId, long versionId) {
        deleteItem(persistentId, versionId);
    }


    @Override
    protected ItemVersionRepository<Tool> getItemRepository() {
        return toolRepository;
    }

    @Override
    protected Tool makeItem(ToolCore toolCore, Tool prevTool, boolean conflict) {
        return toolFactory.create(toolCore, prevTool, conflict);
    }

    @Override
    protected Tool modifyItem(ToolCore toolCore, Tool tool) {
        return toolFactory.modify(toolCore, tool);
    }

    @Override
    protected Tool makeItemCopy(Tool tool) {
        return toolFactory.makeNewVersion(tool);
    }

    @Override
    protected PaginatedTools wrapPage(Page<Tool> toolsPage, List<ToolDto> tools) {
        return PaginatedTools.builder().tools(tools)
                .count(toolsPage.getContent().size())
                .hits(toolsPage.getTotalElements())
                .page(toolsPage.getNumber() + 1)
                .perpage(toolsPage.getSize())
                .pages(toolsPage.getTotalPages())
                .build();
    }

    @Override
    protected ToolDto convertItemToDto(Tool tool) {
        ToolDto dto = ToolMapper.INSTANCE.toDto(tool);
        if(LoggedInUserHolder.getLoggedInUser() ==null || !LoggedInUserHolder.getLoggedInUser().isModerator()){
            dto.getInformationContributor().setEmail(null);
            dto.getContributors().forEach(contributor -> contributor.getActor().setEmail(null));
        }
        return dto;
    }

    @Override
    protected ToolDto convertToDto(Item item) {
        ToolDto dto = ToolMapper.INSTANCE.toDto(item);
        if(LoggedInUserHolder.getLoggedInUser() ==null || !LoggedInUserHolder.getLoggedInUser().isModerator()){
            dto.getInformationContributor().setEmail(null);
            dto.getContributors().forEach(contributor -> contributor.getActor().setEmail(null));
        }
        return dto;
    }

    @Override
    protected String getItemTypeName() {
        return Tool.class.getName();
    }

    public List<ItemExtBasicDto> getToolVersions(String persistentId, boolean draft, boolean approved) {
        return getItemHistory(persistentId, getLatestTool(persistentId, draft, approved, false).getId());
    }

    public List<UserDto> getInformationContributors(String id) {
        return super.getInformationContributors(id);
    }

    public List<UserDto> getInformationContributors(String id, Long versionId) {
        return super.getInformationContributors(id, versionId);
    }

    public ToolDto getMerge(String persistentId, List<String> mergeList) {
        return prepareMergeItems(persistentId, mergeList);
    }

    public ToolDto merge(ToolCore mergeTool, List<String> mergeList) throws ItemIsAlreadyMergedException {
        checkIfMergeIsPossible(mergeList);
        Tool tool = createItem(mergeTool, false);
        tool = mergeItem(tool.getPersistentId(), mergeList);
        return prepareItemDto(tool);
    }

    public List<SourceDto> getSources(String persistentId) {
        return getAllSources(persistentId);
    }

    public ItemsDifferencesDto getDifferences(String toolPersistentId, Long toolVersionId, String otherPersistentId, Long otherVersionId) {

        return super.getDifferences(toolPersistentId, toolVersionId, otherPersistentId, otherVersionId);
    }

}
