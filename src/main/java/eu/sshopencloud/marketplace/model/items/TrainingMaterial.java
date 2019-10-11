package eu.sshopencloud.marketplace.model.items;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "training_materials")
@Data
@NoArgsConstructor
public class TrainingMaterial extends  DigitalObject {

    @Basic
    @Column(nullable = false)
    private TrainingMaterialType trainingMaterialType;

}
