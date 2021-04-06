package eu.sshopencloud.marketplace.domain.common;

public interface OrderableEntity<Id> {

    Id getId();

    int getOrd();
    void setOrd(int ord);
}
