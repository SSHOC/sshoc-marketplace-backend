package eu.sshopencloud.marketplace.model;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.List;

@javax.persistence.Entity
public abstract class MarketplaceEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(nullable = false)
    private String label;
    private String description;
    @OneToMany
    private List<Actor> contributors;
    @OneToMany
    private List<Property> properties;
    @OneToMany
    private List<Relation> relatedItems;
    private URL accessibleAt;
    private URL license;
    private MarketplaceUser informationContributor;
    private Date lastInfoUpdate;
    @OneToMany
    private List<Comment> comments;
    @OneToMany
    private List<MarketplaceEntity> revisions;


    public MarketplaceEntity(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastInfoUpdate() {
        return lastInfoUpdate;
    }

    public void setLastInfoUpdate(Date lastInfoUpdate) {
        this.lastInfoUpdate = lastInfoUpdate;
    }

    public URL getAccessibleAt() {
        return accessibleAt;
    }

    public void setAccessibleAt(URL accessibleAt) {
        this.accessibleAt = accessibleAt;
    }

    public URL getLicense() {
        return license;
    }

    public void setLicense(URL license) {
        this.license = license;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public MarketplaceUser getInformationContributor() {
        return informationContributor;
    }

    public void setInformationContributor(MarketplaceUser informationContributor) {
        this.informationContributor = informationContributor;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<MarketplaceEntity> getRevisions() {
        return revisions;
    }

    public void setRevisions(List<MarketplaceEntity> revisions) {
        this.revisions = revisions;
    }

    public List<Actor> getContributors() {
        return contributors;
    }

    public void setContributors(List<Actor> contributors) {
        this.contributors = contributors;
    }
    
    public List<Relation> getRelatedItems() {
        return relatedItems;
    }

    public void setRelatedItems(List<Relation> relatedItems) {
        this.relatedItems = relatedItems;
    }
}
