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

        Workflow workflow = (prevWorkflow != null) ? new Workflow(prevWorkflow) : new Workflow();
        workflow = itemFactory.initializeItem(workflowCore, workflow, ItemCategory.WORKFLOW, errors);

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return workflow;
    }

    public Workflow makeNewVersion(Workflow workflow) {
        Workflow newWorkflow = new Workflow(workflow);
        return itemFactory.initializeNewVersion(newWorkflow);
    }
}
