package eu.sshopencloud.marketplace.controllers.workflows;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.workflows.*;
import eu.sshopencloud.marketplace.services.items.StepService;
import eu.sshopencloud.marketplace.services.items.WorkflowService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
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

    @GetMapping(path = "/{workflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> getWorkflow(@PathVariable("workflowId") String workflowId,
                                                   @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                   @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(workflowService.getLatestWorkflow(workflowId, draft, approved));
    }

    @GetMapping(path = "/{workflowId}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> getWorkflowVersion(@PathVariable("workflowId") String workflowId,
                                                          @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(workflowService.getWorkflowVersion(workflowId, versionId));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> createWorkflow(@RequestBody WorkflowCore newWorkflow,
                                                      @RequestParam(value = "draft", defaultValue = "false") boolean draft) {
        return ResponseEntity.ok(workflowService.createWorkflow(newWorkflow, draft));
    }

    @PutMapping(path = "/{workflowId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> updateWorkflow(@PathVariable("workflowId") String workflowId,
                                                      @RequestBody WorkflowCore updatedWorkflow,
                                                      @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(workflowService.updateWorkflow(workflowId, updatedWorkflow, draft));
    }

    @PutMapping(path = "/{id}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> revertWorkflow(@PathVariable("id") String id, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(workflowService.revertWorkflow(id, versionId));
    }

    @DeleteMapping(path = "/{workflowId}")
    public void deleteWorkflow(@PathVariable("workflowId") String workflowId,
                               @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        workflowService.deleteWorkflow(workflowId, draft);
    }

    @GetMapping(path = "/{workflowId}/steps/{stepId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> getStep(@PathVariable("workflowId") String workflowId,
                                           @PathVariable("stepId") String stepId,
                                           @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                           @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(stepService.getLatestStep(workflowId, stepId, draft, approved));
    }

    @GetMapping(path = "/{workflowId}/steps/{stepId}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> getStepVersion(@PathVariable("workflowId") String workflowId,
                                                  @PathVariable("stepId") String stepId,
                                                  @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(stepService.getStepVersion(workflowId, stepId, versionId));
    }

    @PostMapping(
            path = "/{workflowId}/steps",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<StepDto> createStep(@PathVariable("workflowId") String workflowId, @RequestBody StepCore newStep,
                                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(stepService.createStep(workflowId, newStep, draft));
    }

    @PostMapping(
            path = "/{workflowId}/steps/{stepId}/steps",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<StepDto> createSubstep(@PathVariable("workflowId") String workflowId,
                                                 @PathVariable("stepId") String stepId,
                                                 @RequestBody StepCore newStep,
                                                 @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(stepService.createSubStep(workflowId, stepId, newStep, draft));
    }

    @PutMapping(
            path = "/{workflowId}/steps/{stepId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<StepDto> updateStep(@PathVariable("workflowId") String workflowId,
                                              @PathVariable("stepId") String stepId,
                                              @RequestBody StepCore updatedStep,
                                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(stepService.updateStep(workflowId, stepId, updatedStep, draft));
    }

    @PutMapping(
            path = "/{workflowId}/steps/{stepId}/versions/{versionId}/revert",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<StepDto> revertStep(@PathVariable("workflowId") String workflowId,
                                              @PathVariable("stepId") String stepId,
                                              @PathVariable long versionId) {

        return ResponseEntity.ok(stepService.revertStep(workflowId, stepId, versionId));
    }

    @DeleteMapping("/{workflowId}/steps/{stepId}")
    public void deleteStep(@PathVariable("workflowId") String workflowId, @PathVariable("stepId") String stepId,
                           @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        stepService.deleteStep(workflowId, stepId, draft);
    }

    @PostMapping(path = "/{workflowId}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> publishWorkflow(@PathVariable("workflowId") String workflowId) {
        WorkflowDto workflow = workflowService.commitDraftWorkflow(workflowId);
        return ResponseEntity.ok(workflow);
    }

    @GetMapping(path = "/{workflowId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getWorkflowHistory(@PathVariable("workflowId") String id,
                                                                    @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                    @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(workflowService.getWorkflowVersions(id, draft, approved));
    }

    @GetMapping(path = "/{workflowId}/steps/{stepId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getStepHistory(@PathVariable("workflowId") String workflowId, @PathVariable("stepId") String stepId,
                                                                @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(stepService.getStepVersions(workflowId, stepId, draft, approved));
    }

    @GetMapping(path = "/{workflowId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("workflowId") String id) {

        return ResponseEntity.ok(workflowService.getInformationContributors(id));
    }

    @GetMapping(path = "/{workflowId}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributorsForVersion(@PathVariable("workflowId") String id, @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(workflowService.getInformationContributors(id, versionId));
    }

    @GetMapping(path = "/{workflowId}/steps/{stepId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getStepInformationContributors(@PathVariable("workflowId") String id, @PathVariable("stepId") String stepId) {

        return ResponseEntity.ok(stepService.getInformationContributors(id, stepId));
    }

    @GetMapping(path = "/{workflowId}/steps/{stepId}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getStepInformationContributorsForVersion(@PathVariable("workflowId") String id, @PathVariable("stepId") String stepId, @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(stepService.getInformationContributors(id, stepId, versionId));
    }

    @GetMapping(path = "/{id}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> getMerge(@PathVariable("id") String id,
                                                @RequestParam List<String> with) {
        return ResponseEntity.ok(workflowService.getMerge(id, with));
    }

    @PostMapping(path = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> merge(@RequestParam List<String> with,
                                             @RequestBody WorkflowCore mergeWorkflow) {
        return ResponseEntity.ok(workflowService.merge(mergeWorkflow, with));
    }

    @GetMapping(path = "{workflowId}/steps/{id}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> getMergeSteps(@PathVariable("workflowId") String workflowId,
                                                 @PathVariable("id") String id,
                                                 @RequestParam List<String> with) {
        return ResponseEntity.ok(stepService.getMerge(id, with));
    }

    @PostMapping(path = "{workflowId}/steps/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> mergeSteps(@PathVariable("workflowId") String id,
                                              @RequestParam List<String> with,
                                              @RequestBody StepCore mergeStep) {
        return ResponseEntity.ok(stepService.merge(id, mergeStep, with));
    }

}
