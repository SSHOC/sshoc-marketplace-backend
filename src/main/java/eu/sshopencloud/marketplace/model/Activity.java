package eu.sshopencloud.marketplace.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
public class Activity extends MarketplaceEntity implements Serializable {
    @OneToMany
    private List<MarketplaceEntity> inputEntities;
    @OneToMany
    private List<MarketplaceEntity> outputEntities;
    @OneToMany
    private List<Property> inputProperties;
    @OneToMany
    private List<Property> outputProperties;
    @OneToMany
    private List<Tool> usedTools;

    public Activity(String label) {
        super(label);
    }

    public List<MarketplaceEntity> getInputEntities() {
        return inputEntities;
    }

    public void setInputEntities(List<MarketplaceEntity> inputEntities) {
        this.inputEntities = inputEntities;
    }

    public List<MarketplaceEntity> getOutputEntities() {
        return outputEntities;
    }

    public void setOutputEntities(List<MarketplaceEntity> outputEntities) {
        this.outputEntities = outputEntities;
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

    public List<Tool> getUsedTools() {
        return usedTools;
    }

    public void setUsedTools(List<Tool> usedTools) {
        this.usedTools = usedTools;
    }
}
