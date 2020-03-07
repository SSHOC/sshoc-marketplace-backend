package eu.sshopencloud.marketplace.model.activities;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "activities_parthoods")
@Data
@NoArgsConstructor
public class ActivityParthood implements Serializable {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(name="parent_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name="activities_parthoods_parent_id_fk"))
    private Activity parent;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(name="child_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name="activities_parthoods_object_id_fk"))
    private Activity child;

    @Basic
    @Column(nullable = false)
    private Integer ord;

}
