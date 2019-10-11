package eu.sshopencloud.marketplace.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Deprecated
@Entity
public class PropertyType implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String label;
    @OneToMany
    private List<Vocabulary> allowedValues;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Vocabulary> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<Vocabulary> allowedValues) {
        this.allowedValues = allowedValues;
    }
}
