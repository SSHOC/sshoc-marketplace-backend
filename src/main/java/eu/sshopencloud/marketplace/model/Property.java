package eu.sshopencloud.marketplace.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Property implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private PropertyType key;
    private String value;
    private Concept concept;

    public Property() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public PropertyType getKey() {
        return key;
    }

    public void setKey(PropertyType key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
}
