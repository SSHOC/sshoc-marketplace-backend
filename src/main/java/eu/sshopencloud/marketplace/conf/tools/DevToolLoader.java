package eu.sshopencloud.marketplace.conf.tools;

import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.repositories.tools.ToolRepository;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DevToolLoader {

    private final ToolRepository toolRepository;

    private final ItemService itemService;

    private  final SearchService searchService;

    public void createTools(List<? extends Tool> newTools) {
        for (Tool newTool: newTools) {
            Tool tool = toolRepository.save(newTool);
            if (itemService.isNewestVersion(tool)) {
                if (tool.getPrevVersion() != null) {
                    searchService.removeItem(tool.getPrevVersion());
                }
                searchService.indexItem(tool);
            }
        }
    }

}
