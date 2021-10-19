package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.ItemRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRelationRepository extends JpaRepository<ItemRelation, String> {

    ItemRelation getItemRelationByCode(String code);

    ItemRelation getItemRelationByInverseOfCode(String inverseOfCode);


}
