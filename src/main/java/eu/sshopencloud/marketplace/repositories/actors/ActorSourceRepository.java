package eu.sshopencloud.marketplace.repositories.actors;

import eu.sshopencloud.marketplace.model.actors.ActorSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ActorSourceRepository extends JpaRepository<ActorSource, String> {

//    @Query(
//            "select case when count(a) > 0 then true else false end from Actor a " +
//                    "where a.externalId.identifierService.code = :serviceCode"
//    )
//    boolean isActorSourceInUse(@Param("serviceCode") String actorSourceCode);
}
