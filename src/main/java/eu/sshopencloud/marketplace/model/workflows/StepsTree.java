package eu.sshopencloud.marketplace.model.workflows;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Composite class representing workflow steps tree or steps subtree
 * See: {@see <a href="https://refactoring.guru/design-patterns/composite"/>}
 */
@Entity
@Table(name = "steps_trees")
@Data
public class StepsTree {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "steps_tree_id_generator")
    @SequenceGenerator(name = "steps_tree_id_generator", sequenceName = "steps_tree_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @ManyToOne
    @JoinColumn(name = "step_id")
    private Step step;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private StepsTree parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "ord")
    private List<StepsTree> subTrees;

    @Column(name = "ord", nullable = false)
    private int ord;

    @Column(name = "is_root", nullable = false)
    private boolean root;


    public StepsTree(Step step, StepsTree parent, int pos) {
        this(false);

        this.workflow = parent.getWorkflow();
        this.step = step;
        this.parent = parent;
        this.ord = pos;
    }

    // Use static factory methods below
    protected StepsTree() {
        this.subTrees = new ArrayList<>();
        this.ord = 0;
    }

    private StepsTree(boolean root) {
        this();
        this.root = root;
    }

    private StepsTree(StepsTree stepsTree, StepsTree parent) {
        this.workflow = stepsTree.getWorkflow();
        this.step = stepsTree.getStep();
        this.parent = parent;
        this.subTrees = stepsTree.getSubTrees()
                .stream()
                .map(subTree -> new StepsTree(subTree, this))
                .collect(Collectors.toList());

        this.ord = stepsTree.getOrd();
        this.root = stepsTree.isRoot();
    }

    public void appendStep(Step step) {
        beforeStepAdd(step);

        StepsTree subtree = new StepsTree(step, this, subTrees.size());
        subTrees.add(subtree);
    }

    public void addStep(Step step, int stepNo) {
        beforeStepAdd(step);

        if (stepNo <= 0 || stepNo > subTrees.size())
            throw new IndexOutOfBoundsException(String.format("Invalid step number: %d", stepNo));

        StepsTree subtree = new StepsTree(step, this, stepNo);
        subTrees.add(stepNo - 1, subtree);

        renumberSubTrees();
    }

    private void beforeStepAdd(Step step) {
        removeStep(step);
    }

    public void removeStep(Step step) {
        subTrees.removeIf(st -> st.getStep().getId().equals(step.getId()));
        renumberSubTrees();
    }

    private void renumberSubTrees() {
        int i = 0;

        for (StepsTree subTree : subTrees)
            subTree.setOrd(i++);
    }

    public List<StepsTree> getSubTrees() {
        return Collections.unmodifiableList(subTrees);
    }

    public void visit(StepsTreeVisitor visitor) {
        if (!isRoot())
            visitor.onNextStep(step);

        for (StepsTree subtree : subTrees) {
            subtree.visit(visitor);
            visitor.onBackToParent();
        }
    }


    public static StepsTree newVersion(StepsTree tree) {
        return new StepsTree(tree, null);
    }

    public static StepsTree makeRoot() {
        return new StepsTree(true);
    }
}
