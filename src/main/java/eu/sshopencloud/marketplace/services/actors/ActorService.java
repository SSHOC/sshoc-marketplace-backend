package eu.sshopencloud.marketplace.services.actors;

import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

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

}
