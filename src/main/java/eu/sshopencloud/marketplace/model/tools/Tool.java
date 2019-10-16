package eu.sshopencloud.marketplace.model.tools;

import eu.sshopencloud.marketplace.model.items.Item;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "tools")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
public abstract class Tool extends Item {

    @ManyToOne(optional =  false, fetch = FetchType.EAGER, cascade = { CascadeType.REMOVE, CascadeType.REFRESH })
    private ToolType toolType;

    @ManyToOne(optional = true, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    private EaseOfUse easeOfUse;

    // TODO properties

}
