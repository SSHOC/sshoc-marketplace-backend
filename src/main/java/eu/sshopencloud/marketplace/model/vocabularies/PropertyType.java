package eu.sshopencloud.marketplace.model.vocabularies;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.sshopencloud.marketplace.model.auth.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "property_types")
@Data
@NoArgsConstructor
public class PropertyType {

    @Id
    protected String code;

    @Basic
    @Column(nullable = false)
    @JsonIgnore
    protected Integer ord;

    @Basic
    @Column(nullable = false)
    private String label;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.REMOVE, CascadeType.REFRESH })
    @JoinTable(name = "property_types_allowed_vocabularies", joinColumns = @JoinColumn(name = "property_type_code", referencedColumnName = "code"),
            inverseJoinColumns = @JoinColumn(name = "vocabulary_id", referencedColumnName = "id"))
    @OrderColumn(name = "ord")
    private List<Vocabulary> allowedVocabularies;

}
