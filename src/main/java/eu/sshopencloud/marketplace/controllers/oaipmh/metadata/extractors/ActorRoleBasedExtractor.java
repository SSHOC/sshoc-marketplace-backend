package eu.sshopencloud.marketplace.controllers.oaipmh.metadata.extractors;

import eu.sshopencloud.marketplace.controllers.oaipmh.OaiItemUtils;
import eu.sshopencloud.marketplace.controllers.oaipmh.metadata.DcValue;
import eu.sshopencloud.marketplace.controllers.oaipmh.metadata.ValuesExtractor;
import eu.sshopencloud.marketplace.repositories.oaipmh.OaiItem;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ActorRoleBasedExtractor implements ValuesExtractor {

    @Override
    public List<DcValue> extractValues(@NotNull OaiItem item, String dcLocalName) {
        return item.getItem().getContributors().stream().filter(c -> Objects.nonNull(c.getRole()) &&
                        OaiItemUtils.getActorRoleForDcName(dcLocalName).contains(c.getRole().getCode()) &&
                        Objects.nonNull(c.getActor().getName())).map(c -> new DcValue(c.getActor().getName()))
                .collect(Collectors.toList());
    }
}
