package eu.sshopencloud.marketplace.repositories.actors;

import eu.sshopencloud.marketplace.model.actors.ActorExternalId;
import eu.sshopencloud.marketplace.model.actors.ActorSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActorExternalIdRepository extends JpaRepository<ActorExternalId, Long> {

    Optional<ActorExternalId> findByIdentifierServiceAndIdentifier(ActorSource identifierService, String identifier);

}
