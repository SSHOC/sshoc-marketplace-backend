package eu.sshopencloud.marketplace.repositories.oaipmh.metadata.extractors;

import eu.sshopencloud.marketplace.repositories.oaipmh.metadata.DcValue;
import eu.sshopencloud.marketplace.repositories.oaipmh.OaiItem;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class RelationExtractor extends CodeBasedPropertyExtractor {
    @Override
    public List<DcValue> extractValues(@NotNull OaiItem item, String dcLocalName) {
        List<DcValue> values = new ArrayList<>(super.extractValues(item, dcLocalName));
        if (Objects.nonNull(item.getItem().getAccessibleAt())) {
            item.getItem().getAccessibleAt().stream().filter(Strings::isNotBlank).map(DcValue::new)
                    .forEach(values::add);
        }
        if (Objects.nonNull(item.getRelatedItems()) && item.getRelatedItems().size() > 0) {
            item.getRelatedItems().stream().map(ri -> new DcValue(ri.getLabel())).forEach(values::add);
        }
        return values;
    }
}
