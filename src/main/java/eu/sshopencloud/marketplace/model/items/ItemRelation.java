package eu.sshopencloud.marketplace.model.items;

import eu.sshopencloud.marketplace.domain.common.OrderableEntity;
import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "item_relations")
@Data
@EqualsAndHashCode(exclude = "inverseOf")
@ToString(exclude = "inverseOf")
@NoArgsConstructor
public class ItemRelation implements OrderableEntity<String> {

    @Id
    private String code;

    @Basic
    @Column(nullable = false)
    private Integer ord;

    @Basic
    @Column(nullable = false)
    private String label;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name = "item_relation_inverse_of_code_fk"))
    private ItemRelation inverseOf;


    public ItemRelationId getItemRelationId() {
        return new ItemRelationId(code);
    }

    @Override
    public String getId() {
        return code;
    }

    @Override
    public void setOrd(int ord) {
        this.ord = ord;
    }

    public int getOrd(){
        return ord.intValue();
    }


}
