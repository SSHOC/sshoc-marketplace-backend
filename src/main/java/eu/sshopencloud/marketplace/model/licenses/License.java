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

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinTable(name = "licenses_license_types", joinColumns = @JoinColumn(name = "license_code", referencedColumnName = "code"),
            inverseJoinColumns = @JoinColumn(name = "type_code", referencedColumnName = "code"))
    @OrderColumn(name = "ord")
    private List<LicenseType> types;

    @Basic
    @Column(nullable = false)
    private String accessibleAt;

}
