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

    public StepDto getStepVersion(String workflowId, String stepId, long stepVersionId) {
        validateWorkflowAndStepConsistency(workflowId, stepId, stepVersionId);
        return getItemVersion(stepId, stepVersionId);
    }

    public StepDto createStep(String workflowId, StepCore stepCore) {
        Workflow newWorkflow = workflowService.liftWorkflowVersion(workflowId);
        WorkflowStepCore workflowStepCore = new WorkflowStepCore(stepCore, newWorkflow.getStepsTree());

        Step step = super.createItem(workflowStepCore);
        return prepareItemDto(step);
    }


    public StepDto createSubstep(String workflowId, String stepId, StepCore substepCore) {
        validateWorkflowAndStepConsistency(workflowId, stepId);

        Workflow newWorkflow = workflowService.liftWorkflowVersion(workflowId);
        Step step = super.loadLatestItem(stepId);
        StepsTree stepTree = stepsTreeRepository.findByWorkflowIdAndStepId(newWorkflow.getId(), step.getId()).get();

        WorkflowStepCore workflowStepCore = new WorkflowStepCore(substepCore, stepTree);

        Step subStep = super.createItem(workflowStepCore);
        return prepareItemDto(subStep);
    }

    public StepDto updateStep(String workflowId, String stepId, StepCore updatedStepCore) {
        validateWorkflowAndStepConsistency(workflowId, stepId);

        Workflow newWorkflow = workflowService.liftWorkflowVersion(workflowId);
        Step step = super.loadLatestItem(stepId);
        StepsTree stepTree = stepsTreeRepository.findByWorkflowIdAndStepId(newWorkflow.getId(), step.getId()).get();
        StepsTree parentStepTree = stepTree.getParent();

        WorkflowStepCore workflowStepCore = new WorkflowStepCore(updatedStepCore, parentStepTree);

        Step updatedStep = super.updateItem(stepId, workflowStepCore);
        return prepareItemDto(updatedStep);
    }

    public StepDto revertStep(String workflowId, String stepId, long versionId) {
        validateWorkflowAndStepConsistency(workflowId, stepId, versionId);

        Workflow newWorkflow = workflowService.liftWorkflowVersion(workflowId);
        Step step = super.loadItemVersion(stepId, versionId);
        StepsTree stepTree = stepsTreeRepository.findByWorkflowIdAndStepId(newWorkflow.getId(), step.getId()).get();

        Step revStep = super.revertItemVersion(stepId, versionId);
        stepTree.setStep(revStep);

        return super.prepareItemDto(revStep);
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
        Workflow workflow = workflowService.loadLatestItem(workflowId);
        Step step = super.loadLatestItem(stepId);

        stepsTreeRepository.findByWorkflowIdAndStepId(workflow.getId(), step.getId())
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format(
                                        "Unable to find %s with id %s in workflow %s",
                                        getItemTypeName(), stepId, workflowId
                                )
                        )
                );
    }

    private void validateWorkflowAndStepConsistency(String workflowId, String stepId, long stepVersionId) {
        Workflow workflow = workflowService.loadLatestItem(workflowId);

        stepsTreeRepository.findByWorkflowIdAndStepId(workflow.getId(), stepVersionId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format(
                                        "Unable to find %s with id %s (version %d) in workflow %s",
                                        getItemTypeName(), stepId, stepVersionId, workflowId
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

        return stepFactory.create(stepCore, prevStep, parentTree);
    }

    @Override
    protected Step makeVersionCopy(Step step) {
        return stepFactory.makeNewVersion(step);
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
