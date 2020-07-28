package eu.sshopencloud.marketplace.model.workflows;

import java.util.List;

public interface StepParent {

    List<Step> getSteps();

    void appendStep(Step step);
    void addStep(Step step, int stepNo);

    Workflow getRootWorkflow();
}
