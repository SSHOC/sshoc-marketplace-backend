package eu.sshopencloud.marketplace.validators.tools;

import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.repositories.items.ToolRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.items.ItemFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;


@Component
@RequiredArgsConstructor
@Slf4j
public class ToolFactory {

    private final ItemFactory itemFactory;


    public Tool create(ToolCore toolCore, Tool prevTool) throws ValidationException {
        Tool tool = (prevTool != null) ? new Tool(prevTool) : new Tool();
        return setToolValues(toolCore, tool);
    }

    public Tool modify(ToolCore toolCore, Tool tool) throws ValidationException {
        return setToolValues(toolCore, tool);
    }

    private Tool setToolValues(ToolCore toolCore, Tool tool) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(toolCore, "Tool");
        tool = itemFactory.initializeItem(toolCore, tool, ItemCategory.TOOL_OR_SERVICE, errors);

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return tool;
    }

    public Tool makeNewVersion(Tool tool) {
        Tool newTool = new Tool(tool);
        return itemFactory.initializeNewVersion(newTool);
    }
}
