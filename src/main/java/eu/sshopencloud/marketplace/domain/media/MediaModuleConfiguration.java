package eu.sshopencloud.marketplace.domain.media;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class MediaModuleConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
