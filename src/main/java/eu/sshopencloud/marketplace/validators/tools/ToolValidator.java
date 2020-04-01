package eu.sshopencloud.marketplace.validators.tools;

import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.repositories.tools.ToolRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.items.ItemValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ToolValidator {

    private final ToolRepository toolRepository;

    private final ItemValidator itemValidator;


    public Tool validate(ToolCore toolCore, Long toolId) throws ValidationException {
        Tool tool = getOrCreateTool(toolId);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(toolCore, "Tool");

        itemValidator.validate(toolCore, ItemCategory.TOOL, tool, errors);

        tool.setRepository(toolCore.getRepository());

        if (toolCore.getPrevVersionId() != null) {
            if (toolId != null && tool.getId().equals(toolCore.getPrevVersionId())) {
                errors.rejectValue("prevVersionId", "field.cycle", "Previous tool cannot be the same as the current one.");
            }
            Optional<Tool> prevVersionHolder = toolRepository.findById(toolCore.getPrevVersionId());
            if (!prevVersionHolder.isPresent()) {
                errors.rejectValue("prevVersionId", "field.notExist", "Previous tool does not exist.");
            } else {
                tool.setNewPrevVersion(prevVersionHolder.get());
            }
        }

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return tool;
        }
    }


    private Tool getOrCreateTool(Long toolId) {
        if (toolId != null) {
            return toolRepository.getOne(toolId);
        } else {
            return new Tool();
        }
    }

}
