package eu.sshopencloud.marketplace.model.workflows;

import eu.sshopencloud.marketplace.model.items.Item;
import lombok.*;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name = "workflows")
@Data
@EqualsAndHashCode(callSuper = true)
public class Workflow extends Item {

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    private StepsTree steps;

    @OneToMany(mappedBy = "workflow", fetch = FetchType.LAZY)
    // For the data loading optimization purposes only
    private List<StepsTree> allSteps;


    public Workflow() {
        super();
        this.steps = StepsTree.makeRoot();
    }

    public Workflow(Workflow prevWorkflow) {
        super(prevWorkflow);
        this.steps = StepsTree.newVersion(prevWorkflow.getSteps());
    }

    private Workflow(StepsTree stepsTree) {
        super();
        this.steps = stepsTree;
    }

    public StepsTree gatherSteps() {
        // Invoke size method to force steps fetch
        int prefetchSize = allSteps.size();
        return steps;
    }

    public static Workflow fromWorkflowSteps(Workflow workflow) {
        StepsTree stepsTree = StepsTree.newVersion(workflow.getSteps());
        return new Workflow(stepsTree);
    }
}
