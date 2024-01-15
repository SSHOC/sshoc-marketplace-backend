package eu.sshopencloud.marketplace.conf.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableSolrRepositories(
//        basePackages = "eu.sshopencloud.marketplace.repositories.search"
//)
@ComponentScan
public class SolrConfig {

    @Value("${spring.data.solr.host}")
    private String solrUrl;

    @Bean
    public SolrClient solrClient() {
        return new Http2SolrClient.Builder(solrUrl).build();
    }

//    @Bean
//    public SolrTemplate solrTemplate(SolrClient client) {
//        // tag cannot be in the facet field parameters because of the bug in spring-data-solr-4.1.4. We have to override some constructions
//        return new ForceFacetSortSolrTemplate(client);
//    }

}
