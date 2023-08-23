package eu.sshopencloud.marketplace.repositories.oaipmh;

import eu.sshopencloud.marketplace.controllers.oaipmh.OaiItemUtils;
import eu.sshopencloud.marketplace.dto.items.RelatedItemDto;
import io.gdcc.xoai.dataprovider.model.Item;
import io.gdcc.xoai.dataprovider.model.Set;
import io.gdcc.xoai.model.oaipmh.results.record.Metadata;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class OaiItem implements Item {

    private final eu.sshopencloud.marketplace.model.items.Item item;
    private final List<RelatedItemDto> relatedItems;

    @Override
    public Metadata getMetadata() {
        return new Metadata(OaiItemUtils.convertToOaiDc(this));
    }

    @Override
    public String getIdentifier() {
        return item.getPersistentId();
    }

    @Override
    public Instant getDatestamp() {
        return item.getLastInfoUpdate().toInstant();
    }

    @Override
    public List<Set> getSets() {
        return List.of();
    }

    @Override
    public boolean isDeleted() {
        return false;
    }
}
