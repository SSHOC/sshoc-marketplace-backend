package eu.sshopencloud.marketplace.conf.handle;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration used for communication with handle server for persistent identifiers
 */
@Configuration
@ConfigurationProperties("handle")
@Getter
@Setter
public class HandleServerConfiguration {

  private String baseUrl;
  private String appHandleValue;
  private String keyLocation;
  private String userIdHandle;
  private int userIdIndex;
}
