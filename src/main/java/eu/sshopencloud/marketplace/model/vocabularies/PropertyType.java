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

    /* This relation is managed from the PropertyTypeVocabulary class in order to easier attach / detach vocabularies. */
    @Transient
    private List<VocabularyInline> allowedVocabularies;

}
