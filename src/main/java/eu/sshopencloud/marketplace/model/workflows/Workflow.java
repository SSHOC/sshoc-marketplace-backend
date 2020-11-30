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

    @OneToMany(mappedBy = "workflow", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    // For the data loading optimization purposes only
    private List<StepsTree> allSteps;


    public Workflow() {
        super();
        this.stepsTree = StepsTree.makeRoot(this);
    }

    public Workflow(Workflow baseWorkflow) {
        super(baseWorkflow);
        this.stepsTree = StepsTree.newVersion(this, baseWorkflow.gatherSteps());
    }

    public StepsTree gatherSteps() {
        // Invoke size method to force steps fetch
        if (allSteps != null) {
            int prefetchSize = allSteps.size();
        }

        return stepsTree;
    }
}
