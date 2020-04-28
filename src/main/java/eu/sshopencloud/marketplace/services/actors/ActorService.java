package eu.sshopencloud.marketplace.services.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorDto;
import eu.sshopencloud.marketplace.mappers.actors.ActorMapper;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.validators.actors.ActorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;

    private final ActorValidator actorValidator;


    public List<ActorDto> getActors(String q, int perpage) {
        ExampleMatcher queryActorMatcher = ExampleMatcher.matchingAny()
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("website", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("email", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

        Actor queryActor = new Actor();
        queryActor.setName(q);
        queryActor.setWebsite(q);
        queryActor.setEmail(q);

        Page<Actor> actorsPage = actorRepository.findAll(Example.of(queryActor, queryActorMatcher), PageRequest.of(0, perpage, Sort.by(Sort.Order.asc("name"))));
        return ActorMapper.INSTANCE.toDto(actorsPage.getContent());
    }

    public ActorDto getActor(Long id) {
        return ActorMapper.INSTANCE.toDto(actorRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id)));
    }



    public ActorDto createActor(ActorCore actorCore) {
        // TODO don't allow creating without authentication (in WebSecurityConfig)
        Actor actor = actorValidator.validate(actorCore, null);
        actorRepository.save(actor);
        return ActorMapper.INSTANCE.toDto(actor);
    }


    public ActorDto updateActor(Long id, ActorCore actorCore) {
        // TODO don't allow creating without authentication (in WebSecurityConfig)
        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id);
        }
        Actor actor = actorValidator.validate(actorCore, id);
        actorRepository.save(actor);
        return ActorMapper.INSTANCE.toDto(actor);
    }


    public void deleteActor(Long id) {
        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id);
        }
        actorRepository.deleteById(id);
    }

}
