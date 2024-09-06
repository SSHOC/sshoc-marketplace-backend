package eu.sshopencloud.marketplace.repositories.items;


import eu.sshopencloud.marketplace.model.items.ItemContributor;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;


@Repository
public class ItemContributorCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public ItemContributor findByItemIdAndActorIdAndActorRole(Long itemId, Long actorId, String roleCode) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ItemContributor> cq = cb.createQuery(ItemContributor.class);
        Root<ItemContributor> rootItemContributor = cq.from(ItemContributor.class);
        cq.select(rootItemContributor);
        cq.where(
                cb.and(
                        cb.equal(rootItemContributor.get("item").get("id"), itemId),
                        cb.equal(rootItemContributor.get("actor").get("id"), actorId),
                        cb.equal(rootItemContributor.get("role").get("code"), roleCode)
                )
        );
        TypedQuery<ItemContributor> query = entityManager.createQuery(cq);
        return query.getResultStream().findFirst().orElse(null);

    }

    public List<ItemContributor> findByItemId(Long itemId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ItemContributor> cq = cb.createQuery(ItemContributor.class);
        Root<ItemContributor> rootItemContributor = cq.from(ItemContributor.class);
        cq.select(rootItemContributor);
        cq.orderBy(cb.asc(rootItemContributor.get("ord")));
        cq.where(cb.equal(rootItemContributor.get("item").get("id"), itemId));
        TypedQuery<ItemContributor> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    public List<ItemContributor> findByActorIdAndActorRole(Long actorId, String roleCode) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ItemContributor> cq = cb.createQuery(ItemContributor.class);
        Root<ItemContributor> rootItemContributor = cq.from(ItemContributor.class);
        cq.select(rootItemContributor);
        cq.where(
                cb.and(
                        cb.equal(rootItemContributor.get("actor").get("id"), actorId),
                        cb.equal(rootItemContributor.get("role").get("code"), roleCode)
                )
        );
        TypedQuery<ItemContributor> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    public int deleteByActorId(Long actorId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<ItemContributor> criteriaDelete = cb.createCriteriaDelete(ItemContributor.class);
        Root<ItemContributor> rootItemContributor = criteriaDelete.from(ItemContributor.class);
        criteriaDelete.where(
                cb.and(
                        cb.equal(rootItemContributor.get("actor").get("id"), actorId)
                )
        );
        Query query = entityManager.createQuery(criteriaDelete);
        return query.executeUpdate();
    }
}
