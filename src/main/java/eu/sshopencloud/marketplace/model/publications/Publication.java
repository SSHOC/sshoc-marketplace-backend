package eu.sshopencloud.marketplace.model.publications;

import eu.sshopencloud.marketplace.model.items.DigitalObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "publications")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Publication extends DigitalObject {

}
