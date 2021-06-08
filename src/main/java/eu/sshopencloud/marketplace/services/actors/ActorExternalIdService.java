package eu.sshopencloud.marketplace.services.actors;

import eu.sshopencloud.marketplace.model.actors.ActorExternalId;
import eu.sshopencloud.marketplace.model.actors.ActorSource;
import eu.sshopencloud.marketplace.repositories.actors.ActorExternalIdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ActorExternalIdService {

    private final ActorExternalIdRepository actorExternalIdRepository;

    public Optional<ActorExternalId> loadActorExternalId(ActorSource identifierService, String identifier) {
        return actorExternalIdRepository.findByIdentifierServiceAndIdentifier(identifierService, identifier);
    }

}
