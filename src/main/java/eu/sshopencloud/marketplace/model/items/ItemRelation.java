package eu.sshopencloud.marketplace.model.items;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "item_relations")
@Data
@EqualsAndHashCode(exclude = "inverseOf")
@NoArgsConstructor
public class ItemRelation {

    @Id
    private String code;

    @Basic
    @Column(nullable = false)
    private Integer ord;

    @Basic
    @Column(nullable = false)
    private String label;

    @OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.REMOVE, CascadeType.REFRESH }, orphanRemoval = true)
    @JoinColumn(foreignKey = @ForeignKey(name="item_relation_inverse_of_code_fk"))
    private ItemRelation inverseOf;

}
