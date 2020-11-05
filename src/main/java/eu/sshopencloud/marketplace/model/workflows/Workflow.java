package eu.sshopencloud.marketplace.model.workflows;

import eu.sshopencloud.marketplace.model.items.Item;
import lombok.*;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name = "workflows")
@Data
@ToString(callSuper = true, exclude = { "stepsTree", "allSteps" })
@EqualsAndHashCode(callSuper = true, exclude = { "stepsTree", "allSteps" })
public class Workflow extends Item {

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "steps_tree_id", nullable = false)
    private StepsTree stepsTree;

    @OneToMany(mappedBy = "workflow", fetch = FetchType.LAZY)
    // For the data loading optimization purposes only
    private List<StepsTree> allSteps;


    public Workflow() {
        super();
        this.stepsTree = StepsTree.makeRoot();
    }

    public Workflow(Workflow baseWorkflow) {
        super(baseWorkflow);
        this.stepsTree = StepsTree.newVersion(baseWorkflow.gatherSteps());
    }

    private Workflow(StepsTree stepsTree) {
        super();
        this.stepsTree = stepsTree;
    }

    public StepsTree gatherSteps() {
        // Invoke size method to force steps fetch
        int prefetchSize = allSteps.size();
        return stepsTree;
    }

    public static Workflow fromWorkflowSteps(Workflow workflow) {
        StepsTree stepsTree = StepsTree.newVersion(workflow.gatherSteps());
        return new Workflow(stepsTree);
    }
}
