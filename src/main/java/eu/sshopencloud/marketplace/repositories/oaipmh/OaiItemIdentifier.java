package eu.sshopencloud.marketplace.repositories.oaipmh;

import eu.sshopencloud.marketplace.controllers.oaipmh.OaiItemUtils;
import eu.sshopencloud.marketplace.model.items.Item;
import io.gdcc.xoai.dataprovider.model.ItemIdentifier;
import io.gdcc.xoai.dataprovider.model.Set;
import lombok.Builder;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Builder
public class OaiItemIdentifier implements ItemIdentifier {

    private String identifier;
    private Date datestamp;
    private List<Set> sets;
    private boolean deleted;

    public static OaiItemIdentifier fromItem(Item item) {
        return OaiItemIdentifier.builder()
                .datestamp(Date.from(item.getLastInfoUpdate().toInstant()))
                .identifier(OaiItemUtils.buildOaiId(item.getPersistentId()))
                .deleted(false)
                .sets(List.of())
                .build();
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Instant getDatestamp() {
        return datestamp.toInstant();
    }

    @Override
    public List<Set> getSets() {
        return sets;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }
}
