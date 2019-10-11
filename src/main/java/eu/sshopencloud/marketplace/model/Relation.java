package eu.sshopencloud.marketplace.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Deprecated
@javax.persistence.Entity
public class Relation implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private MarketplaceEntity marketplaceEntity;
    private String type;

    public MarketplaceEntity getMarketplaceEntity() {
        return marketplaceEntity;
    }

    public void setMarketplaceEntity(MarketplaceEntity marketplaceEntity) {
        this.marketplaceEntity = marketplaceEntity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
