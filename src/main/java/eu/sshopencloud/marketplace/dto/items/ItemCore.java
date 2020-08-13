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
public class ItemCore {

    private String label;

    private String version;

    private String description;

    private List<LicenseId> licenses;

    private List<ItemContributorId> contributors;

    private List<PropertyCore> properties;

    private List<String> accessibleAt;

    private SourceId source;

    private ItemStatus status;

    private String sourceItemId;

    private Long prevVersionId;

}
