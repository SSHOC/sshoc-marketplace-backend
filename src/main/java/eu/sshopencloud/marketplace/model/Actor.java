package eu.sshopencloud.marketplace.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.net.URL;

@Entity
public class Actor implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private URL externalIdentifier;
    private String name;

    public URL getExternalIdentifier() {
        return externalIdentifier;
    }

    public void setExternalIdentifier(URL externalIdentifier) {
        this.externalIdentifier = externalIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
