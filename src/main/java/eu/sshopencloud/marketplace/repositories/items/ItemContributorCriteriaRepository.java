package eu.sshopencloud.marketplace.repositories.items;


import eu.sshopencloud.marketplace.model.items.ItemContributor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;


@Repository
public class ItemContributorCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<ItemContributor> findItemContributorByItemId(Long itemId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ItemContributor> cq = cb.createQuery(ItemContributor.class);
        Root<ItemContributor> rootItemContributor = cq.from(ItemContributor.class);
        cq.select(rootItemContributor);
        cq.orderBy(cb.asc(rootItemContributor.get("ord")));
        cq.where(cb.equal(rootItemContributor.get("item").get("id"), itemId));
        TypedQuery<ItemContributor> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

}
