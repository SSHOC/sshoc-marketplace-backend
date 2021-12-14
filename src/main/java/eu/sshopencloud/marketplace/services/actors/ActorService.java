package eu.sshopencloud.marketplace.services.actors;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorDto;
import eu.sshopencloud.marketplace.dto.actors.PaginatedActors;
import eu.sshopencloud.marketplace.mappers.actors.ActorMapper;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.services.actors.event.ActorChangedEvent;
import eu.sshopencloud.marketplace.services.search.IndexActorService;
import eu.sshopencloud.marketplace.validators.actors.ActorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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

    private final IndexActorService indexActorService;

    private final ApplicationEventPublisher eventPublisher;

    public PaginatedActors getActors(PageCoords pageCoords) {

        Page<Actor> actorsPage = actorRepository.findAll(PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("name"))));

        List<ActorDto> actors = actorsPage.stream().map(ActorMapper.INSTANCE::toDto).collect(Collectors.toList());

        return PaginatedActors.builder().actors(actors)
                .count(actorsPage.getContent().size()).hits(actorsPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(actorsPage.getTotalPages())
                .build();
    }

    public Actor loadActor(Long id) {
        return actorRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id));
    }

    public ActorDto getActor(Long id) {
        return ActorMapper.INSTANCE.toDto(loadActor(id));
    }


    public ActorDto createActor(ActorCore actorCore) {
        Actor actor = actorFactory.create(actorCore, null);
        actorRepository.save(actor);
        indexActorService.indexActor(actor);
        return ActorMapper.INSTANCE.toDto(actor);
    }


    public ActorDto updateActor(Long id, ActorCore actorCore) {
        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id);
        }
        Actor actor = actorFactory.create(actorCore, id);
        actorRepository.save(actor);
        indexActorService.indexActor(actor);

        eventPublisher.publishEvent(new ActorChangedEvent(id, false));

        return ActorMapper.INSTANCE.toDto(actor);
    }


    public void deleteActor(Long id) {
        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id);
        }
        actorRepository.deleteById(id);
        indexActorService.removeActor(id);

        eventPublisher.publishEvent(new ActorChangedEvent(id, true));
    }

}
