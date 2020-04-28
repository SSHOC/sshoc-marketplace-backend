package eu.sshopencloud.marketplace.model.workflows;

import eu.sshopencloud.marketplace.model.items.Item;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "workflows")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Workflow extends Item {

    @OneToMany(mappedBy = "workflow", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "ord")
    private List<Step> steps;


}
