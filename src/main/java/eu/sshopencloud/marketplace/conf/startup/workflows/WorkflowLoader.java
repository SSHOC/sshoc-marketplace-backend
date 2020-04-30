package eu.sshopencloud.marketplace.conf.startup.workflows;

import eu.sshopencloud.marketplace.conf.startup.items.ItemLoader;
import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.repositories.workflows.WorkflowRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowLoader {

    private final ItemLoader itemLoader;

    private final WorkflowRepository workflowRepository;

    private  final IndexService indexService;

    public void createWorkflows(String profile, List<Workflow> newWorkflows) {
        for (Workflow newWorkflow: newWorkflows) {
            itemLoader.completeItemRelations(newWorkflow);
            for (Step step: newWorkflow.getSteps()) {
                step.setWorkflow(newWorkflow);
                completeSteps(step);
            }

            Workflow workflow = workflowRepository.save(newWorkflow);
            if (!profile.equals("prod")) {
                indexService.indexItem(workflow);
            }
        }
    }

    private void completeSteps(Step newStep) {
        itemLoader.completeItemRelations(newStep);
        if (newStep.getSubsteps() != null) {
            for (Step substep : newStep.getSubsteps()) {
                substep.setStep(newStep);
                completeSteps(substep);
            }
        }
    }

}
