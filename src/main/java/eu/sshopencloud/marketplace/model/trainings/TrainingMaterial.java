package eu.sshopencloud.marketplace.model.trainings;

import eu.sshopencloud.marketplace.model.items.DigitalObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "training_materials")
@Data
@NoArgsConstructor
public class TrainingMaterial extends DigitalObject {

    @ManyToOne(optional =  false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    private TrainingMaterialType trainingMaterialType;

}
