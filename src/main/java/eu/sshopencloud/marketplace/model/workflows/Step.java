package eu.sshopencloud.marketplace.model.workflows;


import eu.sshopencloud.marketplace.model.items.Item;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "steps")
@Data
@ToString(callSuper = true, exclude = {"workflow", "step"})
@EqualsAndHashCode(callSuper = true, exclude = {"workflow", "step"})
@NoArgsConstructor
public class Step extends Item {

    @ManyToOne(optional = true, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(foreignKey = @ForeignKey(name="step_workflow_id_fk"))
    private Workflow workflow;

    @ManyToOne(optional = true, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(foreignKey = @ForeignKey(name="step_step_id_fk"))
    private Step step;

    @OneToMany(mappedBy = "step", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "ord")
    private List<Step> substeps;

}
