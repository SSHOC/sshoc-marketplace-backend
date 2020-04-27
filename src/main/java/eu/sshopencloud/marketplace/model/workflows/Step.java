package eu.sshopencloud.marketplace.model.workflows;


import eu.sshopencloud.marketplace.model.items.Item;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "steps")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Step extends Item {


}
