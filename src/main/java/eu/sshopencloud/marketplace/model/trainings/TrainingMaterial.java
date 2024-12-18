package eu.sshopencloud.marketplace.model.trainings;

import eu.sshopencloud.marketplace.model.items.DigitalObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;


@Entity
@Table(name = "training_materials")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TrainingMaterial extends DigitalObject {

    public TrainingMaterial(TrainingMaterial trainingMaterial) {
        super(trainingMaterial);
    }
}
