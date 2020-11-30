package eu.sshopencloud.marketplace.model.licenses;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "licenses")
@Data
@NoArgsConstructor
public class License {

    @Id
    private String code;

    @Basic
    @Column(nullable = false)
    private String label;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "licenses_license_types", joinColumns = @JoinColumn(name = "license_code", referencedColumnName = "code", foreignKey = @ForeignKey(name="licenses_license_types_license_code_fk")),
            inverseJoinColumns = @JoinColumn(name = "type_code", referencedColumnName = "code", foreignKey = @ForeignKey(name="licenses_license_types_type_code_fk")))
    @OrderColumn(name = "ord")
    private List<LicenseType> types;

    @Basic
    @Column(nullable = false)
    private String accessibleAt;

}
