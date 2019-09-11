package eu.sshopencloud.marketplace.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Yoann on 30.08.19.
 */

@Entity
public class Tool extends MarketplaceEntity implements Serializable {
    @OneToMany
    private List<Property> inputProperties;
    @OneToMany
    private List<Property> outputProperties;

    public Tool(final String title) {
        super(title);
    }

    public List<Property> getInputProperties() {
        return inputProperties;
    }

    public void setInputProperties(List<Property> inputProperties) {
        this.inputProperties = inputProperties;
    }

    public List<Property> getOutputProperties() {
        return outputProperties;
    }

    public void setOutputProperties(List<Property> outputProperties) {
        this.outputProperties = outputProperties;
    }
}