package eu.sshopencloud.marketplace.services.actors;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorDto;
import eu.sshopencloud.marketplace.dto.actors.PaginatedActors;
import eu.sshopencloud.marketplace.mappers.actors.ActorMapper;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.validators.actors.ActorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;

    private final ActorFactory actorFactory;

    private final IndexService indexService;


    public PaginatedActors getActors(String q, PageCoords pageCoords) {
        ExampleMatcher queryActorMatcher = ExampleMatcher.matchingAny()
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("website", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("email", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

        Actor queryActor = new Actor();
        queryActor.setName(q);
        queryActor.setWebsite(q);
        queryActor.setEmail(q);

        Page<Actor> actorsPage = actorRepository.findAll(Example.of(queryActor, queryActorMatcher),
                PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("name"))));

        List<ActorDto> actors = actorsPage.stream().map(ActorMapper.INSTANCE::toDto).collect(Collectors.toList());

        return PaginatedActors.builder().actors(actors)
                .count(actorsPage.getContent().size()).hits(actorsPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(actorsPage.getTotalPages())
                .build();
    }

    public ActorDto getActor(Long id) {
        return ActorMapper.INSTANCE.toDto(actorRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id)));
    }



    public ActorDto createActor(ActorCore actorCore) {
        Actor actor = actorFactory.create(actorCore, null);
        actorRepository.save(actor);
        indexService.indexActor(actor);
        return ActorMapper.INSTANCE.toDto(actor);
    }


    public ActorDto updateActor(Long id, ActorCore actorCore) {
        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id);
        }
        Actor actor = actorFactory.create(actorCore, id);
        actorRepository.save(actor);
        indexService.indexActor(actor);
        return ActorMapper.INSTANCE.toDto(actor);
    }


    public void deleteActor(Long id) {
        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id);
        }
        actorRepository.deleteById(id);
    }

}
