package eu.sshopencloud.marketplace.controllers.oaipmh.metadata.extractors;

import eu.sshopencloud.marketplace.controllers.oaipmh.OaiItemUtils;
import eu.sshopencloud.marketplace.controllers.oaipmh.metadata.DcValue;
import eu.sshopencloud.marketplace.controllers.oaipmh.metadata.ValuesExtractor;
import eu.sshopencloud.marketplace.repositories.oaipmh.OaiItem;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CodeBasedPropertyExtractor implements ValuesExtractor {

    @Override
    public List<DcValue> extractValues(@NotNull OaiItem item, String dcLocalName) {
        return item.getItem().getProperties().stream().filter(p -> OaiItemUtils.getPropertyCodeForDcName(dcLocalName).contains(p.getType().getCode())).map(p ->
                new DcValue(p.getValue())).collect(Collectors.toList());
    }
}
