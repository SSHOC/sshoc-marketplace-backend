package eu.sshopencloud.marketplace.controllers.items.dto;

import eu.sshopencloud.marketplace.controllers.licenses.dto.LicenseId;
import eu.sshopencloud.marketplace.controllers.vocabularies.dto.PropertyCore;
import eu.sshopencloud.marketplace.model.items.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ItemCore {

    private ItemCategory category;

    private String label;

    private String version;

    private String description;

    private List<LicenseId> licenses;

    private List<ItemContributorId> contributors;

    private List<PropertyCore> properties;

    private String accessibleAt;

    private Long prevVersionId;

}
