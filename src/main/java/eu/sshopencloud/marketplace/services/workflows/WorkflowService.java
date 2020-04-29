package eu.sshopencloud.marketplace.services.workflows;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.workflows.PaginatedWorkflows;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowCore;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowDto;
import eu.sshopencloud.marketplace.mappers.workflows.WorkflowMapper;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.repositories.workflows.WorkflowRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.validators.workflows.WorkflowValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final WorkflowRepository workflowRepository;

    private final WorkflowValidator workflowValidator;

    private final ItemRelatedItemService itemRelatedItemService;

    private final ItemService itemService;

    private final IndexService indexService;


    public PaginatedWorkflows getWorkflows(PageCoords pageCoords) {
        Page<Workflow> workflowsPage = workflowRepository.findAll(PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));
        List<WorkflowDto> workflows = workflowsPage.stream().map(WorkflowMapper.INSTANCE::toDto)
                .map(this::completeWorkflow)
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
        return completeWorkflow(WorkflowMapper.INSTANCE.toDto(workflow));
    }


    private WorkflowDto completeWorkflow(WorkflowDto workflow) {
        itemService.completeItem(workflow);
        return workflow;
    }


    public WorkflowDto createWorkflow(WorkflowCore workflowCore) {
        Workflow workflow = workflowValidator.validate(workflowCore, null);
        itemService.updateInfoDates(workflow);

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        itemService.addInformationContributorToItem(workflow, LoggedInUserHolder.getLoggedInUser());

        Item nextVersion = itemService.clearVersionForCreate(workflow);
        workflow = workflowRepository.save(workflow);
        itemService.switchVersion(workflow, nextVersion);
        indexService.indexItem(workflow);

        return completeWorkflow(WorkflowMapper.INSTANCE.toDto(workflow));
    }


    public WorkflowDto updateWorkflow(long workflowId, WorkflowCore workflowCore) {
        if (!workflowRepository.existsById(workflowId)) {
            throw new EntityNotFoundException("Unable to find " + Workflow.class.getName() + " with id " + workflowId);
        }
        Workflow workflow = workflowValidator.validate(workflowCore, workflowId);
        itemService.updateInfoDates(workflow);

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        itemService.addInformationContributorToItem(workflow, LoggedInUserHolder.getLoggedInUser());

        Item prevVersion = workflow.getPrevVersion();
        Item nextVersion = itemService.clearVersionForUpdate(workflow);
        workflow = workflowRepository.save(workflow);
        itemService.switchVersion(prevVersion, nextVersion);
        indexService.indexItem(workflow);

        return itemService.completeItem(WorkflowMapper.INSTANCE.toDto(workflow));
    }


    public void deleteWorkflow(long workflowId) {
        // TODO don't allow deleting without authentication (in WebSecurityConfig)
        if (!workflowRepository.existsById(workflowId)) {
            throw new EntityNotFoundException("Unable to find " + Workflow.class.getName() + " with id " + workflowId);
        }
        Workflow workflow = workflowRepository.getOne(workflowId);
        itemRelatedItemService.deleteRelationsForItem(workflow);
        Item prevVersion = workflow.getPrevVersion();
        Item nextVersion = itemService.clearVersionForDelete(workflow);
        workflowRepository.delete(workflow);
        itemService.switchVersion(prevVersion, nextVersion);
        indexService.removeItem(workflow);
    }

}
