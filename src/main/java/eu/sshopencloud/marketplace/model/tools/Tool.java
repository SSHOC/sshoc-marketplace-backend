package eu.sshopencloud.marketplace.model.tools;

import eu.sshopencloud.marketplace.model.items.Item;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "tools")
@Data
@NoArgsConstructor
public class Tool extends Item {

}
