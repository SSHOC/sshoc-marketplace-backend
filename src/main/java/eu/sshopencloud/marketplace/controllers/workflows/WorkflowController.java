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


    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedWorkflows> getWorkflows(@RequestParam(value = "page", required = false) Integer page,
                                                           @RequestParam(value = "perpage", required = false) Integer perpage,
                                                           @RequestParam(value = "approved", defaultValue = "true") boolean approved)
            throws PageTooLargeException {
        return ResponseEntity.ok(workflowService.getWorkflows(pageCoordsValidator.validate(page, perpage), approved));
    }

    @GetMapping(path = "/{workflowPersistentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> getWorkflow(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                   @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                   @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(workflowService.getLatestWorkflow(workflowPersistentId, draft, approved));
    }

    @GetMapping(path = "/{workflowPersistentId}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> getWorkflowVersion(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                          @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(workflowService.getWorkflowVersion(workflowPersistentId, versionId));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> createWorkflow(@Parameter(
            description = "Created workflow",
            required = true,
            schema = @Schema(implementation =WorkflowCore.class)) @RequestBody WorkflowCore newWorkflow,
                                                      @RequestParam(value = "draft", defaultValue = "false") boolean draft) {
        return ResponseEntity.ok(workflowService.createWorkflow(newWorkflow, draft));
    }

    @PutMapping(path = "/{workflowPersistentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> updateWorkflow(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                      @Parameter(
                                                              description = "Updated workflow",
                                                              required = true,
                                                              schema = @Schema(implementation = WorkflowCore.class)) @RequestBody WorkflowCore updatedWorkflow,
                                                      @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(workflowService.updateWorkflow(workflowPersistentId, updatedWorkflow, draft));
    }

    @PutMapping(path = "/{workflowPersistentId}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> revertWorkflow(@PathVariable("workflowPersistentId") String workflowPersistentId, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(workflowService.revertWorkflow(workflowPersistentId, versionId));
    }

    @DeleteMapping(path = "/{workflowPersistentId}")
    public void deleteWorkflow(@PathVariable("workflowPersistentId") String workflowPersistentId,
                               @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        workflowService.deleteWorkflow(workflowPersistentId, draft);
    }

    @GetMapping(path = "/{workflowPersistentId}/steps/{stepPersistentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> getStep(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                           @PathVariable("stepPersistentId") String stepPersistentId,
                                           @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                           @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(stepService.getLatestStep(workflowPersistentId, stepPersistentId, draft, approved));
    }

    @GetMapping(path = "/{workflowPersistentId}/steps/{stepPersistentId}/versions/{stepVersionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> getStepVersion(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                  @PathVariable("stepPersistentId") String stepPersistentId,
                                                  @PathVariable("stepVersionId") long stepVersionId) {

        return ResponseEntity.ok(stepService.getStepVersion(workflowPersistentId, stepPersistentId, stepVersionId));
    }

    @PostMapping(
            path = "/{workflowPersistentId}/steps",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<StepDto> createStep(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                              @Parameter(
                                                      description = "Created step",
                                                      required = true,
                                                      schema = @Schema(implementation = StepCore.class)) @RequestBody StepCore newStep,
                                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(stepService.createStep(workflowPersistentId, newStep, draft));
    }

    @PostMapping(
            path = "/{workflowPersistentId}/steps/{stepPersistentId}/steps",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<StepDto> createSubstep(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                 @PathVariable("stepPersistentId") String stepPersistentId,
                                                 @Parameter(
                                                         description = "Created substep",
                                                         required = true,
                                                         schema = @Schema(implementation = StepCore.class)) @RequestBody StepCore newStep,
                                                 @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(stepService.createSubStep(workflowPersistentId, stepPersistentId, newStep, draft));
    }

    @PutMapping(
            path = "/{workflowPersistentId}/steps/{stepPersistentId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<StepDto> updateStep(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                              @PathVariable("stepPersistentId") String stepPersistentId,
                                              @Parameter(
                                                      description = "Updated step",
                                                      required = true,
                                                      schema = @Schema(implementation = StepCore.class)) @RequestBody StepCore updatedStep,
                                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(stepService.updateStep(workflowPersistentId, stepPersistentId, updatedStep, draft));
    }

    @PutMapping(
            path = "/{workflowPersistentId}/steps/{stepPersistentId}/versions/{stepVersionId}/revert",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<StepDto> revertStep(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                              @PathVariable("stepPersistentId") String stepPersistentId,
                                              @PathVariable("stepVersionId") long stepVersionId) {

        return ResponseEntity.ok(stepService.revertStep(workflowPersistentId, stepPersistentId, stepVersionId));
    }

    @DeleteMapping("/{workflowPersistentId}/steps/{stepPersistentId}")
    public void deleteStep(@PathVariable("workflowPersistentId") String workflowPersistentId,
                           @PathVariable("stepPersistentId") String stepPersistentId,
                           @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        stepService.deleteStep(workflowPersistentId, stepPersistentId, draft);
    }

    @PostMapping(path = "/{workflowPersistentId}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> publishWorkflow(@PathVariable("workflowPersistentId") String workflowPersistentId) {
        WorkflowDto workflow = workflowService.commitDraftWorkflow(workflowPersistentId);
        return ResponseEntity.ok(workflow);
    }

    @GetMapping(path = "/{workflowPersistentId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getWorkflowHistory(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                                    @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                    @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(workflowService.getWorkflowVersions(workflowPersistentId, draft, approved));
    }

    @GetMapping(path = "/{workflowPersistentId}/steps/{stepPersistentId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getStepHistory(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                                @PathVariable("stepPersistentId") String stepPersistentId,
                                                                @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(stepService.getStepVersions(workflowPersistentId, stepPersistentId, draft, approved));
    }

    @GetMapping(path = "/{workflowPersistentId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("workflowPersistentId") String workflowPersistentId) {

        return ResponseEntity.ok(workflowService.getInformationContributors(workflowPersistentId));
    }

    @GetMapping(path = "/{workflowPersistentId}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributorsForVersion(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                                              @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(workflowService.getInformationContributors(workflowPersistentId, versionId));
    }

    @GetMapping(path = "/{workflowPersistentId}/steps/{stepPersistentId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getStepInformationContributors(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                                        @PathVariable("stepPersistentId") String stepPersistentId) {

        return ResponseEntity.ok(stepService.getInformationContributors(workflowPersistentId, stepPersistentId));
    }

    @GetMapping(path = "/{workflowPersistentId}/steps/{stepPersistentId}/versions/{stepVersionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getStepInformationContributorsForVersion(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                                                  @PathVariable("stepPersistentId") String stepPersistentId,
                                                                                  @PathVariable("stepVersionId") long stepVersionId) {

        return ResponseEntity.ok(stepService.getInformationContributors(workflowPersistentId, stepPersistentId, stepVersionId));
    }

    @GetMapping(path = "/{workflowPersistentId}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> getMerge(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                @RequestParam List<String> with) {
        return ResponseEntity.ok(workflowService.getMerge(workflowPersistentId, with));
    }

    @PostMapping(path = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> merge(@RequestParam List<String> with,
                                             @Parameter(
                                                     description = "Merged workflow",
                                                     required = true,
                                                     schema = @Schema(implementation = WorkflowCore.class)) @RequestBody WorkflowCore mergeWorkflow) {
        return ResponseEntity.ok(workflowService.merge(mergeWorkflow, with));
    }

    @GetMapping(path = "{workflowPersistentId}/steps/{stepPersistentId}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> getMergeSteps(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                 @PathVariable("stepPersistentId") String stepPersistentId,
                                                 @RequestParam List<String> with) {
        return ResponseEntity.ok(stepService.getMerge(stepPersistentId, with));
    }

    @PostMapping(path = "{workflowPersistentId}/steps/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> mergeSteps(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                              @RequestParam List<String> with,
                                              @Parameter(
                                                      description = "Merged step",
                                                      required = true,
                                                      schema = @Schema(implementation = StepCore.class)) @RequestBody StepCore mergeStep) {
        return ResponseEntity.ok(stepService.merge(workflowPersistentId, mergeStep, with));
    }

    @GetMapping(path = "/{workflowPersistentId}/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SourceDto>> getSources(@PathVariable("workflowPersistentId") String workflowPersistentId) {

        return ResponseEntity.ok(workflowService.getSources(workflowPersistentId));
    }

    @GetMapping(path = "/{workflowPersistentId}/steps/{stepPersistentId}/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SourceDto>> getStepSources(@PathVariable("workflowPersistentId") String workflowPersistentId,
                                                          @PathVariable("stepPersistentId") String stepPersistentId) {
        return ResponseEntity.ok(stepService.getSources(workflowPersistentId, stepPersistentId));
    }

}
