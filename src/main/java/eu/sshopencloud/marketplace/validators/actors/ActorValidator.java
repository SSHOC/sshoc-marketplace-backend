package eu.sshopencloud.marketplace.validators.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ActorValidator {

    private final ActorRepository actorRepository;


    public Actor validate(ActorCore actorCore, Long actorId) throws ValidationException {
        Actor actor = getOrCreateActor(actorId);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(actorCore, "Actor");

        if (StringUtils.isBlank(actorCore.getName())) {
            errors.rejectValue("name", "field.required", "Name is required.");
        } else {
            actor.setName(actorCore.getName());
        }

        // TODO validate URL
        actor.setWebsite(actorCore.getWebsite());
        // TODO validate email
        actor.setEmail(actorCore.getEmail());

        if (actor.getAffiliations() != null) {
            actor.getAffiliations().addAll(validate(actorCore.getAffiliations(), actor, errors, "affiliations"));
        } else {
            actor.setAffiliations(validate(actorCore.getAffiliations(), actor, errors, "affiliations"));
        }

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return actor;
        }
    }


    private List<Actor> validate(List<ActorId> actorIds, Actor affiliatedActor, Errors errors, String nestedPath) {
        List<Actor> actors = new ArrayList<Actor>();
        if (actorIds != null) {
            for (int i = 0; i < actorIds.size(); i++) {
                errors.pushNestedPath(nestedPath + "[" + i + "]");
                Actor actor = validate(actorIds.get(i), errors);
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


    public Actor validate(ActorId actorId, Errors errors) {
        if (actorId.getId() == null) {
            errors.rejectValue("id", "field.required", "Actor identifier is required.");
            return null;
        }
        Optional<Actor> actorHolder = actorRepository.findById(actorId.getId());
        if (!actorHolder.isPresent()) {
            errors.rejectValue("id", "field.notExist", "Actor does not exist.");
            return null;
        } else {
            return actorHolder.get();
        }
    }


    private Actor getOrCreateActor(Long actorId) {
        if (actorId != null) {
            return actorRepository.getOne(actorId);
        } else {
            return new Actor();
        }
    }

}
