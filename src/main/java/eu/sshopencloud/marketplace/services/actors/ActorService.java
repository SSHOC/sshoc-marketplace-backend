package eu.sshopencloud.marketplace.services.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;

    public List<Actor> getActors(String q, int perpage) {
        ExampleMatcher queryActorMatcher = ExampleMatcher.matchingAny()
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        Actor queryActor = new Actor();
        queryActor.setName(q);

        Page<Actor> actors = actorRepository.findAll(Example.of(queryActor, queryActorMatcher), PageRequest.of(0, perpage, new Sort(Sort.Direction.ASC, "name")));
        return actors.getContent();
    }

    public Actor getActor(Long id) {
        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id);
        }
        Actor actor = actorRepository.getOne(id);
        return actor;
    }

    private List<Actor> validate(String prefix, List<ActorId> actors) throws DataViolationException {
        List<Actor> result = new ArrayList<Actor>();
        if (actors != null) {
            for (int i = 0; i < actors.size(); i++) {
                result.add(validate(prefix + "[" + i + "].", actors.get(i)));
            }
        }
        return result;
    }

    public Actor validate(String prefix, ActorId actor) throws DataViolationException {
        if (actor.getId() == null) {
            throw new DataViolationException(prefix + "id", actor.getId());
        }
        Optional<Actor> result = actorRepository.findById(actor.getId());
        if (!result.isPresent()) {
            throw new DataViolationException(prefix + "id", actor.getId());
        }
        return result.get();
    }


    private Actor validate(ActorCore newActor, Long actorId) throws DataViolationException {
        Actor result = createOrGetTool(actorId);
        if (StringUtils.isBlank(newActor.getName())) {
            throw new DataViolationException("name", newActor.getName());
        }
        result.setName(newActor.getName());
        // TODO validate URL
        result.setWebsite(newActor.getWebsite());
        // TODO validate email
        result.setEmail(newActor.getEmail());
        if (result.getAffiliations() != null) {
            result.getAffiliations().clear();
            result.getAffiliations().addAll(validate("affiliations", newActor.getAffiliations()));
        } else {
            result.setAffiliations(validate("affiliations", newActor.getAffiliations()));
        }
        return result;
    }

    private Actor createOrGetTool(Long actorId) {
        if (actorId != null) {
            return actorRepository.getOne(actorId);
        } else {
            return new Actor();
        }
    }

    public Actor createActor(ActorCore newActor) throws DataViolationException {
        // TODO don't allow creating without authentication (in WebSecurityConfig)
        Actor actor = validate(newActor, null);
        actorRepository.save(actor);
        return actor;
    }


    public Actor updateActor(Long id, ActorCore newActor) throws DataViolationException {
        // TODO don't allow creating without authentication (in WebSecurityConfig)
        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id);
        }
        Actor actor = validate(newActor, id);
        actorRepository.save(actor);
        return actor;
    }


    public void deleteActor(Long id) {
        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id);
        }
        actorRepository.deleteById(id);
    }

}
