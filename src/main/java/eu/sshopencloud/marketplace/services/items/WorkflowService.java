package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.items.ItemsDifferencesDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.dto.workflows.PaginatedWorkflows;
import eu.sshopencloud.marketplace.dto.workflows.StepDto;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowCore;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowDto;
import eu.sshopencloud.marketplace.mappers.workflows.WorkflowMapper;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
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
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.auth.UserService;
import eu.sshopencloud.marketplace.services.items.exception.ItemIsAlreadyMergedException;
import eu.sshopencloud.marketplace.services.items.exception.VersionNotChangedException;
import eu.sshopencloud.marketplace.services.search.IndexItemService;
import eu.sshopencloud.marketplace.services.sources.SourceService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.validators.workflows.WorkflowFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
@Slf4j
public class WorkflowService extends ItemCrudService<Workflow, WorkflowDto, PaginatedWorkflows, WorkflowCore> {

    private final WorkflowRepository workflowRepository;
    private final WorkflowFactory workflowFactory;
    private final StepService stepService;


    public WorkflowService(WorkflowRepository workflowRepository, WorkflowFactory workflowFactory, @Lazy StepService stepService,
                           ItemRepository itemRepository, VersionedItemRepository versionedItemRepository,
                           ItemVisibilityService itemVisibilityService, ItemUpgradeRegistry<Workflow> itemUpgradeRegistry,
                           DraftItemRepository draftItemRepository, ItemRelatedItemService itemRelatedItemService,
                           PropertyTypeService propertyTypeService, IndexItemService indexItemService, UserService userService,
                           MediaStorageService mediaStorageService, SourceService sourceService, ApplicationEventPublisher eventPublisher) {

        super(
                itemRepository, versionedItemRepository, itemVisibilityService, itemUpgradeRegistry, draftItemRepository,
                itemRelatedItemService, propertyTypeService, indexItemService, userService, mediaStorageService, sourceService,
                eventPublisher
        );

        this.workflowRepository = workflowRepository;
        this.workflowFactory = workflowFactory;
        this.stepService = stepService;
    }


    public PaginatedWorkflows getWorkflows(PageCoords pageCoords, boolean approved) {
        return getItemsPage(pageCoords, approved);
    }

    public WorkflowDto getLatestWorkflow(String persistentId, boolean draft, boolean approved, boolean redirect) {
        return getLatestItem(persistentId, draft, approved, redirect);
    }

    public WorkflowDto getWorkflowVersion(String persistentId, long versionId) {
        return getItemVersion(persistentId, versionId);
    }

