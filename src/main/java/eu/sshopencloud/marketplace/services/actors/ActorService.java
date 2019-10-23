package eu.sshopencloud.marketplace.services.actors;

import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;

    public List<Actor> getActors(String q) {
        ExampleMatcher queryActorMatcher = ExampleMatcher.matchingAny()
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        Actor queryActor = new Actor();
        queryActor.setName(q);

        return actorRepository.findAll(Example.of(queryActor, queryActorMatcher), new Sort(Sort.Direction.ASC, "name"));
    }

}
