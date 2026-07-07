package eu.sshopencloud.marketplace.model.skg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class ResearchProduct {

    @JsonProperty("local_identifier")
    private String localIdentifier;

    @JsonProperty("@product_type")
    private String productType;

    @JsonProperty("entity_type")
    private String entityType;

    private Map<String, String> titles;
    private Map<String, List<String>> abstracts;
    private List<Contribution> contributions;
    private List<RelatedProduct> relatedProducts;

    private List<Map<String, Map<String, String>>> manifestations;
}
