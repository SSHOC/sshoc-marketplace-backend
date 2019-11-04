package eu.sshopencloud.marketplace.services.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
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

}
