package eu.sshopencloud.marketplace.services.tools;

import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.repositories.tools.ToolRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolService {

    private final ToolRepository toolRepository;

    private final ItemRelatedItemService itemRelatedItemService;

    public List<Tool> getAllTools() {
        List<Tool> tools = toolRepository.findAll(new Sort(Sort.Direction.ASC, "label"));
        for (Tool tool: tools) {
            tool.setRelatedItems(itemRelatedItemService.getItemRelatedItems(tool.getId()));
        }
        return tools;
    }

    public Tool getTool(Long id) {
        Tool tool = toolRepository.getOne(id);
        tool.setRelatedItems(itemRelatedItemService.getItemRelatedItems(id));
        return tool;
    }

}
