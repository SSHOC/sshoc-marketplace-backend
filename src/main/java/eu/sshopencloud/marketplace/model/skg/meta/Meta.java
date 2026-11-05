package eu.sshopencloud.marketplace.model.skg.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Meta {

    @JsonProperty("local_identifier")
    private String localIdentifier;
    @JsonProperty("entity_type")
    private String entityType;
    @JsonProperty("next_paeg")
    private PageInfo nextPage;
    @JsonProperty("prev_page")
    private PageInfo prevPage;
    @JsonProperty("last_page")
    private PageInfo lastPage;
    @JsonProperty("part_of")
    private PartOf partOf;
}
