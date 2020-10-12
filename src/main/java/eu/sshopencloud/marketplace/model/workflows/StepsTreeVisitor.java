package eu.sshopencloud.marketplace.model.workflows;

public interface StepsTreeVisitor {
    void onNextStep(Step step);
    void onBackToParent();
}
