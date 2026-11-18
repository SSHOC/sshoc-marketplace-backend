package eu.sshopencloud.marketplace.conf;

import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAsync
class ApplicationConfig {

    @Bean
    @Primary
    public PageCoordsValidator productsPageCoordsValidator(
            @Value("${marketplace.pagination.default-perpage}") int defaultPerpage,
            @Value("${marketplace.pagination.maximal-perpage}") int maximalPerpage) {
        return new PageCoordsValidator(defaultPerpage, maximalPerpage);
    }

    @Bean
    public PageCoordsValidator skgPageCoordsValidator(
            @Value("${marketplace.pagination.skg-default-perpage}") int defaultPerpage,
            @Value("${marketplace.pagination.maximal-perpage}") int maximalPerpage) {
        return new PageCoordsValidator(defaultPerpage, maximalPerpage);
    }


}
