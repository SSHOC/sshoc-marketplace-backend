package eu.sshopencloud.marketplace.model.datasets;

import eu.sshopencloud.marketplace.model.items.DigitalObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "datasets")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Dataset extends DigitalObject {

}
