package eu.sshopencloud.marketplace.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Deprecated
@Entity
public class MarketplaceUser implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private String jsonPreferences;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJsonPreferences() {
        return jsonPreferences;
    }

    public void setJsonPreferences(String jsonPreferences) {
        this.jsonPreferences = jsonPreferences;
    }
}
