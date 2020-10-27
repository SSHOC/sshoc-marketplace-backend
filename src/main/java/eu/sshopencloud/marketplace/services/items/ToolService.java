package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.tools.PaginatedTools;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.mappers.tools.ToolMapper;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.repositories.items.*;
import eu.sshopencloud.marketplace.services.auth.UserService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.validators.tools.ToolFactory;
import lombok.extern.slf4j.Slf4j;
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
                       DraftItemRepository draftItemRepository, ItemRelatedItemService itemRelatedItemService,
                       PropertyTypeService propertyTypeService, IndexService indexService, UserService userService) {

        super(
                itemRepository, versionedItemRepository, draftItemRepository,
                itemRelatedItemService, propertyTypeService, indexService, userService
        );

        this.toolRepository = toolRepository;
        this.toolFactory = toolFactory;
    }


    public PaginatedTools getTools(PageCoords pageCoords) {
        return super.getItemsPage(pageCoords);
    }

    public ToolDto getToolVersion(String persistentId, long versionId) {
        return getItemVersion(persistentId, versionId);
    }

    public ToolDto getLatestTool(String persistentId, boolean draft) {
        return getLatestItem(persistentId, draft);
    }

    public ToolDto createTool(ToolCore toolCore, boolean draft) {
        Tool tool = createItem(toolCore, draft);
        return prepareItemDto(tool);
    }

    public ToolDto updateTool(String persistentId, ToolCore toolCore, boolean draft) {
        Tool tool = updateItem(persistentId, toolCore, draft);
        return prepareItemDto(tool);
    }

    public ToolDto revertTool(String persistentId, long versionId) {
        Tool tool = revertItemVersion(persistentId, versionId);
        return prepareItemDto(tool);
    }

    public void deleteTool(String persistentId) {
        deleteItem(persistentId);
    }


    @Override
    protected ItemVersionRepository<Tool> getItemRepository() {
        return toolRepository;
    }

    @Override
    protected Tool makeItem(ToolCore toolCore, Tool prevTool) {
        return toolFactory.create(toolCore, prevTool);
    }

    @Override
    protected Tool modifyItem(ToolCore toolCore, Tool tool) {
        return toolFactory.modify(toolCore, tool);
    }

    @Override
    protected Tool makeVersionCopy(Tool tool) {
        return toolFactory.makeNewVersion(tool);
    }

    @Override
    protected PaginatedTools wrapPage(Page<Tool> toolsPage, List<ToolDto> tools) {
        return PaginatedTools.builder().tools(tools)
                .count(toolsPage.getContent().size())
                .hits(toolsPage.getTotalElements())
                .page(toolsPage.getNumber())
                .perpage(toolsPage.getSize())
                .pages(toolsPage.getTotalPages())
                .build();
    }

    @Override
    protected ToolDto convertItemToDto(Tool tool) {
        return ToolMapper.INSTANCE.toDto(tool);
    }

    @Override
    protected String getItemTypeName() {
        return Tool.class.getName();
    }
}
