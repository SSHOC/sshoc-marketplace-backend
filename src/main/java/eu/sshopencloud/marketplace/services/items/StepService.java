package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PaginatedResult;
import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.dto.workflows.StepDto;
import eu.sshopencloud.marketplace.mappers.workflows.StepMapper;
import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.model.workflows.StepsTree;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.repositories.items.DraftItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.repositories.items.workflow.StepRepository;
import eu.sshopencloud.marketplace.repositories.items.workflow.StepsTreeRepository;
import eu.sshopencloud.marketplace.services.auth.UserService;
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
                       DraftItemRepository draftItemRepository, ItemRelatedItemService itemRelatedItemService,
                       PropertyTypeService propertyTypeService, IndexService indexService, UserService userService) {

        super(
                itemRepository, versionedItemRepository, draftItemRepository,
                itemRelatedItemService, propertyTypeService, indexService, userService
        );

        this.stepRepository = stepRepository;
        this.stepsTreeRepository = stepsTreeRepository;
        this.stepFactory = stepFactory;
        this.workflowService = workflowService;
    }


    public StepDto getLatestStep(String workflowId, String stepId, boolean draft) {
        validateWorkflowAndStepConsistency(workflowId, stepId, true, draft);
        return getLatestItem(stepId, draft);
    }

    public StepDto getStepVersion(String workflowId, String stepId, long stepVersionId) {
        validateWorkflowAndStepConsistency(workflowId, stepId, stepVersionId);
        return getItemVersion(stepId, stepVersionId);
    }

    public StepDto createStep(String workflowId, StepCore stepCore, boolean draft) {
        Workflow newWorkflow = workflowService.liftWorkflowForNewStep(workflowId, draft);
        WorkflowStepCore workflowStepCore = new WorkflowStepCore(stepCore, newWorkflow.getStepsTree());

        Step step = createItem(workflowStepCore, draft);
        addStepToTree(step, stepCore.getStepNo(), newWorkflow.getStepsTree());

        return prepareItemDto(step);
    }

    public StepDto createSubStep(String workflowId, String stepId, StepCore substepCore, boolean draft) {
        validateWorkflowAndStepConsistency(workflowId, stepId, false, draft);

        Workflow newWorkflow = workflowService.liftWorkflowForNewStep(workflowId, draft);
        StepsTree stepTree = loadStepTreeInWorkflow(newWorkflow, stepId);
//        Step step = loadItemForCurrentUser(stepId);
//        StepsTree stepTree = stepsTreeRepository.findByWorkflowIdAndStepId(newWorkflow.getId(), step.getId()).get();

        WorkflowStepCore workflowStepCore = new WorkflowStepCore(substepCore, stepTree);

        Step subStep = createItem(workflowStepCore, draft);
        addStepToTree(subStep, substepCore.getStepNo(), stepTree);

        return prepareItemDto(subStep);
    }

    public StepDto updateStep(String workflowId, String stepId, StepCore updatedStepCore, boolean draft) {
        validateWorkflowAndStepConsistency(workflowId, stepId, false, draft);

        Workflow newWorkflow = workflowService.liftWorkflowForNewStep(workflowId, draft);
        StepsTree stepTree = loadStepTreeInWorkflow(newWorkflow, stepId);
//        Step step = loadItemForCurrentUser(stepId);
//        StepsTree stepTree = stepsTreeRepository.findByWorkflowIdAndStepId(newWorkflow.getId(), step.getId()).get();
        StepsTree parentStepTree = stepTree.getParent();

        WorkflowStepCore workflowStepCore = new WorkflowStepCore(updatedStepCore, parentStepTree);

        Step updatedStep = updateItem(stepId, workflowStepCore, draft);
        addStepToTree(updatedStep, updatedStepCore.getStepNo(), parentStepTree);

        return prepareItemDto(updatedStep);
    }

    private void addStepToTree(Step step, Integer stepNo, StepsTree parentStepsTree) {
        if (stepNo == null) {
            parentStepsTree.appendStep(step);
        }
        else {
            parentStepsTree.addStep(step, stepNo);
        }
    }

    public StepDto revertStep(String workflowId, String stepId, long versionId) {
        validateWorkflowAndStepConsistency(workflowId, stepId, false, false);
        validateWorkflowAndStepConsistency(workflowId, stepId, versionId);

        Workflow newWorkflow = workflowService.liftWorkflowForNewStep(workflowId, false);
        Step step = loadItemVersion(stepId, versionId);
        StepsTree stepTree = stepsTreeRepository.findByWorkflowIdAndStepId(newWorkflow.getId(), step.getId()).get();

        Step revStep = revertItemVersion(stepId, versionId);
        stepTree.setStep(revStep);

        return prepareItemDto(revStep);
    }

    public void deleteStep(String workflowId, String stepId, boolean draft) {
        validateWorkflowAndStepConsistency(workflowId, stepId, false, draft);

        Workflow newWorkflow = workflowService.liftWorkflowForNewStep(workflowId, draft);
        StepsTree stepTree = loadStepTreeInWorkflow(newWorkflow, stepId);
        Step step = stepTree.getStep();
//        Step step = draft ? loadItemDraftForCurrentUser(stepId) : loadCurrentItem(stepId);
//        StepsTree stepTree = stepsTreeRepository.findByWorkflowIdAndStepId(newWorkflow.getId(), step.getId()).get();
        StepsTree parentStepTree = stepTree.getParent();

        parentStepTree.removeStep(step);

        // A draft workflow can contain non draft steps if they were derived from base non-draft version
        // In a draft workflow we remove the part of the tree associated with the step only (if the step is not a draft)
        // If the step is a draft it should be physically deleted
        if (!draft || step.isDraft())
            deleteItem(stepId, draft);
    }

    @Override
    protected Step liftItemVersion(String persistentId, boolean draft, boolean modifyStatus) {
        Step step = draft ? loadItemForCurrentUser(persistentId) : loadCurrentItem(persistentId);
        StepsTree stepTree = stepsTreeRepository.findByStepId(step.getId());
        Workflow workflow = stepTree.getWorkflow();

        Workflow newWorkflow = workflowService.liftItemVersion(workflow.getVersionedItem().getPersistentId(), draft, modifyStatus);
        Step newStep = super.liftItemVersion(persistentId, draft, modifyStatus);
        StepsTree newStepTree = stepsTreeRepository.findByWorkflowIdAndStepId(newWorkflow.getId(), step.getId()).get();

        newStepTree.setStep(newStep);

        return newStep;
    }

    Step commitDraftStep(Step step) {
        return commitItemDraft(step);
    }

    void deleteStepOnly(Step step, boolean draft) {
        deleteItem(step.getVersionedItem().getPersistentId(), draft);
    }


    private StepsTree loadStepTreeInWorkflow(Workflow workflow, String stepId) {
        return stepsTreeRepository.findByWorkflowIdAndStepPersistentId(workflow.getId(), stepId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format(
                                        "Unable to find %s with id %s in workflow with id %s (version %d)",
                                        getItemTypeName(), stepId, workflow.getPersistentId(), workflow.getId()
                                )
                        )
                );
    }

    private void validateDraftWorkflowAndStepConsistency(String workflowId, String stepId) {
        Workflow draftWorkflow = workflowService.loadItemDraftForCurrentUser(workflowId);

        stepsTreeRepository.findByWorkflowIdAndStepPersistentId(draftWorkflow.getId(), stepId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format(
                                        "Unable to find draft %s with id %s in draft workflow %s",
                                        getItemTypeName(), stepId, workflowId
                                )
                        )
                );
    }

    private void validateWorkflowAndStepConsistency(String workflowId, String stepId, boolean lastApproved, boolean draft) {
        if (draft) {
            validateDraftWorkflowAndStepConsistency(workflowId, stepId);
            return;
        }

        Workflow workflow =
                lastApproved ? workflowService.loadLatestItem(workflowId) : workflowService.loadItemForCurrentUser(workflowId);

        Step step = lastApproved ? loadLatestItem(stepId) : loadItemForCurrentUser(stepId);

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
    protected Step modifyItem(WorkflowStepCore workflowStepCore, Step step) {
        StepCore stepCore = workflowStepCore.getStepCore();
        StepsTree parentTree = workflowStepCore.getParentTree();

        return stepFactory.modify(stepCore, step, parentTree);
    }

    @Override
    protected Step makeItemCopy(Step step) {
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
