package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.search.IndexActor;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.repositories.search.IndexActorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IndexActorService {

    private final IndexActorRepository indexActorRepository;
    private final ActorRepository actorRepository;

    public IndexActor indexActor(Actor actor) {
        IndexActor indexedActor = IndexConverter.covertActor(actor);
        return indexActorRepository.save(indexedActor);
    }

    public void reindexActors() {
        log.debug("Before actor reindex.");
        clearActorIndex();
        for (Actor actor : actorRepository.findAll()) {
            indexActor(actor);
        }
        log.debug("After actor index.");
    }

    public void clearActorIndex() {
        indexActorRepository.deleteAll();
    }

    public void removeActor(Long actorId) {
        indexActorRepository.deleteById(actorId.toString());
    }


}
