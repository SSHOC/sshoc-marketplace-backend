package eu.sshopencloud.marketplace.repositories.oaipmh;

import eu.sshopencloud.marketplace.model.items.Item;
import io.gdcc.xoai.dataprovider.model.ItemIdentifier;
import io.gdcc.xoai.dataprovider.model.Set;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
public class OaiItemIdentifier implements ItemIdentifier {

    private String identifier;
    private Instant datestamp;
    private List<Set> sets;
    private boolean deleted;

    public static OaiItemIdentifier fromItem(Item item) {
        return OaiItemIdentifier.builder()
                .datestamp(item.getLastInfoUpdate().toInstant())
                .identifier(OaiItemUtils.buildOaiId(item.getPersistentId()))
                .deleted(false)
                .sets(List.of())
                .build();
    }
}
