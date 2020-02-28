package eu.sshopencloud.marketplace.conf.solr;

import eu.sshopencloud.marketplace.repositories.search.solr.ForceFacetSortSolrTemplate;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

@Configuration
@EnableSolrRepositories(
        basePackages = "eu.sshopencloud.marketplace.repositories.search"
)
@ComponentScan
public class SolrConfig {

    @Value("${spring.data.solr.host}")
    private String solrUrl;

    @Bean
    public SolrClient solrClient() {
        return new HttpSolrClient.Builder().withBaseSolrUrl(solrUrl).build();
    }

    @Bean
    public SolrTemplate solrTemplate(SolrClient client) {
        // tag cannot be in the facet field parameters because of the bug in spring-data-solr-4.1.4. We have to override some constructions
        return new ForceFacetSortSolrTemplate(client);
    }

}
