package eu.sshopencloud.marketplace.validators.workflows;

import eu.sshopencloud.marketplace.dto.workflows.WorkflowCore;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.items.ItemFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WorkflowFactory {

    private final ItemFactory itemFactory;


    public Workflow create(WorkflowCore workflowCore, Workflow prevWorkflow) throws ValidationException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(workflowCore, "Workflow");

        Workflow baseWorkflow = (prevWorkflow == null) ? new Workflow() : Workflow.fromWorkflowSteps(prevWorkflow);
        Workflow workflow = itemFactory.initializeItem(
                workflowCore, baseWorkflow, prevWorkflow, ItemCategory.WORKFLOW, errors
        );

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return workflow;
    }

    public Workflow makeNewVersion(Workflow baseWorkflow) {
        Workflow newWorkflow = new Workflow(baseWorkflow);
        return itemFactory.initializeNewVersion(newWorkflow, baseWorkflow);
    }
}
