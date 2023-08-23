package eu.sshopencloud.marketplace.controllers.oaipmh.metadata.extractors;

import eu.sshopencloud.marketplace.controllers.oaipmh.metadata.DcValue;
import eu.sshopencloud.marketplace.controllers.oaipmh.metadata.ValuesExtractor;
import eu.sshopencloud.marketplace.repositories.oaipmh.OaiItem;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TitleExtractor implements ValuesExtractor {
    @Override
    public List<DcValue> extractValues(@NotNull OaiItem item, String dcLocalName) {
        if (Strings.isNotBlank(item.getItem().getLabel())) {
            return List.of(new DcValue(item.getItem().getLabel()));
        }
        return List.of();
    }
}