    private void collectSteps(WorkflowDto dto, Workflow workflow) {
        StepsTree tree = workflow.gatherSteps();
        Stack<StepDto> nestedSteps = new Stack<>();
        List<StepDto> rootSteps = new ArrayList<>();

        tree.visit(new StepsTreeVisitor() {
            @Override
            public void onNextStep(StepsTree stepTree) {
                Step step = stepTree.getStep();
                StepDto stepDto = stepService.prepareItemDto(step);
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
        return prepareItemDto(workflow);
    }

    public WorkflowDto updateWorkflow(String persistentId, WorkflowCore workflowCore, boolean draft, boolean approved) throws VersionNotChangedException {
        Workflow workflow = updateItem(persistentId, workflowCore, draft, approved);

        if (!draft)
            commitSteps(workflow.getStepsTree());

        return prepareItemDto(workflow);
    }

    private void commitSteps(StepsTree stepsTree) {
        stepsTree.visit(new StepsTreeVisitor() {
            @Override
            public void onNextStep(StepsTree stepTree) {
                Step step = stepTree.getStep();

                if (step != null && step.getStatus().equals(ItemStatus.DRAFT)) {
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

    public void deleteWorkflow(String persistentId, boolean draft) {
        if (draft) {
            deleteWorkflowDraft(persistentId);
        } else {
            deleteWorkflowVersion(persistentId, null);
        }
    }

    public void deleteWorkflow(String persistentId, long versionId) {
        Workflow workflow = loadItemVersion(persistentId, versionId);
        if (workflow.getStatus() == ItemStatus.DRAFT) {
            User currentUser = LoggedInUserHolder.getLoggedInUser();
            if (currentUser.equals(workflow.getInformationContributor())) {
                deleteWorkflowDraft(persistentId);
            } else {
                throw new AccessDeniedException(
                        String.format(
                                "User is not authorized to access the given draft version with id %s (version id: %d)",
                                persistentId, versionId
                        )
                );
            }
        } else {
            deleteWorkflowVersion(persistentId, versionId);
        }
    }

    public void deleteWorkflowVersion(String persistentId, Long versionId) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();
        if (!currentUser.isModerator())
            throw new AccessDeniedException("Current user is not a moderator and is not allowed to remove items");

        Workflow currentWorkflow = loadCurrentItem(persistentId);
        Workflow workflow = (versionId != null) ? loadItemVersion(persistentId, versionId) : currentWorkflow;


        workflow.getStepsTree().visit(new StepsTreeVisitor() {
            @Override
            public void onNextStep(StepsTree stepTree) {
                Step step = stepTree.getStep();
                stepService.deleteStepOnly(step, false);
            }

            @Override
            public void onBackToParent() {
            }
        });

        setDeleteItem(persistentId, versionId);
    }


    private void deleteWorkflowDraft(String persistentId) {
        Workflow workflow = loadLatestItem(persistentId);
        workflow.getStepsTree().visit(new StepsTreeVisitor() {
            @Override
            public void onNextStep(StepsTree stepTree) {
                Step step = stepTree.getStep();
                stepService.deleteStepOnly(step, true);
            }

            @Override
            public void onBackToParent() {
            }
        });

        deleteItemDraft(persistentId);
    }

    Workflow liftWorkflowForNewStep(String persistentId, boolean draft) {
        Optional<Workflow> workflowDraft = resolveItemDraftForCurrentUser(persistentId);

        if (!draft && workflowDraft.isPresent()) {
            throw new IllegalArgumentException(
                    String.format(
                            "%s with id %s is in draft state for current user, so a non-draft step cannot be added, changed and removed",
                            getItemTypeName(), persistentId
                    )
            );
        }

        if (draft && workflowDraft.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format(
                            "No draft %s with id %s is found for current user, so a draft step cannot be added, changed and removed",
                            getItemTypeName(), persistentId
                    )
            );
        }

        return super.liftItemVersion(persistentId, draft);
    }

    public WorkflowDto commitDraftWorkflow(String workflowId) {
        Workflow committedWorkflow = publishDraftItem(workflowId);
        commitSteps(committedWorkflow.gatherSteps());

        return prepareItemDto(committedWorkflow);
    }


    @Override
    protected Workflow saveItemVersion(Workflow workflow) {
        workflow = super.saveItemVersion(workflow);
        workflowRepository.flush();
        workflowRepository.refresh(workflow);

        return workflowRepository.getOne(workflow.getId());
    }

    @Override
    protected ItemVersionRepository<Workflow> getItemRepository() {
        return workflowRepository;
    }

    @Override
    protected Workflow makeItem(WorkflowCore workflowCore, Workflow prevWorkflow, boolean conflict) {
        return workflowFactory.create(workflowCore, prevWorkflow, conflict);
    }

    @Override
    protected Workflow modifyItem(WorkflowCore workflowCore, Workflow workflow) {
        return workflowFactory.modify(workflowCore, workflow);
    }

    @Override
    protected Workflow makeItemCopy(Workflow workflow) {
        return workflowFactory.makeNewVersion(workflow);
    }

    @Override
    protected PaginatedWorkflows wrapPage(Page<Workflow> workflowsPage, List<WorkflowDto> workflows) {
        return PaginatedWorkflows.builder()
                .workflows(workflows)
                .count(workflowsPage.getContent().size())
                .hits(workflowsPage.getTotalElements())
                .page(workflowsPage.getNumber() + 1)
                .perpage(workflowsPage.getSize())
                .pages(workflowsPage.getTotalPages())
                .build();
    }

    @Override
    protected WorkflowDto convertItemToDto(Workflow workflow) {

        WorkflowDto dto = WorkflowMapper.INSTANCE.toDto(workflow);

        if (!LoggedInUserHolder.getLoggedInUser().isModerator()) {
            dto.getInformationContributor().setEmail(null);
            dto.getContributors().forEach(contributor -> contributor.getActor().setEmail(null));
        }

        collectSteps(dto, workflow);

        return dto;
    }

    @Override
    protected WorkflowDto convertToDto(Item item) {

        WorkflowDto dto = WorkflowMapper.INSTANCE.toDto(item);
        if (!LoggedInUserHolder.getLoggedInUser().isModerator()) {
            dto.getInformationContributor().setEmail(null);
            dto.getContributors().forEach(contributor -> contributor.getActor().setEmail(null));
        }
        return dto;
    }


    @Override
    protected String getItemTypeName() {
        return Workflow.class.getName();
    }

    public List<ItemExtBasicDto> getWorkflowVersions(String persistentId, boolean draft, boolean approved) {
        return getItemHistory(persistentId, getLatestWorkflow(persistentId, draft, approved, false).getId());
    }

    public List<UserDto> getInformationContributors(String id) {
        return super.getInformationContributors(id);
    }

    public List<UserDto> getInformationContributors(String id, Long versionId) {
        return super.getInformationContributors(id, versionId);
    }


    public WorkflowDto getMerge(String persistentId, List<String> mergeList) {
        return prepareMergeItems(persistentId, mergeList);
    }

    public void collectStepsFromMergedWorkflows(Workflow workflow, List<String> workflowList) {

        for (String s : workflowList) {
            Workflow workflowTmp = loadCurrentItem(s);
            if (!workflowTmp.getAllSteps().isEmpty())
                collectTrees(workflow.getStepsTree(), workflowTmp.getAllSteps());
        }

    }

    public void collectTrees(StepsTree parent, List<StepsTree> stepsTrees) {
        StepsTree s;
        List<StepsTree> subTrees = new ArrayList<>();

        for (StepsTree stepsTree : stepsTrees) {
            s = stepsTree;
            if (!s.isRoot() && !Objects.isNull(s.getId()))
                if (s.getSubTrees().size() > 0) {
                    stepService.addStepToTree(s.getStep(), null, parent);
                    Step step = s.getStep();
                    List<StepsTree> nextParentList = parent.getSubTrees().stream().filter(c -> c.getStep().equals(step)).collect(Collectors.toList());
                    StepsTree nextParent = nextParentList.get(0);
                    subTrees.addAll(s.getSubTrees());
                    collectTrees(nextParent, s.getSubTrees());
                } else {
                    if (!subTrees.contains(s))
                        stepService.addStepToTree(s.getStep(), null, parent);
                }
        }
    }

    public WorkflowDto merge(WorkflowCore mergeWorkflow, List<String> mergeList) throws ItemIsAlreadyMergedException {
        checkIfMergeIsPossible(mergeList);
        Workflow workflow = createItem(mergeWorkflow, false);
        workflow = mergeItem(workflow.getPersistentId(), mergeList);
        WorkflowDto workflowDto = prepareItemDto(workflow);
        collectStepsFromMergedWorkflows(workflow, findAllWorkflows(mergeList));
        commitSteps(workflow.getStepsTree());
        collectSteps(workflowDto, workflow);
        return workflowDto;
    }

    public List<String> findAllWorkflows(List<String> mergeList) {
        List<String> mergeWorkflowsList = new ArrayList<>();
        for (String mergeItem : mergeList)
            if (checkIfWorkflow(mergeItem)) mergeWorkflowsList.add(mergeItem);

        return mergeWorkflowsList;
    }

    public List<SourceDto> getSources(String persistentId) {
        return getAllSources(persistentId);
    }

    public ItemsDifferencesDto getDifferences(String workflowPersistentId, Long workflowVersionId, String otherPersistentId, Long otherVersionId) {

        return super.getDifferences(workflowPersistentId, workflowVersionId, otherPersistentId, otherVersionId);
    }
}
