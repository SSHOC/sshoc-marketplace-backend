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
        Step step = checkWorkflowAndStepConsistency(workflowId, stepId);
        return completeStep(StepMapper.INSTANCE.toDto(step));
    }

    private StepDto completeStep(StepDto step) {
        itemService.completeItem(step);
        return step;
    }

    public StepDto createStep(long workflowId, StepCore stepCore) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Workflow.class.getName() + " with id " + workflowId));
        int numberOfSiblings = 0;
        List<Step> steps = new ArrayList<>();
        if (workflow.getSteps() != null) {
            numberOfSiblings = workflow.getSteps().size();
            steps = workflow.getSteps();
        } else {
            workflow.setSteps(steps);
        }

        Step step = stepValidator.validate(stepCore, null, numberOfSiblings);
        step.setWorkflow(workflow);
        itemService.updateInfoDates(step);

        itemService.addInformationContributorToItem(step, LoggedInUserHolder.getLoggedInUser());

        Item nextVersion = itemService.clearVersionForCreate(step);
        step = stepRepository.save(step);

        if (stepCore.getStepNo() == null) {
            steps.add(step);
        } else {
            steps.add(stepCore.getStepNo() - 1, step);
        }
        workflowRepository.save(workflow);

        itemService.switchVersion(step, nextVersion);

        return completeStep(StepMapper.INSTANCE.toDto(step));
    }

    public StepDto createSubstep(long workflowId, long stepId, StepCore substepCore) {
        Step step = checkWorkflowAndStepConsistency(workflowId, stepId);
        int numberOfSiblings = 0;
        List<Step> substeps = new ArrayList<>();
        if (step.getSubsteps() != null) {
            numberOfSiblings = step.getSubsteps().size();
            substeps = step.getSubsteps();
        } else {
            step.setSubsteps(substeps);
        }

        Step substep = stepValidator.validate(substepCore, null, numberOfSiblings);
        substep.setStep(step);
        itemService.updateInfoDates(substep);

        itemService.addInformationContributorToItem(substep, LoggedInUserHolder.getLoggedInUser());

        Item nextVersion = itemService.clearVersionForCreate(substep);
        substep = stepRepository.save(substep);

        if (substepCore.getStepNo() == null) {
            substeps.add(substep);
        } else {
            substeps.add(substepCore.getStepNo() - 1, substep);
        }
        stepRepository.save(step);

        itemService.switchVersion(substep, nextVersion);

        return completeStep(StepMapper.INSTANCE.toDto(substep));
    }

    public StepDto updateStep(long workflowId, long stepId, StepCore updatedStep) {
        Step step = checkWorkflowAndStepConsistency(workflowId, stepId);
        boolean isSubstep = (step.getWorkflow() == null);
        List<Step> ssteps;
        if (!isSubstep) {
            ssteps = step.getWorkflow().getSteps();
        } else {
            ssteps = step.getStep().getSubsteps();
        }
        int numberOfSiblings = ssteps.size();

        step = stepValidator.validate(updatedStep, stepId, numberOfSiblings);
        itemService.updateInfoDates(step);

        itemService.addInformationContributorToItem(step, LoggedInUserHolder.getLoggedInUser());

        Item prevVersion = step.getPrevVersion();
        Item nextVersion = itemService.clearVersionForUpdate(step);
        step = stepRepository.save(step);

        if (updatedStep.getStepNo() != null) {
            int idx = -1;
            for (int i = 0; i < numberOfSiblings; i++) {
                if (ssteps.get(i).getId().equals(step.getId())) {
                    idx = i;
                    break;
                }
            }
            ssteps.remove(idx);
            ssteps.add(updatedStep.getStepNo() - 1, step);
            if (!isSubstep) {
                workflowRepository.save(step.getWorkflow());
            } else {
                stepRepository.save(step.getStep());
            }
        }

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
