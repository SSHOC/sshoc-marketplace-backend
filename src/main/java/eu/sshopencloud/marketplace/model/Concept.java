package eu.sshopencloud.marketplace.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.net.URL;

@Deprecated
@Entity
public class Concept implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String preLabel;
    private String definition;
    private URL url;
    private Concept relatedConcept;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPreLabel() {
        return preLabel;
    }

    public void setPreLabel(String preLabel) {
        this.preLabel = preLabel;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Concept getRelatedConcept() {
        return relatedConcept;
    }

    public void setRelatedConcept(Concept relatedConcept) {
        this.relatedConcept = relatedConcept;
    }
}
