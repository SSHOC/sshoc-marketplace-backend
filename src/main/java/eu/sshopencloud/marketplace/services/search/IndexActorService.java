package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.search.IndexActor;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IndexActorService {
    private final SolrClient solrClient;
    private final ActorRepository actorRepository;

    @Value("${marketplace.index.reindexActorsBatchSize}")
    private int reindexActorsBatchSize;

    public void indexActor(Actor actor) {
        try {
            solrClient.add(IndexActor.COLLECTION_NAME, IndexConverter.covertActor(actor));
            solrClient.commit(IndexActor.COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void indexActors(List<Actor> actors) {
        try {
            solrClient.add(IndexActor.COLLECTION_NAME, actors.stream().map(IndexConverter::covertActor).collect(Collectors.toList()));
            solrClient.commit(IndexActor.COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reindexActors() {
        log.debug("Before actor reindex. Clearing index...");
        clearActorIndex();
        log.debug("Retrieving actors to reindex...");
        List<Actor> actorsToReindex = actorRepository.findAll();
        long numberOfActors = actorsToReindex.size();
        log.debug("{} actors to reindex. Indexing...", numberOfActors);
        long numberOfIteratedActors = 0L;
        long numberOfIndexedActors = 0L;
        List<Actor> batch = new ArrayList<>();
        for (Actor actor : actorsToReindex) {
            numberOfIteratedActors++;
            batch.add(actor);
            if (numberOfIteratedActors % reindexActorsBatchSize == 0) {
                indexActors(batch);
                numberOfIndexedActors += batch.size();
                log.debug("Indexed {} actors out of {} actors", numberOfIndexedActors, numberOfActors);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            indexActors(batch);
            numberOfIndexedActors += batch.size();
            log.debug("Indexed {} actors out of {} actors", numberOfIndexedActors, numberOfActors);
            batch.clear();
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
