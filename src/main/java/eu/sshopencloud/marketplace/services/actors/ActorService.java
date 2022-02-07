package eu.sshopencloud.marketplace.services.actors;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorDto;
import eu.sshopencloud.marketplace.dto.actors.ActorHistoryDto;
import eu.sshopencloud.marketplace.dto.actors.PaginatedActors;
import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.mappers.actors.ActorMapper;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorExternalId;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.services.actors.event.ActorChangedEvent;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.items.ItemsService;
import eu.sshopencloud.marketplace.services.search.IndexActorService;
import eu.sshopencloud.marketplace.validators.actors.ActorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
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

    private final ItemsService itemsService;

    private final ActorHistoryService actorHistoryService;

    private final IndexActorService indexActor;


    public PaginatedActors getActors(PageCoords pageCoords) {

        Page<Actor> actorsPage = actorRepository.findAll(
                PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("name"))));

        List<ActorDto> actors = actorsPage.stream().map(ActorMapper.INSTANCE::toDto).collect(Collectors.toList());

        if(!LoggedInUserHolder.getLoggedInUser().isModerator()) actors.forEach(actorDto -> actorDto.setEmail(null));

        return PaginatedActors.builder().actors(actors).count(actorsPage.getContent().size())
                .hits(actorsPage.getTotalElements()).page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(actorsPage.getTotalPages()).build();
    }


    public Actor loadActor(Long id) {
        return actorRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id));
    }


    public ActorDto getActor(Long id, boolean items) {
        ActorDto actorDto = ActorMapper.INSTANCE.toDto(loadActor(id));
        if (items) {
            actorDto.setItems(getItemsByActor(id));
        }
        if (!LoggedInUserHolder.getLoggedInUser().isModerator()) actorDto.setEmail(null);
        return actorDto;
    }


    public ActorDto createActor(ActorCore actorCore) {
        Actor actor = actorFactory.create(actorCore, null);
        actorRepository.save(actor);
        indexActorService.indexActor(actor);
        ActorDto actorDto = ActorMapper.INSTANCE.toDto(actor);
        if (!LoggedInUserHolder.getLoggedInUser().isModerator()) actorDto.setEmail(null);
        return actorDto;
    }


    public ActorDto updateActor(Long id, ActorCore actorCore) {
        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id);
        }
        Actor actor = actorFactory.create(actorCore, id);
        actorRepository.save(actor);
        indexActorService.indexActor(actor);

        eventPublisher.publishEvent(new ActorChangedEvent(id, false));

        ActorDto actorDto = ActorMapper.INSTANCE.toDto(actor);
        if (!LoggedInUserHolder.getLoggedInUser().isModerator()) actorDto.setEmail(null);
        return actorDto;
    }


    public void deleteActor(Long id) {
        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id);
        }
        actorRepository.deleteById(id);
        indexActorService.removeActor(id);

        //TODO - rethink
        // eventPublisher.publishEvent(new ActorChangedEvent(id, true));
    }


    public ActorDto mergeActors(long id, List<Long> with) {

        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id);
        }

        with.forEach(mergeId -> {
            if (!actorRepository.existsById(mergeId))
                throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + mergeId);
        });

        Actor actor = loadActor(id);

        with.forEach(mergeId -> {

            Actor mergeActor = loadActor(mergeId);

            actor.getAffiliations().remove(mergeActor);

            actor.addToHistory(actorHistoryService.createActorHistory(actor, mergeActor));

            mergeActor.getAffiliations().stream().filter(a -> !actor.getAffiliations().contains(a)).forEach(a -> actor.getAffiliations().add(a));

            mergeAffiliations(actor, mergeActor);

            actorRepository.save(mergeActor);

            itemsService.mergeContributors(actor, mergeActor);

            actor.addExternalIdsList(mergeExternalIds(actor, mergeActor));

        });

        actorRepository.save(actor);
        indexActor.indexActor(actor);
        eventPublisher.publishEvent(new ActorChangedEvent(id, false));

        with.forEach(this::deleteActor);

        ActorDto actorDto = ActorMapper.INSTANCE.toDto(actor);
        if (!LoggedInUserHolder.getLoggedInUser().isModerator()) actorDto.setEmail(null);
        return actorDto;
    }


    public boolean containsExternalId(List<ActorExternalId> externalIds, ActorExternalId id) {
        return externalIds.stream().anyMatch(eId -> eId.getIdentifier().equals(id.getIdentifier()) && eId.getIdentifierService().equals(id.getIdentifierService()));
    }


    public List<ActorHistoryDto> getHistory(long id) {
        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id);
        }
        return actorHistoryService.findHistory(loadActor(id));
    }


    public void mergeAffiliations(Actor actor, Actor mergeActor) {
        List<Actor> affiliations = actorRepository.getActorsByAffiliations(mergeActor);
        affiliations.forEach(affiliation -> {
            affiliation.getAffiliations().remove(mergeActor);
            if (!affiliation.equals(actor))
                affiliation.getAffiliations().add(actor);
        });

        actorRepository.saveAll(affiliations);
    }

    public List<ActorExternalId> mergeExternalIds(Actor actor, Actor mergeActor) {
        List<ActorExternalId> externalIds = new ArrayList<>();
        mergeActor.getExternalIds().forEach(
                externalId -> {
                    if (!containsExternalId(actor.getExternalIds(), externalId)) {
                        ActorExternalId actorExternalId = new ActorExternalId(externalId.getIdentifierService(),
                                externalId.getIdentifier(), actor);
                        externalIds.add(actorExternalId);
                    }
                }
        );

        return externalIds;
    }

    private List<ItemBasicDto> getItemsByActor(long id) {
        return itemsService.getItemsByActor(loadActor(id));
    }

}
