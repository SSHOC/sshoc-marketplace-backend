package eu.sshopencloud.marketplace.conf.oaipmh;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@EnableConfigurationProperties
@ConfigurationProperties("marketplace.oai-pmh-data-provider.mapping")
@Component
@Getter
@Setter
public class OaiPmhMetadataConfiguration {
    private Map<String, List<String>> dcToPropertyCode;
    private Map<String, List<String>> dcToActorRole;

    public List<String> getPropertyCode(String dcElementName) {
        return getCodesForDcElement(dcToPropertyCode, dcElementName);
    }

    public List<String> getActorRole(String dcElementName) {
        return getCodesForDcElement(dcToActorRole, dcElementName);
    }

    @NotNull
    private List<String> getCodesForDcElement(Map<String, List<String>> mapping, String dcElementName) {
        return mapping != null ? Optional.ofNullable(mapping.get(dcElementName)).orElse(List.of()) : List.of();
    }
}
