package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "tools")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
public abstract class Tool extends Item {

    @Basic
    @Column(nullable = false)
    private ToolType toolType;

    @ManyToOne(optional = true, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    private EasyOfUse easyOfUse;

    // TODO properties

}
