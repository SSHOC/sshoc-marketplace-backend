package eu.sshopencloud.marketplace.model.workflows;

import eu.sshopencloud.marketplace.model.items.Item;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "workflows")
@Data
@EqualsAndHashCode(callSuper = true)
public class Workflow extends Item implements StepParent {

    @OneToMany(mappedBy = "workflow", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "ord")
    private List<Step> steps;


    public Workflow() {
        super();
        this.steps = new ArrayList<>();
    }

    @Override
    public void appendStep(@NonNull Step step) {
        beforeStepAdd(step);
        steps.add(step);
    }

    @Override
    public void addStep(@NonNull Step step, int stepNo) {
        if (stepNo <= 0 || stepNo > steps.size())
            throw new IllegalArgumentException(String.format("Invalid step number: %d", stepNo));

        beforeStepAdd(step);
        steps.add(stepNo - 1, step);
    }

    private void beforeStepAdd(Step step) {
        steps.removeIf(s -> s.getId().equals(step.getId()));
        step.setWorkflow(this);
    }

    @Override
    public List<Step> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    @Override
    public Workflow getRootWorkflow() {
        return this;
    }
}
