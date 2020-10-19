package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.dto.workflows.StepDto;
import eu.sshopencloud.marketplace.mappers.workflows.StepMapper;
import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.model.workflows.StepsTree;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.repositories.items.workflow.StepRepository;
import eu.sshopencloud.marketplace.repositories.items.workflow.StepsTreeRepository;
import eu.sshopencloud.marketplace.repositories.items.workflow.WorkflowRepository;
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
    private final StepsTreeRepository stepsTreeRepository;
    private final WorkflowRepository workflowRepository;
    private final StepFactory stepFactory;
    private final ItemCrudService itemService;


    public StepDto getStep(long workflowId, long stepId) {
        validateWorkflowAndStepConsistency(workflowId, stepId);
        Step step = stepRepository.getOne(stepId);
        return convertStep(step);
    }

    StepDto convertStep(Step step) {
        StepDto dto = StepMapper.INSTANCE.toDto(step);
        itemService.completeItem(dto);

        return dto;
    }

    public StepDto createStep(long workflowId, StepCore stepCore) {
        Workflow workflow = loadWorkflow(workflowId);
        Workflow newWorkflow = Workflow.fromWorkflowSteps(workflow);
        newWorkflow = workflowRepository.save(newWorkflow);

        return createNewStep(newWorkflow.getStepsTree(), stepCore);
    }

    public StepDto createSubstep(long workflowId, long stepId, StepCore substepCore) {
        validateWorkflowAndStepConsistency(workflowId, stepId);

        Workflow workflow = loadWorkflow(workflowId);
        Workflow newWorkflow = Workflow.fromWorkflowSteps(workflow);
        newWorkflow = workflowRepository.save(newWorkflow);

        StepsTree stepTree = stepsTreeRepository.findByWorkflowIdAndStepId(newWorkflow.getId(), stepId).get();

        return createNewStep(stepTree, substepCore);
    }

    private StepDto createNewStep(StepsTree stepsTree, StepCore stepCore) {
        Step step = stepFactory.create(stepCore, null, stepsTree);
        step = stepRepository.save(step);

        return convertStep(step);
    }

    public StepDto updateStep(long workflowId, long stepId, StepCore updatedStep) {
        validateWorkflowAndStepConsistency(workflowId, stepId);

        Workflow workflow = loadWorkflow(workflowId);
        Workflow newWorkflow = Workflow.fromWorkflowSteps(workflow);
        newWorkflow = workflowRepository.save(newWorkflow);

        StepsTree stepTree = stepsTreeRepository.findByWorkflowIdAndStepId(newWorkflow.getId(), stepId).get();
        StepsTree parentStepTree = stepTree.getParent();

        Step prevStep = stepRepository.getOne(stepId);
        Step step = stepFactory.create(updatedStep, prevStep, parentStepTree);
        step = stepRepository.save(step);

        return convertStep(step);
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

        stepsTreeRepository.findByWorkflowIdAndStepId(workflowId, stepId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format("Unable to find %s with id %d in workflow %d", Step.class.getName(), stepId, workflowId)
                        )
                );
    }

    private Workflow loadWorkflow(long workflowId) {
        return workflowRepository.findById(workflowId).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Workflow.class.getName() + " with id " + workflowId)
        );
    }
}
