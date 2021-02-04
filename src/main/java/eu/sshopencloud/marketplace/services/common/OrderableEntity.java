package eu.sshopencloud.marketplace.services.common;

public interface OrderableEntity<Id> {

    Id getId();

    int getOrd();
    void setOrd(int ord);
}
