package eu.sshopencloud.marketplace.model.workflows;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemFlag;
import lombok.*;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "workflows")
@Data
@ToString(callSuper = true, exclude = {"stepsTree", "allSteps"})
@EqualsAndHashCode(callSuper = true, exclude = {"stepsTree", "allSteps"})
public class Workflow extends Item {

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "steps_tree_id", nullable = false)
    private StepsTree stepsTree;

    @OneToMany(mappedBy = "workflow", fetch = FetchType.LAZY)
    // For the data loading optimization purposes only
    private List<StepsTree> allSteps;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    Set<ItemFlag> flags = new HashSet<>();

    public Workflow() {
        super();
        this.stepsTree = StepsTree.makeRoot(this);
    }

    public Workflow(Workflow baseWorkflow) {
        super(baseWorkflow);
        this.stepsTree = StepsTree.newVersion(this, baseWorkflow.gatherSteps());
        this.setFlags(new HashSet<>(baseWorkflow.getFlags()));
    }

    public StepsTree gatherSteps() {
        // Invoke size method to force steps fetch - rethink is it really needed?
        if (allSteps != null) {
            int prefetchSize = allSteps.size();
        }

        return stepsTree;
    }

    public int getSize() {
        return allSteps.size();
    }
}
