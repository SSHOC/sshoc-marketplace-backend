package eu.sshopencloud.marketplace.controllers.workflows;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.dto.workflows.*;
import eu.sshopencloud.marketplace.services.items.StepService;
import eu.sshopencloud.marketplace.services.items.WorkflowService;
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
    @GetMapping(path = "/{workflowPersistentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> getWorkflow(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                   @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                   @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(workflowService.getLatestWorkflow(workflowPersistentId, draft, approved));
    }

    @Operation(summary = "Get workflow selected version by its persistentId and versionId")
    @GetMapping(path = "/{workflowPersistentId}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> getWorkflowVersion(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                          @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(workflowService.getWorkflowVersion(workflowPersistentId, versionId));
    }

    @Operation(summary = "Creating workflow")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> createWorkflow(@Parameter(
            description = "Created workflow",
            required = true,
            schema = @Schema(implementation =WorkflowCore.class)) @RequestBody WorkflowCore newWorkflow,
                                                      @RequestParam(value = "draft", defaultValue = "false") boolean draft) {
        return ResponseEntity.ok(workflowService.createWorkflow(newWorkflow, draft));
    }

    @Operation(summary = "Updating workflow for given persistentId")
    @PutMapping(path = "/{workflowPersistentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> updateWorkflow(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                      @Parameter(
                                                              description = "Updated workflow",
                                                              required = true,
                                                              schema = @Schema(implementation = WorkflowCore.class)) @RequestBody WorkflowCore updatedWorkflow,
                                                      @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(workflowService.updateWorkflow(workflowPersistentId, updatedWorkflow, draft));
    }

    @Operation(summary = "Revert workflow to target version by its persistentId and versionId that is reverted to")
    @PutMapping(path = "/{workflowPersistentId}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> revertWorkflow(@PathVariable("workflowPersistentId") String workflowPersistentId, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(workflowService.revertWorkflow(workflowPersistentId, versionId));
    }

    @Operation(summary = "Delete workflow by its persistentId")
    @DeleteMapping(path = "/{workflowPersistentId}")
    public void deleteWorkflow(@PathVariable("workflowPersistentId") String workflowPersistentId,
                               @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        workflowService.deleteWorkflow(workflowPersistentId, draft);
    }

    @Operation(summary = "Get single step by its persistentId and workflow persistentId")
    @GetMapping(path = "/{workflowPersistentId}/steps/{stepPersistentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> getStep(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                           @PathVariable("stepPersistentId") String stepPersistentId,
                                           @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                           @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(stepService.getLatestStep(workflowPersistentId, stepPersistentId, draft, approved));
    }

    @Operation(summary = "Get step selected version by its persistentId, versionId and workflow persistentId")
    @GetMapping(path = "/{workflowPersistentId}/steps/{stepPersistentId}/versions/{stepVersionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> getStepVersion(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                  @PathVariable("stepPersistentId") String stepPersistentId,
                                                  @PathVariable("stepVersionId") long stepVersionId) {

        return ResponseEntity.ok(stepService.getStepVersion(workflowPersistentId, stepPersistentId, stepVersionId));
    }

    @Operation(summary = "Creating step for given persistentId and workflow persistentId")
    @PostMapping(path = "/{workflowPersistentId}/steps", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> createStep(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                              @Parameter(
                                                      description = "Created step",
                                                      required = true,
                                                      schema = @Schema(implementation = StepCore.class)) @RequestBody StepCore newStep,
                                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(stepService.createStep(workflowPersistentId, newStep, draft));
    }

    @Operation(summary = "Creating substep for given persistentId and workflow persistentId")
    @PostMapping(path = "/{workflowPersistentId}/steps/{stepPersistentId}/steps", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> createSubstep(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                 @PathVariable("stepPersistentId") String stepPersistentId,
                                                 @Parameter(
                                                         description = "Created substep",
                                                         required = true,
                                                         schema = @Schema(implementation = StepCore.class)) @RequestBody StepCore newStep,
                                                 @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(stepService.createSubStep(workflowPersistentId, stepPersistentId, newStep, draft));
    }

    @Operation(summary = "Updated step for given persistentId and workflow persistentId")
    @PutMapping(path = "/{workflowPersistentId}/steps/{stepPersistentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> updateStep(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                              @PathVariable("stepPersistentId") String stepPersistentId,
                                              @Parameter(
                                                      description = "Updated step",
                                                      required = true,
                                                      schema = @Schema(implementation = StepCore.class)) @RequestBody StepCore updatedStep,
                                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(stepService.updateStep(workflowPersistentId, stepPersistentId, updatedStep, draft));
    }

    @Operation(summary = "Revert step to target version by its persistentId and versionId that is reverted to")
    @PutMapping(path = "/{workflowPersistentId}/steps/{stepPersistentId}/versions/{stepVersionId}/revert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> revertStep(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                              @PathVariable("stepPersistentId") String stepPersistentId,
                                              @PathVariable("stepVersionId") long stepVersionId) {

        return ResponseEntity.ok(stepService.revertStep(workflowPersistentId, stepPersistentId, stepVersionId));
    }

    @Operation(summary = "Delete step by its persistentId and workflow persistentId")
    @DeleteMapping("/{workflowPersistentId}/steps/{stepPersistentId}")
    public void deleteStep(@PathVariable("workflowPersistentId") String workflowPersistentId,
                           @PathVariable("stepPersistentId") String stepPersistentId,
                           @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        stepService.deleteStep(workflowPersistentId, stepPersistentId, draft);
    }

    @Operation(summary = "Committing draft of workflow by its persistentId")
    @PostMapping(path = "/{workflowPersistentId}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> publishWorkflow(@PathVariable("workflowPersistentId") String workflowPersistentId) {
        WorkflowDto workflow = workflowService.commitDraftWorkflow(workflowPersistentId);
        return ResponseEntity.ok(workflow);
    }

    @Operation(summary = "Retrieving history of workflow by its persistentId" )
    @GetMapping(path = "/{workflowPersistentId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getWorkflowHistory(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                                    @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                    @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(workflowService.getWorkflowVersions(workflowPersistentId, draft, approved));
    }

    @Operation(summary = "Retrieving history of step by its persistentId and workflow persistentId")
    @GetMapping(path = "/{workflowPersistentId}/steps/{stepPersistentId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getStepHistory(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                                @PathVariable("stepPersistentId") String stepPersistentId,
                                                                @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(stepService.getStepVersions(workflowPersistentId, stepPersistentId, draft, approved));
    }

    @Operation(summary = "Retrieving list of information-contributors across the whole history of workflow by its persistentId")
    @GetMapping(path = "/{workflowPersistentId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("workflowPersistentId") String workflowPersistentId) {

        return ResponseEntity.ok(workflowService.getInformationContributors(workflowPersistentId));
    }

    @Operation(summary = "Retrieving list of information-contributors to the selected version of workflow by its persistentId and versionId")
    @GetMapping(path = "/{workflowPersistentId}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributorsForVersion(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                                              @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(workflowService.getInformationContributors(workflowPersistentId, versionId));
    }

    @Operation(summary = "Retrieving list of information-contributors across the whole history of step by its persistentId and workflow persistentId")
    @GetMapping(path = "/{workflowPersistentId}/steps/{stepPersistentId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getStepInformationContributors(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                                        @PathVariable("stepPersistentId") String stepPersistentId) {

        return ResponseEntity.ok(stepService.getInformationContributors(workflowPersistentId, stepPersistentId));
    }

    @Operation(summary = "Retrieving list of information-contributors to the selected version of step by its persistentId, versionId and workflow persistentId")
    @GetMapping(path = "/{workflowPersistentId}/steps/{stepPersistentId}/versions/{stepVersionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getStepInformationContributorsForVersion(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                                                  @PathVariable("stepPersistentId") String stepPersistentId,
                                                                                  @PathVariable("stepVersionId") long stepVersionId) {

        return ResponseEntity.ok(stepService.getInformationContributors(workflowPersistentId, stepPersistentId, stepVersionId));
    }

    @Operation(summary = "Getting body of merged version of workflow")
    @GetMapping(path = "/{workflowPersistentId}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> getMerge(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                @RequestParam List<String> with) {
        return ResponseEntity.ok(workflowService.getMerge(workflowPersistentId, with));
    }

    @Operation(summary = "Performing merge into workflow")
    @PostMapping(path = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> merge(@RequestParam List<String> with,
                                             @Parameter(
                                                     description = "Merged workflow",
                                                     required = true,
                                                     schema = @Schema(implementation = WorkflowCore.class)) @RequestBody WorkflowCore mergeWorkflow) {
        return ResponseEntity.ok(workflowService.merge(mergeWorkflow, with));
    }

    @Operation(summary = "Getting body of merged version of step with its workflow persistentId")
    @GetMapping(path = "{workflowPersistentId}/steps/{stepPersistentId}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> getMergeSteps(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                 @PathVariable("stepPersistentId") String stepPersistentId,
                                                 @RequestParam List<String> with) {
        return ResponseEntity.ok(stepService.getMerge(stepPersistentId, with));
    }

    @Operation(summary = "Performing merge into step")
    @PostMapping(path = "{workflowPersistentId}/steps/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> mergeSteps(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                              @RequestParam List<String> with,
                                              @Parameter(
                                                      description = "Merged step",
                                                      required = true,
                                                      schema = @Schema(implementation = StepCore.class)) @RequestBody StepCore mergeStep) {
        return ResponseEntity.ok(stepService.merge(workflowPersistentId, mergeStep, with));
    }

    @Operation(summary = "Getting list of sources of workflow by its persistentId")
    @GetMapping(path = "/{workflowPersistentId}/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SourceDto>> getSources(@PathVariable("workflowPersistentId") String workflowPersistentId) {

        return ResponseEntity.ok(workflowService.getSources(workflowPersistentId));
    }

    @Operation(summary = "Getting list of sources of step by its persistentId and workflow persistentId")
    @GetMapping(path = "/{workflowPersistentId}/steps/{stepPersistentId}/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SourceDto>> getStepSources(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                          @PathVariable("stepPersistentId") String stepPersistentId) {
        return ResponseEntity.ok(stepService.getSources(workflowPersistentId, stepPersistentId));
    }

}
