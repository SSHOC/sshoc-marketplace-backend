package eu.sshopencloud.marketplace.services.tools;

import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.repositories.tools.ToolRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolService {

    private final ToolRepository toolRepository;

    private final ItemService itemService;

    private final ItemRelatedItemService itemRelatedItemService;

    private  final SearchService searchService;

    public PaginatedTools getTools(int page, int perpage) {
        Page<Tool> tools = toolRepository.findAll(PageRequest.of(page - 1, perpage, new Sort(Sort.Direction.ASC, "label")));
        for (Tool tool: tools) {
            tool.setRelatedItems(itemRelatedItemService.getItemRelatedItems(tool.getId()));
            tool.setOlderVersions(itemService.getOlderVersionsOfItem(tool));
            tool.setNewerVersions(itemService.getNewerVersionsOfItem(tool));
            itemService.fillAllowedVocabulariesForPropertyTypes(tool);
        }
        return new PaginatedTools(tools, page, perpage);
    }

    public Tool getTool(Long id) {
        Tool tool = toolRepository.getOne(id);
        tool.setRelatedItems(itemRelatedItemService.getItemRelatedItems(id));
        tool.setOlderVersions(itemService.getOlderVersionsOfItem(tool));
        tool.setNewerVersions(itemService.getNewerVersionsOfItem(tool));
        itemService.fillAllowedVocabulariesForPropertyTypes(tool);
        return tool;
    }

    public void createTools(List<? extends Tool> newTools) {
        for (Tool newTool: newTools) {
            createTool(newTool);
        }
    }

    public Tool createTool(Tool newTool) {
        // TODO set previous version by older and newer versions
        newTool.setId(null);
        Tool tool = toolRepository.save(newTool);
        if (itemService.isNewestVersion(newTool)) {
            if (newTool.getPrevVersion() != null) {
                searchService.removeItem(newTool.getPrevVersion());
            }
            searchService.indexItem(newTool);
        }
        return tool;
    }

    public Tool updateTool(Long id, Tool newTool) {
        // TODO check ID
        newTool.setId(id);
        Tool tool = toolRepository.save(newTool);
        // TODO index in SOLR
        return tool;
    }

    public void deleteTool(Long id) {
        Tool tool = toolRepository.getOne(id);
        itemRelatedItemService.deleteRelationsForItem(tool);
        itemService.switchVersionForDelete(tool);
        toolRepository.delete(tool);
    }

}
