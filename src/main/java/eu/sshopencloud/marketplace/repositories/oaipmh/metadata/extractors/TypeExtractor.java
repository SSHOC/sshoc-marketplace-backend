package eu.sshopencloud.marketplace.repositories.oaipmh.metadata.extractors;

import eu.sshopencloud.marketplace.repositories.oaipmh.metadata.DcValue;
import eu.sshopencloud.marketplace.repositories.oaipmh.OaiItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TypeExtractor extends CodeBasedPropertyExtractor {
    @Override
    public List<DcValue> extractValues(@NotNull OaiItem item, String dcLocalName) {
        List<DcValue> values = new ArrayList<>(super.extractValues(item, dcLocalName));
        if (Objects.nonNull(item.getItem().getCategory())) {
            values.add(new DcValue(item.getItem().getCategory().getLabel()));
        }
        return values;
    }
}
