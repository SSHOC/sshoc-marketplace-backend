package eu.sshopencloud.marketplace.model.workflows;

import eu.sshopencloud.marketplace.model.items.Item;
import lombok.*;

import javax.persistence.*;


@Entity
@Table(name = "steps")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Step extends Item {
}
