package eu.sshopencloud.marketplace.validators.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorExternalIdCore;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorExternalId;
import eu.sshopencloud.marketplace.model.actors.ActorSource;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.repositories.actors.ActorSourceRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
public class ActorFactory {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    private final ActorRepository actorRepository;
    private final ActorSourceRepository actorSourceRepository;


    public Actor create(ActorCore actorCore, Long actorId) throws ValidationException {
        Actor actor = getOrCreateActor(actorId);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(actorCore, "Actor");

        if (StringUtils.isBlank(actorCore.getName())) {
            errors.rejectValue("name", "field.required", "Name is required.");
        } else {
            actor.setName(actorCore.getName());
        }

        actor.addExternalIds(prepareExternalIds(actorCore.getExternalIds(), actor, errors));

        if (StringUtils.isNotBlank(actorCore.getWebsite())) {
            try {
                actor.setWebsite(new URL(actorCore.getWebsite()).toURI().toString());
            }
            catch (MalformedURLException | URISyntaxException e) {
                errors.rejectValue("website", "field.invalid", "Website is malformed URL.");
            }
        }

        if (StringUtils.isNotBlank(actorCore.getEmail())) {
            if (EMAIL_PATTERN.matcher(actorCore.getEmail()).matches()) {
                actor.setEmail(actorCore.getEmail());
            } else {
                errors.rejectValue("email", "field.invalid", "Email is malformed.");
            }
        }

        actor.getAffiliations().addAll(prepareAffiliations(actorCore.getAffiliations(), actor, errors, "affiliations"));

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return actor;
    }

    private List<ActorExternalId> prepareExternalIds(List<ActorExternalIdCore> externalIds, Actor actor, Errors errors) {
        List<ActorExternalId> actorExternalIds = new ArrayList<>();
        Set<ActorExternalId> processedExternalIds = new HashSet<>();

        if (externalIds == null)
            return actorExternalIds;

        for (int i = 0; i < externalIds.size(); ++i) {
            String nestedPath = String.format("externalIds[%d]", i);
            errors.pushNestedPath(nestedPath);

            ActorExternalId actorExternalId = prepareExternalId(externalIds.get(i), actor, errors);
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
                                    actorExternalId.getIdentifierService().getLabel(), actorExternalId.getIdentifier()
                            )
                    );

                    continue;
                }
            }

            errors.popNestedPath();
        }

        return actorExternalIds;
    }

    private ActorExternalId prepareExternalId(ActorExternalIdCore externalId, Actor actor, Errors errors) {
        Optional<ActorSource> actorSource = actorSourceRepository.findById(externalId.getServiceIdentifier());

        if (actorSource.isEmpty()) {
            errors.rejectValue(
                    "serviceIdentifier", "field.notExist",
                    String.format("Unknown service identifier: %s", externalId.getServiceIdentifier())
            );

            return null;
        }

        return new ActorExternalId(actorSource.get(), externalId.getIdentifier(), actor);
    }

    private List<Actor> prepareAffiliations(List<ActorId> actorIds, Actor affiliatedActor, Errors errors, String nestedPath) {
        List<Actor> actors = new ArrayList<Actor>();
        if (actorIds != null) {
            for (int i = 0; i < actorIds.size(); i++) {
                errors.pushNestedPath(nestedPath + "[" + i + "]");
                Actor actor = prepareAffiliation(actorIds.get(i), errors);
                if (actor != null) {
                    actors.add(actor);
                }
                errors.popNestedPath();
            }
        }
        if (affiliatedActor.getAffiliations() != null) {
            affiliatedActor.getAffiliations().clear();
        }
        return actors;
    }

    public Actor prepareAffiliation(ActorId actorId, Errors errors) {
        if (actorId.getId() == null) {
            errors.rejectValue("id", "field.required", "Actor identifier is required.");
            return null;
        }
        Optional<Actor> actorHolder = actorRepository.findById(actorId.getId());
        if (actorHolder.isEmpty()) {
            errors.rejectValue("id", "field.notExist", "Actor does not exist.");
            return null;
        }

        return actorHolder.get();
    }


    private Actor getOrCreateActor(Long actorId) {
        if (actorId != null)
            return actorRepository.getOne(actorId);

        return new Actor();
    }
}
