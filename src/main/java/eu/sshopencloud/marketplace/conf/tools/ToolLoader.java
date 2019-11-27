package eu.sshopencloud.marketplace.conf.tools;

import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.repositories.tools.ToolRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolLoader {

    private final ToolRepository toolRepository;

    private  final IndexService indexService;

    public void createTools(List<Tool> newTools) {
        for (Tool newTool: newTools) {
            Tool tool = toolRepository.save(newTool);
            indexService.indexItem(tool);
        }
    }

}
