package eu.sshopencloud.marketplace.repositories.sources.projection;


import java.time.ZonedDateTime;

public interface DetailedSourceView {
    Long getId();
    String getDomain();
    String getLabel();
    ZonedDateTime getLastHarvestedDate();
    String getUrl();
    String getUrlTemplate();

    String getSourceItemId();
}
