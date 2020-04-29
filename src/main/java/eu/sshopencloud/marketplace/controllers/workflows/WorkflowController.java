package eu.sshopencloud.marketplace.controllers.workflows;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.dto.workflows.StepDto;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowCore;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowDto;
import eu.sshopencloud.marketplace.dto.workflows.PaginatedWorkflows;
import eu.sshopencloud.marketplace.services.workflows.StepService;
import eu.sshopencloud.marketplace.services.workflows.WorkflowService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkflowController {

    private final PageCoordsValidator pageCoordsValidator;

    private final WorkflowService workflowService;

    private final StepService stepService;

    @GetMapping(path = "/workflows", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedWorkflows> getWorkflows(@RequestParam(value = "page", required = false) Integer page,
                                                           @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(workflowService.getWorkflows(pageCoordsValidator.validate(page, perpage)));
    }

    @GetMapping(path = "/workflows/{workflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> getWorkflow(@PathVariable("workflowId") long workflowId) {
        return ResponseEntity.ok(workflowService.getWorkflow(workflowId));
    }

    @PostMapping(path = "/workflows", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> createWorkflow(@RequestBody WorkflowCore newWorkflow) {
        return ResponseEntity.ok(workflowService.createWorkflow(newWorkflow));
    }

    @PutMapping(path = "/workflows/{workflowId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowDto> updateWorkflow(@PathVariable("workflowId") long workflowId, @RequestBody WorkflowCore updatedWorkflow) {
        return ResponseEntity.ok(workflowService.updateWorkflow(workflowId, updatedWorkflow));
    }

    @DeleteMapping(path = "/workflows/{workflowId}")
    public void deleteWorkflow(@PathVariable("workflowId") long workflowId) {
        workflowService.deleteWorkflow(workflowId);
    }

    @GetMapping(path = "/workflows/{workflowId}/steps/{stepId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> getStep(@PathVariable("workflowId") long workflowId, @PathVariable("stepId") long stepId) {
        return ResponseEntity.ok(stepService.getStep(workflowId, stepId));
    }

    @PostMapping(path = "/workflows/{workflowId}/steps", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> createStep(@PathVariable("workflowId") long workflowId, @RequestBody StepCore newStep) {
        return ResponseEntity.ok(stepService.createStep(workflowId, newStep));
    }

    @PostMapping(path = "/workflows/{workflowId}/steps/{stepId}/steps", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> createStep(@PathVariable("workflowId") long workflowId, @PathVariable("stepId") long stepId, @RequestBody StepCore newStep) {
        return ResponseEntity.ok(stepService.createStep(workflowId, stepId, newStep));
    }

    @PutMapping(path = "/workflows/{workflowId}/steps/{stepId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StepDto> updateStep(@PathVariable("workflowId") long workflowId, @PathVariable("stepId") long stepId, @RequestBody StepCore updatedStep) {
        return ResponseEntity.ok(stepService.updateStep(workflowId, stepId, updatedStep));
    }

    @DeleteMapping(path = "/workflows/{workflowId}/steps/{stepId}")
    public void deleteStep(@PathVariable("workflowId") long workflowId, @PathVariable("stepId") long stepId) {
        stepService.deleteStep(workflowId, stepId);
    }

}
