package eu.sshopencloud.marketplace.repositories.common;

public interface ManagedEntitiesRepository<T> {
    void refresh(T entity);
}
