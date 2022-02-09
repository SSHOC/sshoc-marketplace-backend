package eu.sshopencloud.marketplace.model.workflows;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Composite class representing workflow steps tree or steps subtree
 * See: {@see <a href="https://refactoring.guru/design-patterns/composite"/>}
 */
@Entity
@Table(name = "steps_trees")
@Data
@ToString(exclude = {"workflow", "step", "parent", "subTrees"})
@EqualsAndHashCode(exclude = {"workflow", "step", "parent", "subTrees"})
public class StepsTree {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "steps_tree_id_generator")
    @SequenceGenerator(name = "steps_tree_id_generator", sequenceName = "steps_tree_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    private Workflow workflow;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "step_id")
    @Nullable
    private Step step;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @Nullable
    private StepsTree parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "ord")
    private List<StepsTree> subTrees;

//    @Column(name = "ord", nullable = false)
//    private int ord;

    @Column(name = "is_root", nullable = false)
    private boolean root;


    public StepsTree(Step step, StepsTree parent) {
        this(parent.getWorkflow(), false);

        this.step = step;
        this.parent = parent;
//        this.ord = pos;

//        parent.locateSubStep(step)
//                .ifPresent(prevStepTree -> {
//                    this.subTrees = new ArrayList<>(prevStepTree.getSubTrees());
//                    this.subTrees.forEach(subTree -> subTree.setParent(this));
//                });
    }

    // Use static factory methods below
    protected StepsTree() {
        this.subTrees = new ArrayList<>();
//        this.ord = 0;
    }

    private StepsTree(Workflow workflow, boolean root) {
        this();

        this.workflow = workflow;
        this.root = root;
    }

    private StepsTree(Workflow workflow, StepsTree baseStepsTree, StepsTree parent) {
        this.workflow = workflow;
        this.step = baseStepsTree.getStep();
        this.parent = parent;
        this.subTrees = baseStepsTree.getSubTrees()
                .stream()
                .map(subTree -> new StepsTree(workflow, subTree, this))
                .collect(Collectors.toList());

//        this.ord = baseStepsTree.getOrd();
        this.root = baseStepsTree.isRoot();
    }

    public void appendStep(Step step) {
        StepsTree subtree = newAddedTree(step);
        subTrees.add(subtree);

        removePreviousStep(step);
    }

    public void addStep(Step step, int stepNo) {
        if (isInvalidStepNo(stepNo))
            throw new IndexOutOfBoundsException(String.format("Invalid step number: %d/%d", stepNo, subTrees.size() + 1));

        int stepOrd = resolveOrd(stepNo);
        StepsTree subtree = newAddedTree(step);

        removePreviousStep(step);
        subTrees.add(stepOrd, subtree);

    }

    public void replaceStep(Step step, int stepNo, Step replacedStep) {
        if (isInvalidStepNo(stepNo))
            throw new IndexOutOfBoundsException(String.format("Invalid step number: %d/%d", stepNo, subTrees.size() + 1));

        int stepOrd = resolveOrd(stepNo);
        StepsTree subtree = newAddedTree(step);
        subTrees.add(stepOrd, subtree);

        removeStep(replacedStep);
    }

    private StepsTree newAddedTree(Step step) {
        return locateSubStep(step)
                .map(stepTree -> {
                    StepsTree tree = new StepsTree(workflow, stepTree, this);
                    tree.setStep(step);

                    return tree;
                })
                .orElseGet(() -> new StepsTree(step, this));
    }

    private int resolveOrd(int stepNo) {
        return stepNo - 1;
    }

    public boolean isInvalidStepNo(int stepNo) {
        return stepNo <= 0 || stepNo > subTrees.size() + 1;
    }

    public void replaceChildStep(Step step) {
        locateSubStep(step).ifPresent(st -> st.setStep(step));
    }

    private Optional<StepsTree> locateSubStep(Step step) {
        String persistentId = step.getVersionedItem().getPersistentId();

        return subTrees.stream()
                .filter(st -> st.getStep().getVersionedItem().getPersistentId().equals(persistentId))
                .findFirst();
    }

    //Eliza
    private void removePreviousStep(Step step) {
        String persistentId = step.getVersionedItem().getPersistentId();
        subTrees.removeIf(
                st -> st.getStep().getVersionedItem().getPersistentId().equals(persistentId)
                        && !st.getStep().getId().equals(step.getId())
        );

//        renumberSubTrees();
    }

    //Eliza
    public void removePreviousDraftStep(Step step) {
        String persistentId = step.getVersionedItem().getPersistentId();
        subTrees.removeIf(
                st -> st.getStep().getVersionedItem().getPersistentId().equals(persistentId)
                        && st.getStep().getId().equals(step.getId())
        );

    }

    public void removeStep(Step step) {
        String persistentId = step.getVersionedItem().getPersistentId();
        subTrees.removeIf(st -> st.getStep().getVersionedItem().getPersistentId().equals(persistentId));

//        renumberSubTrees();
    }

//    private void renumberSubTrees() {
//        int i = 0;
//
//        for (StepsTree subTree : subTrees) {
//            if (subTree.getOrd() != i)
//                subTree.setOrd(i);
//
//            i++;
//        }
//    }

    public List<StepsTree> getSubTrees() {
        return Collections.unmodifiableList(subTrees);
    }

    public void visit(StepsTreeVisitor visitor) {
        if (!isRoot())
            visitor.onNextStep(this);

        for (StepsTree subtree : subTrees) {
            subtree.visit(visitor);
            visitor.onBackToParent();
        }
    }


    public static StepsTree newVersion(Workflow newWorkflow, StepsTree tree) {
        return new StepsTree(newWorkflow, tree, null);
    }

    public static StepsTree makeRoot(Workflow workflow) {
        return new StepsTree(workflow, true);
    }
}
