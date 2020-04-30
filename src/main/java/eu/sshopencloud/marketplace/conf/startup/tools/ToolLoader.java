package eu.sshopencloud.marketplace.conf.startup.tools;

import eu.sshopencloud.marketplace.conf.startup.items.ItemLoader;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.repositories.tools.ToolRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolLoader {

    private final ItemLoader itemLoader;

    private final ToolRepository toolRepository;

    private  final IndexService indexService;

    public void createTools(String profile, List<Tool> newTools) {
        for (Tool newTool: newTools) {
            itemLoader.completeItemRelations(newTool);
            Tool tool = toolRepository.save(newTool);
            if (!profile.equals("prod")) {
                indexService.indexItem(tool);
            }
        }
    }

}
