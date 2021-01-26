package eu.sshopencloud.marketplace;

import eu.sshopencloud.marketplace.conf.auth.SecurityProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(SecurityProperties.class)
public class MarketplaceApplication {


    @Bean
    public OpenAPI customOpenAPI(@Value("${springdoc.version}") String apiVersion) {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearer",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("JWT")))
                .info(new Info().title("SSHOC Marketplace API").description("Social Sciences and Humanities Open Cloud Marketplace").version(apiVersion)
                        .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApplication.class, args);
    }
}
