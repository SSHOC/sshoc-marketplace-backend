package eu.sshopencloud.marketplace.model;

import java.io.Serializable;
import java.util.Date;

@Deprecated
@javax.persistence.Entity
public class DigitalObject extends MarketplaceEntity implements Serializable {

    private Date dateLastUpdated;
    private Date dateCreated;
//    private Extent extent;

    public DigitalObject(String title) {
        super(title);
    }

    public Date getDateLastUpdated() {
        return dateLastUpdated;
    }

    public void setDateLastUpdated(Date dateLastUpdated) {
        this.dateLastUpdated = dateLastUpdated;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
