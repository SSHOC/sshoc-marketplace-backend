package eu.sshopencloud.marketplace.services.workflows;

import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.dto.workflows.StepDto;
import eu.sshopencloud.marketplace.mappers.workflows.StepMapper;
import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.model.workflows.StepParent;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.repositories.workflows.StepRepository;
import eu.sshopencloud.marketplace.repositories.workflows.WorkflowRepository;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.validators.workflows.StepFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StepService {

    private final StepRepository stepRepository;
    private final WorkflowRepository workflowRepository;
    private final StepFactory stepFactory;
    private final ItemService itemService;


    public StepDto getStep(long workflowId, long stepId) {
        validateWorkflowAndStepConsistency(workflowId, stepId);
        Step step = stepRepository.getOne(stepId);
        return completeStep(StepMapper.INSTANCE.toDto(step));
    }

    private StepDto completeStep(StepDto step) {
        itemService.completeItem(step);
        return step;
    }

    public StepDto createStep(long workflowId, StepCore stepCore) {
        Workflow workflow = workflowRepository.findById(workflowId).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Workflow.class.getName() + " with id " + workflowId)
        );

        return createNewStep(workflow, stepCore);
    }

    public StepDto createSubstep(long workflowId, long stepId, StepCore substepCore) {
        validateWorkflowAndStepConsistency(workflowId, stepId);
        StepParent parentStep = stepRepository.getOne(stepId);

        return createNewStep(parentStep, substepCore);
    }

    private StepDto createNewStep(StepParent stepParent, StepCore stepCore) {
        Step step = stepFactory.create(stepCore, null, stepParent);
        step = stepRepository.save(step);

        if (stepCore.getStepNo() == null)
            stepParent.appendStep(step);
        else
            stepParent.addStep(step, stepCore.getStepNo() - 1);

        return completeStep(StepMapper.INSTANCE.toDto(step));
    }

    public StepDto updateStep(long workflowId, long stepId, StepCore updatedStep) {
        validateWorkflowAndStepConsistency(workflowId, stepId);
        Step prevStep = stepRepository.getOne(stepId);
        StepParent parentStep = prevStep.getStepParent();

        Step step = stepFactory.create(updatedStep, prevStep, prevStep.getStepParent());
        step = stepRepository.save(step);

        if (updatedStep.getStepNo() != null) {
            parentStep.addStep(step, updatedStep.getStepNo());
        }

        return completeStep(StepMapper.INSTANCE.toDto(step));
    }

    public void deleteStep(long workflowId, long stepId) {
        validateWorkflowAndStepConsistency(workflowId, stepId);
        Step step = stepRepository.getOne(stepId);

        itemService.cleanupItem(step);
        stepRepository.delete(step);
    }

    private void validateWorkflowAndStepConsistency(long workflowId, long stepId) {
        if (!workflowRepository.existsById(workflowId))
            throw new EntityNotFoundException("Unable to find " + Workflow.class.getName() + " with id " + workflowId);

        Step step = stepRepository.findById(stepId).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Step.class.getName() + " with id " + stepId)
        );

        if (step.getRootWorkflow().getId() != workflowId) {
            throw new EntityNotFoundException(
                    "Unable to find " + Step.class.getName() + " with id " + stepId + " in workflow " + workflowId
            );
        }
    }
}
