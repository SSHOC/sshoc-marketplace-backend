package eu.sshopencloud.marketplace.repositories.actors;

import eu.sshopencloud.marketplace.model.actors.ActorRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActorRoleRepository extends JpaRepository<ActorRole, String> {

    @Query("select case when count(c) > 0 then true else false end from ItemContributor c where c.role.code = :role")
    boolean isActorRoleInUse(@Param("role") String actorRole);
}
