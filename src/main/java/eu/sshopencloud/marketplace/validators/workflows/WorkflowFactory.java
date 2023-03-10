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


    public Workflow create(WorkflowCore workflowCore, Workflow prevWorkflow, boolean conflict) throws ValidationException {
        Workflow workflow = (prevWorkflow != null) ? new Workflow(prevWorkflow) : new Workflow();
        return setWorkflowValues(workflowCore, workflow, conflict);
    }

    public Workflow modify(WorkflowCore workflowCore, Workflow workflow) throws ValidationException {
        return setWorkflowValues(workflowCore, workflow, false);
    }

    private Workflow setWorkflowValues(WorkflowCore workflowCore, Workflow workflow, boolean conflict) throws ValidationException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(workflowCore, "Workflow");

        workflow = itemFactory.initializeItem(workflowCore, workflow, conflict, ItemCategory.WORKFLOW, errors);

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return workflow;
    }

    public Workflow makeNewVersion(Workflow workflow) {
        Workflow newWorkflow = new Workflow(workflow);
        return itemFactory.initializeNewVersion(newWorkflow);
    }
}
