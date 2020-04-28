package eu.sshopencloud.marketplace.validators.workflows;

import eu.sshopencloud.marketplace.dto.workflows.WorkflowCore;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.repositories.workflows.WorkflowRepository;
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
public class WorkflowValidator {

    private final WorkflowRepository workflowRepository;

    private final ItemValidator itemValidator;


    public Workflow validate(WorkflowCore workflowCore, Long workflowId) throws ValidationException {
        Workflow workflow = getOrCreateWorkflow(workflowId);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(workflowCore, "Workflow");

        itemValidator.validate(workflowCore, ItemCategory.WORKFLOW, workflow, errors);

        if (workflowCore.getPrevVersionId() != null) {
            if (workflowId != null && workflow.getId().equals(workflowCore.getPrevVersionId())) {
                errors.rejectValue("prevVersionId", "field.cycle", "Previous workflow cannot be the same as the current one.");
            }
            Optional<Workflow> prevVersionHolder = workflowRepository.findById(workflowCore.getPrevVersionId());
            if (!prevVersionHolder.isPresent()) {
                errors.rejectValue("prevVersionId", "field.notExist", "Previous workflow does not exist.");
            } else {
                workflow.setNewPrevVersion(prevVersionHolder.get());
            }
        }

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return workflow;
        }
    }

    private Workflow getOrCreateWorkflow(Long workflowId) {
        if (workflowId != null) {
            return workflowRepository.getOne(workflowId);
        } else {
            return new Workflow();
        }
    }

}
