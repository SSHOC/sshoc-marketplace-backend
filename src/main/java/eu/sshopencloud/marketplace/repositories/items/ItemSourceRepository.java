package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.ItemSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ItemSourceRepository extends JpaRepository<ItemSource, String> {

    @Query(
            "select case when count(i) > 0 then true else false end from ItemExternalId i " +
                    "where i.identifierService.code = :serviceCode"
    )
    boolean isItemSourceInUse(@Param("serviceCode") String itemSourceCode);
}
