package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.workflows.*;
import eu.sshopencloud.marketplace.mappers.workflows.WorkflowMapper;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.model.workflows.StepsTree;
import eu.sshopencloud.marketplace.model.workflows.StepsTreeVisitor;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.repositories.items.DraftItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.repositories.items.workflow.WorkflowRepository;
import eu.sshopencloud.marketplace.services.auth.UserService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.validators.workflows.WorkflowFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;


@Service
@Transactional
@Slf4j
public class WorkflowService extends ItemCrudService<Workflow, WorkflowDto, PaginatedWorkflows, WorkflowCore> {

    private final WorkflowRepository workflowRepository;
    private final WorkflowFactory workflowFactory;
    private final StepService stepService;


    public WorkflowService(WorkflowRepository workflowRepository, WorkflowFactory workflowFactory, @Lazy StepService stepService,
                           ItemRepository itemRepository, VersionedItemRepository versionedItemRepository,
                           DraftItemRepository draftItemRepository, ItemRelatedItemService itemRelatedItemService,
                           PropertyTypeService propertyTypeService, IndexService indexService, UserService userService) {

        super(
                itemRepository, versionedItemRepository, draftItemRepository,
                itemRelatedItemService, propertyTypeService, indexService, userService
        );

        this.workflowRepository = workflowRepository;
        this.workflowFactory = workflowFactory;
        this.stepService = stepService;
    }


    public PaginatedWorkflows getWorkflows(PageCoords pageCoords) {
        return super.getItemsPage(pageCoords);
    }

    public WorkflowDto getLatestWorkflow(String persistentId, boolean draft) {
        return super.getLatestItem(persistentId, draft);
    }

    public WorkflowDto getWorkflowVersion(String persistentId, long versionId) {
        return super.getItemVersion(persistentId, versionId);
    }

    private void collectSteps(WorkflowDto dto, Workflow workflow) {
        StepsTree tree = workflow.gatherSteps();
        Stack<StepDto> nestedSteps = new Stack<>();
        List<StepDto> rootSteps = new ArrayList<>();

        tree.visit(new StepsTreeVisitor() {
            @Override
            public void onNextStep(StepsTree stepTree) {
                Step step = stepTree.getStep();
                StepDto stepDto = stepService.convertItemToDto(step);
                List<StepDto> childCollection = (nestedSteps.empty()) ? rootSteps : nestedSteps.peek().getComposedOf();

                childCollection.add(stepDto);
                nestedSteps.push(stepDto);
            }

            @Override
            public void onBackToParent() {
                nestedSteps.pop();
            }
        });

        dto.setComposedOf(rootSteps);
    }

    public WorkflowDto createWorkflow(WorkflowCore workflowCore, boolean draft) {
        Workflow workflow = createItem(workflowCore, draft);

        if (!draft)
            commitSteps(workflow.getStepsTree());

        return prepareItemDto(workflow);
    }

    public WorkflowDto updateWorkflow(String persistentId, WorkflowCore workflowCore, boolean draft) {
        Workflow workflow = updateItem(persistentId, workflowCore, draft);

        if (!draft)
            commitSteps(workflow.getStepsTree());

        return prepareItemDto(workflow);
    }

    private void commitSteps(StepsTree stepsTree) {
        stepsTree.visit(new StepsTreeVisitor() {
            @Override
            public void onNextStep(StepsTree stepTree) {
                Step step = stepTree.getStep();

                if (step.getStatus().equals(ItemStatus.DRAFT)) {
                    step = stepService.commitDraftStep(step);
                    stepTree.setStep(step);
                }
            }

            @Override
            public void onBackToParent() {
            }
        });
    }

    public WorkflowDto revertWorkflow(String persistentId, long versionId) {
        Workflow revWorkflow = revertItemVersion(persistentId, versionId);
        return prepareItemDto(revWorkflow);
    }

    public void deleteWorkflow(String persistentId) {
        Workflow workflow = super.loadLatestItem(persistentId);
        workflow.getStepsTree().visit(new StepsTreeVisitor() {
            @Override
            public void onNextStep(StepsTree stepTree) {
                Step step = stepTree.getStep();
                stepService.deleteStepOnly(step);
            }

            @Override
            public void onBackToParent() {}
        });

        super.deleteItem(persistentId);
    }

    Workflow resolveWorkflowForNewStep(String persistentId, boolean draft) {
        Optional<Workflow> workflowDraft = loadItemDraftForCurrentUser(persistentId);

        if (!draft && workflowDraft.isPresent()) {
            throw new IllegalArgumentException(
                    String.format(
                            "%s with id %s is in draft state for current user, so a non-draft step cannot be added",
                            getItemTypeName(), persistentId
                    )
            );
        }

        if (draft && workflowDraft.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format(
                            "No draft %s with id %s is found for current user, so a draft step cannot be added",
                            getItemTypeName(), persistentId
                    )
            );
        }

        return super.liftItemVersion(persistentId, draft);
    }


    @Override
    protected ItemVersionRepository<Workflow> getItemRepository() {
        return workflowRepository;
    }

    @Override
    protected Workflow makeItem(WorkflowCore workflowCore, Workflow prevWorkflow) {
        return workflowFactory.create(workflowCore, prevWorkflow);
    }

    @Override
    protected Workflow modifyItem(WorkflowCore workflowCore, Workflow workflow) {
        return workflowFactory.modify(workflowCore, workflow);
    }

    @Override
    protected Workflow makeVersionCopy(Workflow workflow) {
        return workflowFactory.makeNewVersion(workflow);
    }

    @Override
    protected PaginatedWorkflows wrapPage(Page<Workflow> workflowsPage, List<WorkflowDto> workflows) {
        return PaginatedWorkflows.builder()
                .workflows(workflows)
                .count(workflowsPage.getContent().size())
                .hits(workflowsPage.getTotalElements())
                .page(workflowsPage.getNumber())
                .perpage(workflowsPage.getSize())
                .pages(workflowsPage.getTotalPages())
                .build();
    }

    @Override
    protected WorkflowDto convertItemToDto(Workflow workflow) {
        WorkflowDto dto = WorkflowMapper.INSTANCE.toDto(workflow);
        collectSteps(dto, workflow);

        return dto;
    }

    @Override
    protected String getItemTypeName() {
        return Workflow.class.getName();
    }
}
