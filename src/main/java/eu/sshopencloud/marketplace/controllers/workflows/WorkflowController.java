package eu.sshopencloud.marketplace.controllers.workflows;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.items.ItemsDifferencesDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.dto.workflows.*;
import eu.sshopencloud.marketplace.services.items.StepService;
import eu.sshopencloud.marketplace.services.items.WorkflowService;
import eu.sshopencloud.marketplace.services.items.exception.ItemIsAlreadyMergedException;
import eu.sshopencloud.marketplace.services.items.exception.VersionNotChangedException;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final PageCoordsValidator pageCoordsValidator;
    private final WorkflowService workflowService;
    private final StepService stepService;


    @Operation(summary = "Retrieve all workflows in pages")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedWorkflows> getWorkflows(@RequestParam(value = "page", required = false) Integer page,
                                                           @RequestParam(value = "perpage", required = false) Integer perpage,
                                                           @RequestParam(value = "approved", defaultValue = "true") boolean approved)
            throws PageTooLargeException {
        return ResponseEntity.ok(workflowService.getWorkflows(pageCoordsValidator.validate(page, perpage), approved));
    }

    @Operation(summary = "Get single workflow by its persistentId")
    @GetMapping(path = "/{persistentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> getWorkflow(@PathVariable("persistentId") String workflowPersistentId,
                                                   @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                   @RequestParam(value = "approved", defaultValue = "true") boolean approved,
                                                   @RequestParam(value = "redirect", defaultValue = "false") boolean redirect) {

        return ResponseEntity.ok(workflowService.getLatestWorkflow(workflowPersistentId, draft, approved, redirect));
    }

    @Operation(summary = "Get workflow selected version by its persistentId and versionId")
    @GetMapping(path = "/{persistentId}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> getWorkflowVersion(@PathVariable("persistentId") String workflowPersistentId,
                                                          @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(workflowService.getWorkflowVersion(workflowPersistentId, versionId));
    }

    @Operation(summary = "Creating workflow")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> createWorkflow(@Parameter(
            description = "Created workflow",
            required = true,
            schema = @Schema(implementation = WorkflowCore.class)) @RequestBody WorkflowCore newWorkflow,
                                                      @RequestParam(value = "draft", defaultValue = "false") boolean draft) {
        return ResponseEntity.ok(workflowService.createWorkflow(newWorkflow, draft));
    }

    @Operation(summary = "Updating workflow for given persistentId")
    @PutMapping(path = "/{persistentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> updateWorkflow(@PathVariable("persistentId") String workflowPersistentId,
                                                      @Parameter(
                                                              description = "Updated workflow",
                                                              required = true,
                                                              schema = @Schema(implementation = WorkflowCore.class)) @RequestBody WorkflowCore updatedWorkflow,
                                                      @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                      @RequestParam(value = "approved", defaultValue = "true") boolean approved) throws VersionNotChangedException {

        return ResponseEntity.ok(workflowService.updateWorkflow(workflowPersistentId, updatedWorkflow, draft, approved));
    }

    @Operation(summary = "Revert workflow to target version by its persistentId and versionId that is reverted to")
    @PutMapping(path = "/{persistentId}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> revertWorkflow(@PathVariable("persistentId") String workflowPersistentId, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(workflowService.revertWorkflow(workflowPersistentId, versionId));
    }

    @Operation(summary = "Delete workflow by its persistentId")
    @DeleteMapping(path = "/{persistentId}")
    public void deleteWorkflow(@PathVariable("persistentId") String workflowPersistentId,
                               @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        workflowService.deleteWorkflow(workflowPersistentId, draft);
    }

    @Operation(summary = "Delete workflow by its persistentId and versionId")
    @DeleteMapping(path = "/{persistentId}/versions/{versionId}")
    public void deleteWorkflowVersion(@PathVariable("persistentId") String workflowPersistentId, @PathVariable("versionId") long versionId) {

        workflowService.deleteWorkflow(workflowPersistentId, versionId);
    }


    @Operation(summary = "Get single step by its persistentId and workflow persistentId")
    @GetMapping(path = "/{persistentId}/steps/{stepPersistentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> getStep(@PathVariable("persistentId") String workflowPersistentId,
                                           @PathVariable("stepPersistentId") String stepPersistentId,
                                           @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                           @RequestParam(value = "approved", defaultValue = "true") boolean approved,
                                           @RequestParam(value = "redirect", defaultValue = "false") boolean redirect) {

        return ResponseEntity.ok(stepService.getLatestStep(workflowPersistentId, stepPersistentId, draft, approved, redirect));
    }

    @Operation(summary = "Get step selected version by its persistentId, versionId and workflow persistentId")
    @GetMapping(path = "/{persistentId}/steps/{stepPersistentId}/versions/{stepVersionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> getStepVersion(@PathVariable("persistentId") String workflowPersistentId,
                                                  @PathVariable("stepPersistentId") String stepPersistentId,
                                                  @PathVariable("stepVersionId") long stepVersionId) {

        return ResponseEntity.ok(stepService.getStepVersion(workflowPersistentId, stepPersistentId, stepVersionId));
    }

    @Operation(summary = "Creating step for given persistentId and workflow persistentId")
    @PostMapping(path = "/{persistentId}/steps", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> createStep(@PathVariable("persistentId") String workflowPersistentId,
                                              @Parameter(
                                                      description = "Created step",
                                                      required = true,
                                                      schema = @Schema(implementation = StepCore.class)) @RequestBody StepCore newStep,
                                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(stepService.createStep(workflowPersistentId, newStep, draft));
    }

    @Operation(summary = "Creating substep for given persistentId and workflow persistentId")
    @PostMapping(path = "/{persistentId}/steps/{stepPersistentId}/steps", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> createSubstep(@PathVariable("persistentId") String workflowPersistentId,
                                                 @PathVariable("stepPersistentId") String stepPersistentId,
                                                 @Parameter(
                                                         description = "Created substep",
                                                         required = true,
                                                         schema = @Schema(implementation = StepCore.class)) @RequestBody StepCore newStep,
                                                 @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(stepService.createSubStep(workflowPersistentId, stepPersistentId, newStep, draft));
    }

    @Operation(summary = "Updated step for given persistentId and workflow persistentId")
    @PutMapping(path = "/{persistentId}/steps/{stepPersistentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> updateStep(@PathVariable("persistentId") String workflowPersistentId,
                                              @PathVariable("stepPersistentId") String stepPersistentId,
                                              @Parameter(
                                                      description = "Updated step",
                                                      required = true,
                                                      schema = @Schema(implementation = StepCore.class)) @RequestBody StepCore updatedStep,
                                              @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                              @RequestParam(value = "approved", defaultValue = "true") boolean approved) throws VersionNotChangedException {

        return ResponseEntity.ok(stepService.updateStep(workflowPersistentId, stepPersistentId, updatedStep, draft, approved));
    }

    @Operation(summary = "Revert step to target version by its persistentId and versionId that is reverted to")
    @PutMapping(path = "/{persistentId}/steps/{stepPersistentId}/versions/{stepVersionId}/revert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> revertStep(@PathVariable("persistentId") String workflowPersistentId,
                                              @PathVariable("stepPersistentId") String stepPersistentId,
                                              @PathVariable("stepVersionId") long stepVersionId) {

        return ResponseEntity.ok(stepService.revertStep(workflowPersistentId, stepPersistentId, stepVersionId));
    }

    @Operation(summary = "Delete step by its persistentId and workflow persistentId")
    @DeleteMapping("/{persistentId}/steps/{stepPersistentId}")
    public void deleteStep(@PathVariable("persistentId") String workflowPersistentId,
                           @PathVariable("stepPersistentId") String stepPersistentId,
                           @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        stepService.deleteStep(workflowPersistentId, stepPersistentId, draft);
    }

    @Operation(summary = "Delete step by its persistentId, versionId and workflow persistentId")
    @DeleteMapping(path = "/{persistentId}/steps/{stepPersistentId}/versions/{stepVersionId}")
    public void deleteStepVersion(@PathVariable("persistentId") String workflowPersistentId,
                           @PathVariable("stepPersistentId") String stepPersistentId, @PathVariable("stepVersionId") long stepVersionId) {

        stepService.deleteStep(workflowPersistentId, stepPersistentId, stepVersionId);
    }

    @Operation(summary = "Committing draft of workflow by its persistentId")
    @PostMapping(path = "/{persistentId}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> publishWorkflow(@PathVariable("persistentId") String workflowPersistentId) {
        WorkflowDto workflow = workflowService.commitDraftWorkflow(workflowPersistentId);
        return ResponseEntity.ok(workflow);
    }

    @Operation(summary = "Retrieving history of workflow by its persistentId")
    @GetMapping(path = "/{persistentId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getWorkflowHistory(@PathVariable("persistentId") String workflowPersistentId,
                                                                    @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                    @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(workflowService.getWorkflowVersions(workflowPersistentId, draft, approved));
    }

    @Operation(summary = "Retrieving history of step by its persistentId and workflow persistentId")
    @GetMapping(path = "/{persistentId}/steps/{stepPersistentId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getStepHistory(@PathVariable("persistentId") String workflowPersistentId,
                                                                @PathVariable("stepPersistentId") String stepPersistentId,
                                                                @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(stepService.getStepVersions(workflowPersistentId, stepPersistentId, draft, approved));
    }

    @Operation(summary = "Retrieving list of information-contributors across the whole history of workflow by its persistentId", operationId = "getWorkflowInformationContributors")
    @GetMapping(path = "/{persistentId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("persistentId") String workflowPersistentId) {

        return ResponseEntity.ok(workflowService.getInformationContributors(workflowPersistentId));
    }

    @Operation(summary = "Retrieving list of information-contributors to the selected version of workflow by its persistentId and versionId", operationId = "getWorkflowVersionInformationContributors")
    @GetMapping(path = "/{persistentId}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("persistentId") String workflowPersistentId,
                                                                    @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(workflowService.getInformationContributors(workflowPersistentId, versionId));
    }

    @Operation(summary = "Retrieving list of information-contributors across the whole history of step by its persistentId and workflow persistentId", operationId = "getStepInformationContributors")
    @GetMapping(path = "/{persistentId}/steps/{stepPersistentId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getStepInformationContributors(@PathVariable("persistentId") String workflowPersistentId,
                                                                        @PathVariable("stepPersistentId") String stepPersistentId) {

        return ResponseEntity.ok(stepService.getInformationContributors(workflowPersistentId, stepPersistentId));
    }

    @Operation(summary = "Retrieving list of information-contributors to the selected version of step by its persistentId, versionId and workflow persistentId", operationId = "getStepVersionInformationContributors")
    @GetMapping(path = "/{persistentId}/steps/{stepPersistentId}/versions/{stepVersionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getStepInformationContributors(@PathVariable("persistentId") String workflowPersistentId,
                                                                        @PathVariable("stepPersistentId") String stepPersistentId,
                                                                        @PathVariable("stepVersionId") long stepVersionId) {

        return ResponseEntity.ok(stepService.getInformationContributors(workflowPersistentId, stepPersistentId, stepVersionId));
    }

    @Operation(summary = "Getting body of merged version of workflow", operationId = "getWorkflowMerge")
    @GetMapping(path = "/{persistentId}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> getMerge(@PathVariable("persistentId") String workflowPersistentId,
                                                @RequestParam List<String> with) {
        return ResponseEntity.ok(workflowService.getMerge(workflowPersistentId, with));
    }

    @Operation(summary = "Performing merge into workflow", operationId = "mergeWorkflow")
    @PostMapping(path = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> merge(@RequestParam List<String> with,
                                             @Parameter(
                                                     description = "Merged workflow",
                                                     required = true,
                                                     schema = @Schema(implementation = WorkflowCore.class)) @RequestBody WorkflowCore mergeWorkflow)
            throws ItemIsAlreadyMergedException {
        return ResponseEntity.ok(workflowService.merge(mergeWorkflow, with));
    }

    @Operation(summary = "Getting body of merged version of step with its workflow persistentId", operationId = "getStepMerge")
    @GetMapping(path = "{persistentId}/steps/{stepPersistentId}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> getMergeSteps(@PathVariable("persistentId") String workflowPersistentId,
                                                 @PathVariable("stepPersistentId") String stepPersistentId,
                                                 @RequestParam List<String> with) {
        return ResponseEntity.ok(stepService.getMerge(stepPersistentId, with));
    }

    @Operation(summary = "Performing merge into step", operationId = "mergeStep")
    @PostMapping(path = "{persistentId}/steps/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> mergeSteps(@PathVariable("persistentId") String workflowPersistentId,
                                              @RequestParam List<String> with,
                                              @Parameter(
                                                      description = "Merged step",
                                                      required = true,
                                                      schema = @Schema(implementation = StepCore.class)) @RequestBody StepCore mergeStep)
            throws ItemIsAlreadyMergedException {
        return ResponseEntity.ok(stepService.merge(workflowPersistentId, mergeStep, with));
    }

    @Operation(summary = "Getting list of sources of workflow by its persistentId", operationId = "getWorkflowSources")
    @GetMapping(path = "/{persistentId}/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SourceDto>> getSources(@PathVariable("persistentId") String workflowPersistentId) {

        return ResponseEntity.ok(workflowService.getSources(workflowPersistentId));
    }

    @Operation(summary = "Getting list of sources of step by its persistentId and workflow persistentId", operationId = "getStepSources")
    @GetMapping(path = "/{persistentId}/steps/{stepPersistentId}/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SourceDto>> getStepSources(@PathVariable("persistentId") String workflowPersistentId,
                                                          @PathVariable("stepPersistentId") String stepPersistentId) {
        return ResponseEntity.ok(stepService.getSources(workflowPersistentId, stepPersistentId));
    }

    @Operation(summary = "Getting differences between workflow and target version of item", operationId = "getWorkflowAndVersionedItemDifferences")
    @GetMapping(path = "/{persistentId}/diff", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemsDifferencesDto> getWorkflowVersionedItemDifferences(@PathVariable("persistentId") String persistentId,
                                                                                   @RequestParam(required = true) String with,
                                                                                   @RequestParam(required = false) Long otherVersionId) {

        return ResponseEntity.ok(workflowService.getDifferences(persistentId, null, with, otherVersionId));
    }


    @Operation(summary = "Getting differences between target version of workflow and target version of item", operationId = "getVersionedWorkflowAndVersionedItemDifferences")
    @GetMapping(path = "/{persistentId}/versions/{versionId}/diff", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemsDifferencesDto> getVersionedWorkflowVersionedItemDifferences(@PathVariable("persistentId") String persistentId,
                                                                                            @PathVariable("versionId") long versionId,
                                                                                            @RequestParam(required = true) String with,
                                                                                            @RequestParam(required = false) Long otherVersionId) {

        return ResponseEntity.ok(workflowService.getDifferences(persistentId, versionId, with, otherVersionId));
    }


    @Operation(summary = "Getting differences between step and target version of item", operationId = "getStepAndVersionedItemDifferences")
    @GetMapping(path = "/{persistentId}/steps/{stepPersistentId}/diff", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemsDifferencesDto> getStepVersionedItemDifferences(@PathVariable("persistentId") String persistentId,
                                                                               @PathVariable("stepPersistentId") String stepPersistentId,
                                                                               @RequestParam(required = true) String with,
                                                                               @RequestParam(required = false) Long otherVersionId) {

        return ResponseEntity.ok(stepService.getDifferences(persistentId, stepPersistentId, null, with, otherVersionId));
    }


    @Operation(summary = "Getting differences between target version of step and target version of item", operationId = "getVersionedStepAndVersionedItemDifferences")
    @GetMapping(path = "/{persistentId}/steps/{stepPersistentId}/versions/{versionId}/diff", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemsDifferencesDto> getVersionedStepVersionedItemDifferences(@PathVariable("persistentId") String persistentId,
                                                                                        @PathVariable("stepPersistentId") String stepPersistentId,
                                                                                        @PathVariable("versionId") long versionId,
                                                                                        @RequestParam(required = true) String with,
                                                                                        @RequestParam(required = false) Long otherVersionId) {

        return ResponseEntity.ok(stepService.getDifferences(persistentId, stepPersistentId, versionId, with, otherVersionId));
    }


}
