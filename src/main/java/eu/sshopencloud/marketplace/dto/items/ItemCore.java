package eu.sshopencloud.marketplace.dto.items;

import eu.sshopencloud.marketplace.dto.sources.SourceId;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.List;


@Data
@NoArgsConstructor
public class ItemCore implements ItemRelationsCore {

    @NotNull
    private String label;

    @Nullable
    private String version;

    private String description;

    private List<ItemContributorId> contributors;

    private List<ItemExternalIdCore> externalIds;

    private List<PropertyCore> properties;

    private List<RelatedItemCore> relatedItems;

    private List<ItemMediaCore> media;

    private ItemMediaCore thumbnail;

    private List<String> accessibleAt;

    @Nullable
    private SourceId source;

    @Nullable
    private String sourceItemId;
}
