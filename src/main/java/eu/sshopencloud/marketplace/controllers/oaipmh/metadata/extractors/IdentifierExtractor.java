package eu.sshopencloud.marketplace.controllers.oaipmh.metadata.extractors;

import eu.sshopencloud.marketplace.controllers.oaipmh.OaiItemUtils;
import eu.sshopencloud.marketplace.controllers.oaipmh.metadata.DcValue;
import eu.sshopencloud.marketplace.controllers.oaipmh.metadata.ValuesExtractor;
import eu.sshopencloud.marketplace.repositories.oaipmh.OaiItem;
import eu.sshopencloud.marketplace.services.items.ItemSourceService;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IdentifierExtractor implements ValuesExtractor {
    @Override
    public List<DcValue> extractValues(@NotNull OaiItem item, String dcLocalName) {
        List<DcValue> values = new ArrayList<>();
        values.add(new DcValue(OaiItemUtils.buildOaiId(item.getItem().getPersistentId())));
        List<DcValue> externalIdsValues = item.getItem().getExternalIds().stream()
                .filter(eId -> Strings.isNotBlank(eId.getIdentifierService().getUrlTemplate()) &&
                        Strings.isNotBlank(eId.getIdentifier())).map(eId -> new DcValue(
                        eId.getIdentifierService().getUrlTemplate().replaceAll(
                                ItemSourceService.SOURCE_ITEM_ID_PLACEHOLDER.replace("{", "\\{").replace("}", "\\}"),
                                eId.getIdentifier()))).collect(Collectors.toList());
        values.addAll(externalIdsValues);
        return values;
    }
}
