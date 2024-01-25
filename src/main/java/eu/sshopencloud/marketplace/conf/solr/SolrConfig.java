package eu.sshopencloud.marketplace.conf.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class SolrConfig {

    @Value("${spring.data.solr.host}")
    private String solrUrl;

    @Bean
    public SolrClient solrClient() {
        //@TODO change to Http2SolrClient or alike, once the spring boot version is updated
        return new HttpSolrClient.Builder(solrUrl).build();
    }
}
