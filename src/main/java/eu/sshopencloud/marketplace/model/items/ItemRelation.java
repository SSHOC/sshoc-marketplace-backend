package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "item_relations")
@Data
@NoArgsConstructor
public class ItemRelation {

    @Id
    private String code;

    @Basic
    @Column(nullable = false)
    @JsonIgnore
    private Integer ord;

    @Basic
    @Column(nullable = false)
    private String label;

    @OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.REMOVE, CascadeType.REFRESH }, orphanRemoval = true)
    @JoinColumn(foreignKey = @ForeignKey(name="item_relation_inverse_of_code_fk"))
    @JsonIgnore
    private ItemRelation inverseOf;

}
