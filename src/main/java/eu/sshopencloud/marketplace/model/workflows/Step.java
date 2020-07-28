package eu.sshopencloud.marketplace.model.workflows;

import eu.sshopencloud.marketplace.model.items.Item;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "steps")
@Data
@ToString(callSuper = true, exclude = {"workflow", "step"})
@EqualsAndHashCode(callSuper = true, exclude = {"workflow", "step"})
public class Step extends Item implements StepParent {

    @ManyToOne(optional = true, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(foreignKey = @ForeignKey(name="step_workflow_id_fk"))
    private Workflow workflow;

    @ManyToOne(optional = true, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(foreignKey = @ForeignKey(name="step_step_id_fk"))
    private Step step;

    @OneToMany(mappedBy = "step", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "ord")
    private List<Step> substeps;


    public Step() {
        super();
        this.substeps = new ArrayList<>();
    }

    private boolean isSubStep() {
        return (workflow == null);
    }

    public StepParent getStepParent() {
        if (isSubStep())
            return step;

        return workflow;
    }

    public Workflow getRootWorkflow() {
        return getStepParent().getRootWorkflow();
    }

    @Override
    public List<Step> getSteps() {
        return Collections.unmodifiableList(substeps);
    }

    @Override
    public void appendStep(@NonNull Step step) {
        beforeStepAdd(step);
        substeps.add(step);
    }

    @Override
    public void addStep(@NonNull Step step, int stepNo) {
        if (stepNo <= 0 || stepNo > substeps.size())
            throw new IllegalArgumentException(String.format("Invalid step number: %d", stepNo));

        beforeStepAdd(step);
        substeps.add(stepNo - 1, step);
    }

    private void beforeStepAdd(Step step) {
        substeps.removeIf(s -> s.getId().equals(step.getId()));
        step.setStep(this);
    }
}
