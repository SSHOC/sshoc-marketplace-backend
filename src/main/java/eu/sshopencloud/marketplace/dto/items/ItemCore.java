package eu.sshopencloud.marketplace.dto.items;

import eu.sshopencloud.marketplace.dto.licenses.LicenseId;
import eu.sshopencloud.marketplace.dto.sources.SourceId;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.model.items.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ItemCore implements ItemRelationsCore {

    private String label;

    private String version;

    private String description;

    private List<LicenseId> licenses;

    private List<ItemContributorId> contributors;

    private List<ItemExternalIdCore> externalIds;

    private List<PropertyCore> properties;

    private List<RelatedItemCore> relatedItems;

    private List<String> accessibleAt;

    private SourceId source;

    private String sourceItemId;
}
