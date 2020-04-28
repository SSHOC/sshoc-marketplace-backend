package eu.sshopencloud.marketplace.services.workflows;

import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.dto.workflows.StepDto;
import eu.sshopencloud.marketplace.mappers.workflows.StepMapper;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.repositories.workflows.StepRepository;
import eu.sshopencloud.marketplace.repositories.workflows.WorkflowRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.validators.workflows.StepValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StepService {

    private final StepRepository stepRepository;

    private final WorkflowRepository workflowRepository;

    private final StepValidator stepValidator;

    private final ItemRelatedItemService itemRelatedItemService;

    private final ItemService itemService;


    public StepDto getStep(long workflowId, long stepId) {
        Step step = stepRepository.findById(stepId).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Step.class.getName() + " with id " + stepId));
        return completeStep(StepMapper.INSTANCE.toDto(step));
    }

    private StepDto completeStep(StepDto step) {
        itemService.completeItem(step);
        return step;
    }

    public StepDto createStep(long workflowId, StepCore stepCore) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Workflow.class.getName() + " with id " + workflowId));
        Step step = stepValidator.validate(stepCore, null);
        step.setWorkflow(workflow);
        step.setLastInfoUpdate(ZonedDateTime.now());

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        itemService.addInformationContributorToItem(step, LoggedInUserHolder.getLoggedInUser());

        Item nextVersion = itemService.clearVersionForCreate(step);

        int size = 0;
        List<Step> steps = new ArrayList();
        if (workflow.getSteps() != null) {
            size = workflow.getSteps().size();
            steps = workflow.getSteps();
        } else {
            workflow.setSteps(steps);
        }
        steps.add(step);
        workflow = workflowRepository.save(workflow);
        step = workflow.getSteps().get(size);

        itemService.switchVersion(step, nextVersion);

        return completeStep(StepMapper.INSTANCE.toDto(step));
    }

    public StepDto createStep(long workflowId, long stepId, StepCore substepCore) {
        Step step = checkWorkflowAndStepConsistency(workflowId, stepId);

        Step substep = stepValidator.validate(substepCore, null);
        substep.setStep(step);
        substep.setLastInfoUpdate(ZonedDateTime.now());

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        itemService.addInformationContributorToItem(substep, LoggedInUserHolder.getLoggedInUser());

        Workflow workflow = workflowRepository.getOne(workflowId);
        substep.setWorkflow(workflow);

        Item nextVersion = itemService.clearVersionForCreate(substep);

        int size = 0;
        List<Step> substeps = new ArrayList();
        if (step.getSubsteps() != null) {
            size = step.getSubsteps().size();
            substeps = step.getSubsteps();
        } else {
            step.setSubsteps(substeps);
        }
        substeps.add(substep);
        step = stepRepository.save(step);
        substep = step.getSubsteps().get(size);

        itemService.switchVersion(substep, nextVersion);

        return completeStep(StepMapper.INSTANCE.toDto(substep));
    }

    public StepDto updateStep(long workflowId, long stepId, StepCore updatedStep) {
        checkWorkflowAndStepConsistency(workflowId, stepId);

        Step step = stepValidator.validate(updatedStep, stepId);
        step.setLastInfoUpdate(ZonedDateTime.now());

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        itemService.addInformationContributorToItem(step, LoggedInUserHolder.getLoggedInUser());

        Item prevVersion = step.getPrevVersion();
        Item nextVersion = itemService.clearVersionForUpdate(step);
        step = stepRepository.save(step);
        itemService.switchVersion(prevVersion, nextVersion);

        return completeStep(StepMapper.INSTANCE.toDto(step));
    }

    public void deleteStep(long workflowId, long stepId) {
        Step step = checkWorkflowAndStepConsistency(workflowId, stepId);
        itemRelatedItemService.deleteRelationsForItem(step);
        Item prevVersion = step.getPrevVersion();
        Item nextVersion = itemService.clearVersionForDelete(step);
        stepRepository.delete(step);
        itemService.switchVersion(prevVersion, nextVersion);
    }

    private Step checkWorkflowAndStepConsistency(long workflowId, long stepId) {
        // TODO don't allow deleting without authentication (in WebSecurityConfig)
        if (!workflowRepository.existsById(workflowId)) {
            throw new EntityNotFoundException("Unable to find " + Workflow.class.getName() + " with id " + workflowId);
        }
        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Step.class.getName() + " with id " + stepId));
        Step s = step;
        while (s.getWorkflow() == null) {
            s = s.getStep();
        }
        if (s.getWorkflow().getId() != workflowId) {
            throw new EntityNotFoundException("Unable to find " + Step.class.getName() + " with id " + stepId + " in workflow " + workflowId );
        }
        return step;
    }

}
