package eu.sshopencloud.marketplace.model.workflows;

public interface StepsTreeVisitor {
    void onNextStep(StepsTree stepsTree);
    void onBackToParent();
}
