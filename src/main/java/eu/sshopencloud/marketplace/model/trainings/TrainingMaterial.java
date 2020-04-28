package eu.sshopencloud.marketplace.model.trainings;

import eu.sshopencloud.marketplace.model.items.DigitalObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "training_materials")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TrainingMaterial extends DigitalObject {
    
}
