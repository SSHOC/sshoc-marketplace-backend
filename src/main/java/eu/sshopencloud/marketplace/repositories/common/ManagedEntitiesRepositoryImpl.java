package eu.sshopencloud.marketplace.repositories.common;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;


@RequiredArgsConstructor
public class ManagedEntitiesRepositoryImpl<T> implements ManagedEntitiesRepository<T> {

    private final EntityManager em;

    @Override
    @Transactional
    public void refresh(T entity) {
        em.refresh(entity);
    }
}
