package eu.sshopencloud.marketplace.validators.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.validators.CollectionUtils;
import eu.sshopencloud.marketplace.validators.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;


@Component
@RequiredArgsConstructor
public class ActorFactory {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    private final ActorRepository actorRepository;
    private final ActorExternalIdFactory actorExternalIdFactory;

    public Actor create(ActorCore actorCore, Long actorId) throws ValidationException {
        Actor actor = getOrCreateActor(actorId);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(actorCore, "Actor");

        if (StringUtils.isBlank(actorCore.getName())) {
            errors.rejectValue("name", "field.required", "Name is required.");
        } else {
            actor.setName(actorCore.getName());
        }

        actor.addExternalIds(actorExternalIdFactory.create(actorCore.getExternalIds(), actor, errors));

        if (StringUtils.isNotBlank(actorCore.getWebsite())) {
            try {
                actor.setWebsite(new URL(actorCore.getWebsite()).toURI().toString());
            } catch (MalformedURLException | URISyntaxException e) {
                errors.rejectValue("website", "field.invalid", "Website is malformed URL.");
            }
        } else {
            actor.setWebsite(null);
        }

        if (StringUtils.isNotBlank(actorCore.getEmail())) {
            if (EMAIL_PATTERN.matcher(actorCore.getEmail()).matches()) {
                actor.setEmail(actorCore.getEmail());
            } else {
                errors.rejectValue("email", "field.invalid", "Email is malformed.");
            }
        } else {
            actor.setEmail(null);
        }

        actor.getAffiliations().addAll(prepareAffiliations(actorCore.getAffiliations(), actor, errors, "affiliations"));

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return actor;
    }

    private List<Actor> prepareAffiliations(List<ActorId> actorIds, Actor affiliatedActor, Errors errors, String nestedPath) {
        List<Actor> actors = new ArrayList<Actor>();
        if (actorIds != null && !CollectionUtils.isAllNulls(actorIds)) {
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
        if (actorId != null) {
            return actorRepository.getOne(actorId);
        }
        return new Actor();
    }

}
