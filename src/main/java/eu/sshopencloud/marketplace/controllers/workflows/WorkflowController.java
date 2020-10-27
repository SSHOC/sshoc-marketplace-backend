package eu.sshopencloud.marketplace.controllers.workflows;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.dto.workflows.StepDto;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowCore;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowDto;
import eu.sshopencloud.marketplace.dto.workflows.PaginatedWorkflows;
import eu.sshopencloud.marketplace.services.items.StepService;
import eu.sshopencloud.marketplace.services.items.WorkflowService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final PageCoordsValidator pageCoordsValidator;
    private final WorkflowService workflowService;
    private final StepService stepService;


    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedWorkflows> getWorkflows(@RequestParam(value = "page", required = false) Integer page,
                                                           @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(workflowService.getWorkflows(pageCoordsValidator.validate(page, perpage)));
    }

    @GetMapping(path = "/{workflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> getWorkflow(@PathVariable("workflowId") String workflowId,
                                                   @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(workflowService.getLatestWorkflow(workflowId, draft));
    }

    @GetMapping(path = "/{workflowId}/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> getWorkflow(@PathVariable("workflowId") String workflowId,
                                                   @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(workflowService.getWorkflowVersion(workflowId, versionId));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> createWorkflow(@RequestBody WorkflowCore newWorkflow,
                                                      @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {
        return ResponseEntity.ok(workflowService.createWorkflow(newWorkflow, draft));
    }

    @PutMapping(path = "/{workflowId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> updateWorkflow(@PathVariable("workflowId") String workflowId,
                                                      @RequestBody WorkflowCore updatedWorkflow,
                                                      @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(workflowService.updateWorkflow(workflowId, updatedWorkflow, draft));
    }

    @PutMapping(path = "/{id}/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> revertWorkflow(@PathVariable("id") String id, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(workflowService.revertWorkflow(id, versionId));
    }

    @DeleteMapping(path = "/{workflowId}")
    public void deleteWorkflow(@PathVariable("workflowId") String workflowId) {
        workflowService.deleteWorkflow(workflowId);
    }

    @GetMapping(path = "/{workflowId}/steps/{stepId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> getStep(@PathVariable("workflowId") String workflowId, @PathVariable("stepId") String stepId,
                                           @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(stepService.getLatestStep(workflowId, stepId, draft));
    }

    @GetMapping(path = "/{workflowId}/steps/{stepId}/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> getStep(@PathVariable("workflowId") String workflowId,
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
                                              @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

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
                                                 @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(stepService.createSubstep(workflowId, stepId, newStep, draft));
    }

    @PutMapping(
            path = "/{workflowId}/steps/{stepId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<StepDto> updateStep(@PathVariable("workflowId") String workflowId,
                                              @PathVariable("stepId") String stepId,
                                              @RequestBody StepCore updatedStep,
                                              @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(stepService.updateStep(workflowId, stepId, updatedStep, draft));
    }

    @PutMapping(
            path = "/{workflowId}/steps/{stepId}/{versionId}/revert",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<StepDto> revertStep(@PathVariable("workflowId") String workflowId,
                                              @PathVariable("stepId") String stepId,
                                              @PathVariable long versionId) {

        return ResponseEntity.ok(stepService.revertStep(workflowId, stepId, versionId));
    }

    @DeleteMapping(path = "/{workflowId}/steps/{stepId}")
    public void deleteStep(@PathVariable("workflowId") String workflowId, @PathVariable("stepId") String stepId,
                           @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        stepService.deleteStep(workflowId, stepId, draft);
    }
}
