package eu.sshopencloud.marketplace.model.licenses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "license_types")
@Data
@NoArgsConstructor
public class LicenseType {

    @Id
    private String code;

    @Basic
    @Column(nullable = false)
    @JsonIgnore
    private Integer ord;

    @Basic
    @Column(nullable = false)
    private String label;

}
