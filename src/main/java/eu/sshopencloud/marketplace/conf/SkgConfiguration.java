package eu.sshopencloud.marketplace.conf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration used for SKG-IF API
 */
@Configuration
@ConfigurationProperties("skg")
@Getter
@Setter
public class SkgConfiguration {

    private String baseUri;
}
