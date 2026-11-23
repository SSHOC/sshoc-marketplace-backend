package eu.sshopencloud.marketplace.model.skg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class Agent {

    //common properties
    @JsonProperty("local_identifier")
    private String localIdentifier;
    @JsonProperty("entity_type")
    private String entityType;
    private List<Identifier> identifiers;
    private String name;

    //person related properties
    private String givenName;
    private String familyName;
    private List<AgentAffiliations> affiliations;

    //organization related properties
    private String shortName;
    private List<String> otherNames;
    private String website;
    private String country;
    private List<String> types;
}
