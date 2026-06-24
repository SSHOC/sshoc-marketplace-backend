package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.collections.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IndexCollectionService {

    public static final String COLLECTION_NAME = "marketplace-collections";
    private final SolrClient solrClient;

    public void indexCollection(Collection collection) {

        SolrInputDocument indexedCollection = IndexConverter.convertCollection(collection);

        try {
            solrClient.add(COLLECTION_NAME, indexedCollection);
            solrClient.commit(COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
