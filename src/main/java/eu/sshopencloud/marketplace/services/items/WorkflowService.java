package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.workflows.*;
import eu.sshopencloud.marketplace.mappers.workflows.WorkflowMapper;
import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.model.workflows.StepsTree;
import eu.sshopencloud.marketplace.model.workflows.StepsTreeVisitor;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.repositories.items.workflow.WorkflowRepository;
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
                           ItemRelatedItemService itemRelatedItemService, PropertyTypeService propertyTypeService,
                           IndexService indexService) {

        super(itemRepository, versionedItemRepository, itemRelatedItemService, propertyTypeService, indexService);

        this.workflowRepository = workflowRepository;
        this.workflowFactory = workflowFactory;
        this.stepService = stepService;
    }


    public PaginatedWorkflows getWorkflows(PageCoords pageCoords) {
        return super.getItemsPage(pageCoords);
    }

    public WorkflowDto getLatestWorkflow(String persistentId) {
        return super.getLatestItem(persistentId);
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
            public void onNextStep(Step step) {
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

    public WorkflowDto createWorkflow(WorkflowCore workflowCore) {
        Workflow workflow = createItem(workflowCore);
        return prepareItemDto(workflow);
    }

    public WorkflowDto updateWorkflow(String persistentId, WorkflowCore workflowCore) {
        Workflow workflow = updateItem(persistentId, workflowCore);
        return prepareItemDto(workflow);
    }

    public WorkflowDto revertWorkflow(String persistentId, long versionId) {
        Workflow revWorkflow = revertItemVersion(persistentId, versionId);
        return prepareItemDto(revWorkflow);
    }

    public void deleteWorkflow(String persistentId) {
        Workflow workflow = super.loadLatestItem(persistentId);
        workflow.getStepsTree().visit(new StepsTreeVisitor() {
            @Override
            public void onNextStep(Step step) {
                stepService.deleteStepOnly(step);
            }

            @Override
            public void onBackToParent() {}
        });

        super.deleteItem(persistentId);
    }

    public Workflow liftWorkflowVersion(String persistentId) {
        return super.liftItemVersion(persistentId);
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
