package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PaginatedResult;
import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.dto.workflows.StepDto;
import eu.sshopencloud.marketplace.mappers.workflows.StepMapper;
import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.model.workflows.StepsTree;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.repositories.items.workflow.StepRepository;
import eu.sshopencloud.marketplace.repositories.items.workflow.StepsTreeRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.validators.workflows.StepFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;


@Service
@Transactional
@Slf4j
public class StepService extends ItemCrudService<Step, StepDto, PaginatedResult<StepDto>, WorkflowStepCore> {

    private final StepRepository stepRepository;
    private final StepsTreeRepository stepsTreeRepository;
    private final StepFactory stepFactory;
    private final WorkflowService workflowService;


    public StepService(StepRepository stepRepository, StepsTreeRepository stepsTreeRepository,
                       StepFactory stepFactory, WorkflowService workflowService,
                       ItemRepository itemRepository, VersionedItemRepository versionedItemRepository,
                       ItemRelatedItemService itemRelatedItemService, PropertyTypeService propertyTypeService,
                       IndexService indexService) {

        super(itemRepository, versionedItemRepository, itemRelatedItemService, propertyTypeService, indexService);

        this.stepRepository = stepRepository;
        this.stepsTreeRepository = stepsTreeRepository;
        this.stepFactory = stepFactory;
        this.workflowService = workflowService;
    }


    public StepDto getLatestStep(String workflowId, String stepId) {
        validateWorkflowAndStepConsistency(workflowId, stepId);
        return getLatestItem(stepId);
    }

    public StepDto createStep(String workflowId, StepCore stepCore) {
        Workflow newWorkflow = workflowService.liftWorkflowVersion(workflowId);
        WorkflowStepCore workflowStepCore = new WorkflowStepCore(stepCore, newWorkflow.getStepsTree());

        return super.createItem(workflowStepCore);
    }


    public StepDto createSubstep(String workflowId, String stepId, StepCore substepCore) {
        validateWorkflowAndStepConsistency(workflowId, stepId);

        Workflow newWorkflow = workflowService.liftWorkflowVersion(workflowId);
        Step step = super.loadLatestItem(stepId);
        StepsTree stepTree = stepsTreeRepository.findByWorkflowIdAndStepId(newWorkflow.getId(), step.getId()).get();

        WorkflowStepCore workflowStepCore = new WorkflowStepCore(substepCore, stepTree);

        return super.createItem(workflowStepCore);
    }

    public StepDto updateStep(String workflowId, String stepId, StepCore updatedStep) {
        validateWorkflowAndStepConsistency(workflowId, stepId);

        Workflow newWorkflow = workflowService.liftWorkflowVersion(workflowId);
        Step step = super.loadLatestItem(stepId);
        StepsTree stepTree = stepsTreeRepository.findByWorkflowIdAndStepId(newWorkflow.getId(), step.getId()).get();
        StepsTree parentStepTree = stepTree.getParent();

        WorkflowStepCore workflowStepCore = new WorkflowStepCore(updatedStep, parentStepTree);

        return super.updateItem(stepId, workflowStepCore);
    }

    public void deleteStep(String workflowId, String stepId) {
        validateWorkflowAndStepConsistency(workflowId, stepId);

        Workflow newWorkflow = workflowService.liftWorkflowVersion(workflowId);
        Step step = super.loadLatestItem(stepId);
        StepsTree stepTree = stepsTreeRepository.findByWorkflowIdAndStepId(newWorkflow.getId(), step.getId()).get();
        StepsTree parentStepTree = stepTree.getParent();

        parentStepTree.removeStep(step);

        super.deleteItem(stepId);
    }

    void deleteStepOnly(Step step) {
        super.deleteItem(step.getVersionedItem().getPersistentId());
    }

    private void validateWorkflowAndStepConsistency(String workflowId, String stepId) {
        // Throws EntityNotFoundException in case workflow is not found
        Workflow workflow = workflowService.loadLatestItem(workflowId);
        Step step = super.loadLatestItem(stepId);

        stepsTreeRepository.findByWorkflowIdAndStepId(workflow.getId(), step.getId())
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format(
                                        "Unable to find %s with id %s in workflow %s",
                                        Step.class.getName(), stepId, workflowId
                                )
                        )
                );
    }


    @Override
    protected ItemVersionRepository<Step> getItemRepository() {
        return stepRepository;
    }

    @Override
    protected Step makeItem(WorkflowStepCore workflowStepCore, Step prevStep) {
        StepCore stepCore = workflowStepCore.getStepCore();
        StepsTree parentTree = workflowStepCore.getParentTree();

        // Todo lift up the workflow's version
        return stepFactory.create(stepCore, prevStep, parentTree);
    }

    @Override
    protected Step makeVersionCopy(Step step) {
        // TODO implement
        throw new UnsupportedOperationException("Step version lift is not supported yet");
    }

    @Override
    protected PaginatedResult<StepDto> wrapPage(Page<Step> stepsPage, List<StepDto> steps) {
        throw new UnsupportedOperationException("Steps pagination is not supported");
    }

    @Override
    protected StepDto convertItemToDto(Step step) {
        return StepMapper.INSTANCE.toDto(step);
    }

    @Override
    protected String getItemTypeName() {
        return Workflow.class.getName();
    }
}
