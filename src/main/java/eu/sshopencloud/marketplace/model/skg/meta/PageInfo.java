package eu.sshopencloud.marketplace.model.skg.meta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageInfo {

    @JsonProperty("local_identifier")
    private String localIdentifier;

    @JsonProperty("entity_type")
    private String entityType;

    @JsonProperty("total_items")
    private Long totalItems;
}
