package eu.sshopencloud.marketplace.services.actors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorDto;
import eu.sshopencloud.marketplace.dto.actors.PaginatedActors;
import eu.sshopencloud.marketplace.mappers.actors.ActorMapper;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorExternalId;
import eu.sshopencloud.marketplace.model.actors.ActorHistory;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.services.actors.event.ActorChangedEvent;
import eu.sshopencloud.marketplace.services.items.ItemsService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.validators.CollectionUtils;
import eu.sshopencloud.marketplace.validators.actors.ActorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;

    private final ActorFactory actorFactory;

    private final IndexService indexService;

    private final ApplicationEventPublisher eventPublisher;

    private final ItemsService itemsService;

    private final ActorExternalIdService actorExternalIdService;


    public PaginatedActors getActors(PageCoords pageCoords) {

        Page<Actor> actorsPage = actorRepository.findAll(
                PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("name"))));

        List<ActorDto> actors = actorsPage.stream().map(ActorMapper.INSTANCE::toDto).collect(Collectors.toList());

        return PaginatedActors.builder().actors(actors).count(actorsPage.getContent().size())
                .hits(actorsPage.getTotalElements()).page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(actorsPage.getTotalPages()).build();
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

        eventPublisher.publishEvent(new ActorChangedEvent(id, false));

        return ActorMapper.INSTANCE.toDto(actor);
    }


    public void deleteActor(Long id) {
        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id);
        }
        actorRepository.deleteById(id);
        indexService.removeActor(id);

        eventPublisher.publishEvent(new ActorChangedEvent(id, true));
    }

    //Eliza

    //ExternalId
    //ActorSOurce

    //reassing affiliations
    public ActorDto mergeActors(long id, List<Long> with) {

        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Actor.class.getName() + " with id " + id);
        }

        Actor actor = loadActor(id);

        with.forEach(otherId -> {
            Actor otherActor = loadActor(otherId);
            otherActor = loadActor(otherId);


            otherActor.getAffiliations().forEach(affiliation -> {
                if (CollectionUtils.isAllNulls(actor.getAffiliations()) || actor.getExternalIds().isEmpty()) {
                    ArrayList<Actor> affiliations = new ArrayList<>();
                    affiliations.add(affiliation);
                    actor.setAffiliations(affiliations);
                } else {
                    if (!actor.getExternalIds().contains(affiliation))
                        actor.getAffiliations().add(affiliation);
                }
            });


            otherActor.getContributorTo().forEach(contributor -> {
                        if (CollectionUtils.isAllNulls(actor.getContributorTo()) || actor.getContributorTo().isEmpty()) {
                            ArrayList<ItemContributor> contributors = new ArrayList<>();
                            contributors.add(contributor);
                            actor.setContributorTo(contributors);
                        } else {
                            if (!actor.getContributorTo().contains(contributor))
                                actor.getContributorTo().add(contributor);
                        }
                    }
            );



            itemsService.replaceActors(actor, otherActor);

            //Eliza check if contains !!
            otherActor.getExternalIds().forEach(actorExternalId -> {
                if (CollectionUtils.isAllNulls(actor.getExternalIds()) || actor.getExternalIds().isEmpty()) {
                    ArrayList<ActorExternalId> externalIds = new ArrayList<>();
                    externalIds.add(actorExternalId);
                    actorExternalId.setActor(actor);
                    actor.addExternalIds(externalIds);
                } else {
                    actorExternalId.setActor(actor);
                    if (!actor.getExternalIds().contains(actorExternalId)) {
                        actor.addExternalId(actorExternalId);
                    }
                }
            });

         //   otherActor.getExternalIds().clear();

            try {
                ActorHistory actorHistory = addToHistory(actor, otherActor);
                System.out.println(actorHistory);
                //actor.getHistory().add(actorHistory);
                //actorRepository.save(actor);
              //  actor.addToHistory(actorHistory);
            } catch (IOException e) {
                throw new ValidationException("Can not add merged actor to history!");
            }
        });

      //  actorRepository.save(actor);
        eventPublisher.publishEvent(new ActorChangedEvent(id, false));

        with.forEach(otherId -> deleteActor(otherId));

        return ActorMapper.INSTANCE.toDto(actor);
    }


    //Eliza - createHistory
    public ActorHistory addToHistory(Actor actor, Actor other)
            throws IOException {

        Optional<Actor> a = actorRepository.findById(actor.getId());

        if (a.isEmpty())
            return null;

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

        String jsonHistoryString = gson.toJson(other);
        System.out.println(jsonHistoryString);

        ActorHistory actorHistory = new ActorHistory();
        actorHistory.setActor(a.get());
        actorHistory.setHistory(jsonHistoryString);
        actorHistory.setDateCreated(ZonedDateTime.now());

        a.get().addToHistory(actorHistory);
        //actorRepository.save(a.get());

        return actorHistory;
    }

}
