package eu.sshopencloud.marketplace.repositories.oaipmh.metadata;

import eu.sshopencloud.marketplace.repositories.oaipmh.OaiItem;
import lombok.NonNull;

import java.util.List;

public interface ValuesExtractor {
    List<DcValue> extractValues(@NonNull OaiItem item, String dcLocalName);
}
