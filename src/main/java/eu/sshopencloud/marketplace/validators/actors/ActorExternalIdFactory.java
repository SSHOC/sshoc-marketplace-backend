package eu.sshopencloud.marketplace.validators.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorExternalIdCore;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorExternalId;
import eu.sshopencloud.marketplace.model.actors.ActorSource;
import eu.sshopencloud.marketplace.services.actors.ActorSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.*;


@Component
@RequiredArgsConstructor
public class ActorExternalIdFactory {

    private final ActorSourceService actorSourceService;


    public List<ActorExternalId> create(List<ActorExternalIdCore> externalIds, Actor actor, Errors errors) {
        List<ActorExternalId> actorExternalIds = new ArrayList<>();
        Set<ActorExternalId> processedExternalIds = new HashSet<>();

        if (externalIds == null)
            return actorExternalIds;

        for (int i = 0; i < externalIds.size(); ++i) {
            String nestedPath = String.format("externalIds[%d]", i);
            errors.pushNestedPath(nestedPath);

            ActorExternalId actorExternalId = create(externalIds.get(i), actor, errors);
            if (actorExternalId != null) {

                if (!processedExternalIds.contains(actorExternalId)) {
                    actorExternalIds.add(actorExternalId);
                    processedExternalIds.add(actorExternalId);
                }
                else {
                    errors.popNestedPath();
                    errors.rejectValue(
                            nestedPath, "field.duplicateEntry",
                            String.format(
                                    "Duplicate actor's external id: %s (from: %s)",
                                    actorExternalId.getIdentifier(), actorExternalId.getIdentifierService().getLabel()
                            )
                    );

                    continue;
                }
            }

            errors.popNestedPath();
        }

        return actorExternalIds;
    }

    public ActorExternalId create(ActorExternalIdCore externalId, Actor actor, Errors errors) {
        Optional<ActorSource> actorSource = actorSourceService.loadActorSource(externalId.getServiceIdentifier());

        if (actorSource.isEmpty()) {
            errors.rejectValue(
                    "serviceIdentifier", "field.notExist",
                    String.format("Unknown service identifier: %s", externalId.getServiceIdentifier())
            );

            return null;
        }

        return new ActorExternalId(actorSource.get(), externalId.getIdentifier(), actor);
    }
}