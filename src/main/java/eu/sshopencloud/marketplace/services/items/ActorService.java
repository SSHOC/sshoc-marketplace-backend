package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.Actor;
import eu.sshopencloud.marketplace.repositories.items.ActorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;

    public List<Actor> getAllActors() {
        return actorRepository.findAll(new Sort(Sort.Direction.ASC, "name"));
    }

}
