package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.search.IndexActor;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IndexActorService {
    private final SolrClient solrClient;
    private final ActorRepository actorRepository;

    public void indexActor(Actor actor) {
        try {
            solrClient.add(IndexActor.COLLECTION_NAME, IndexConverter.covertActor(actor));
            solrClient.commit(IndexActor.COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
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
        try {
            solrClient.deleteByQuery(IndexActor.COLLECTION_NAME, "*:*");
            solrClient.commit(IndexActor.COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeActor(Long actorId) {
        try {
            solrClient.deleteById(IndexActor.COLLECTION_NAME, actorId.toString());
            solrClient.commit(IndexActor.COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
