package eu.sshopencloud.marketplace.model.trainings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "training_material_types")
@Data
@NoArgsConstructor
public class TrainingMaterialType {

    @Id
    protected String code;

    @Basic
    @Column(nullable = false)
    @JsonIgnore
    protected Integer ord;

    @Basic
    @Column(nullable = false)
    private String label;

}
