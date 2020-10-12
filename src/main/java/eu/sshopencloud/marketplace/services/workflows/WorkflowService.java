package eu.sshopencloud.marketplace.services.workflows;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.workflows.PaginatedWorkflows;
import eu.sshopencloud.marketplace.dto.workflows.StepDto;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowCore;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowDto;
import eu.sshopencloud.marketplace.mappers.workflows.WorkflowMapper;
import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.model.workflows.StepsTree;
import eu.sshopencloud.marketplace.model.workflows.StepsTreeVisitor;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.repositories.workflows.WorkflowRepository;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.validators.workflows.WorkflowFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowFactory workflowFactory;
    private final ItemService itemService;
    private final IndexService indexService;
    private final StepService stepService;


    public PaginatedWorkflows getWorkflows(PageCoords pageCoords) {
        Page<Workflow> workflowsPage = workflowRepository.findAll(PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));
        List<WorkflowDto> workflows = workflowsPage.stream()
                .map(this::convertWorkflow)
                .collect(Collectors.toList());

        return PaginatedWorkflows.builder().workflows(workflows)
                .count(workflowsPage.getContent().size()).hits(workflowsPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(workflowsPage.getTotalPages())
                .build();
    }


    public WorkflowDto getWorkflow(Long workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Workflow.class.getName() + " with id " + workflowId));

        return convertWorkflow(workflow);
    }

    private WorkflowDto convertWorkflow(Workflow workflow) {
        WorkflowDto dto = WorkflowMapper.INSTANCE.toDto(workflow);

        collectSteps(dto, workflow);
        itemService.completeItem(dto);

        return dto;
    }

    private void collectSteps(WorkflowDto dto, Workflow workflow) {
        StepsTree tree = workflow.getStepsTree();
        Stack<StepDto> nestedSteps = new Stack<>();
        List<StepDto> rootSteps = new ArrayList<>();

        tree.visit(new StepsTreeVisitor() {
            @Override
            public void onNextStep(Step step) {
                StepDto stepDto = stepService.convertStep(step);
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
        return createNewWorkflowVersion(workflowCore, null);
    }

    public WorkflowDto updateWorkflow(long workflowId, WorkflowCore workflowCore) {
        if (!workflowRepository.existsById(workflowId))
            throw new EntityNotFoundException("Unable to find " + Workflow.class.getName() + " with id " + workflowId);

        return createNewWorkflowVersion(workflowCore, workflowId);
    }

    private WorkflowDto createNewWorkflowVersion(WorkflowCore workflowCore, Long prevWorkflowId) {
        Workflow prevWorkflow = (prevWorkflowId != null) ? workflowRepository.getOne(prevWorkflowId) : null;
        Workflow workflow = workflowFactory.create(workflowCore, prevWorkflow);

        workflow = workflowRepository.save(workflow);
        indexService.indexItem(workflow);

        return itemService.completeItem(WorkflowMapper.INSTANCE.toDto(workflow));
    }

    public void deleteWorkflow(long workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Workflow.class.getName() + " with id " + workflowId)
        );

        itemService.cleanupItem(workflow);
        workflowRepository.delete(workflow);
        indexService.removeItem(workflow);
    }
}
