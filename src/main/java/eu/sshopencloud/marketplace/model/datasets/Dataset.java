package eu.sshopencloud.marketplace.model.datasets;

import eu.sshopencloud.marketplace.model.items.DigitalObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "datasets")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Dataset extends DigitalObject {

    public Dataset(Dataset baseDataset) {
        super(baseDataset);
    }
}
