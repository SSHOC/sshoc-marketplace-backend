package eu.sshopencloud.marketplace.repositories.search.solr;


import org.apache.solr.client.solrj.SolrClient;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.convert.SolrJConverter;
import org.springframework.stereotype.Component;

@Component
public class ForceNestedDocumentsSolrTemplate extends SolrTemplate {

    public ForceNestedDocumentsSolrTemplate(SolrClient solrClient) {
        super(solrClient);
        setSolrConverter(new SolrJConverter());
    }

}
