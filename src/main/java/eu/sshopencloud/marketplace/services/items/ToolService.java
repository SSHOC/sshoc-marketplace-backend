package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.tools.PaginatedTools;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.mappers.tools.ToolMapper;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import eu.sshopencloud.marketplace.repositories.items.ToolRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
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
                       ItemRelatedItemService itemRelatedItemService, PropertyTypeService propertyTypeService,
                       IndexService indexService) {

        super(itemRepository, versionedItemRepository, itemRelatedItemService, propertyTypeService, indexService);

        this.toolRepository = toolRepository;
        this.toolFactory = toolFactory;
    }


    public PaginatedTools getTools(PageCoords pageCoords) {
        return super.getItemsPage(pageCoords);
    }

    public ToolDto getToolVersion(String persistentId, long versionId) {
        return super.getItemVersion(persistentId, versionId);
    }

    public ToolDto getLatestTool(String persistentId) {
        return super.getLatestItem(persistentId);
    }

    public ToolDto createTool(ToolCore toolCore) {
        return super.createItem(toolCore);
    }

    public ToolDto updateTool(String persistentId, ToolCore toolCore) {
        return super.updateItem(persistentId, toolCore);
    }

    public void deleteTool(String persistentId) {
        super.deleteItem(persistentId);
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
    protected Tool makeVersionCopy(Tool item) {
        // TODO implement
        throw new UnsupportedOperationException("Tool or Service version lift is not supported yet");
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
