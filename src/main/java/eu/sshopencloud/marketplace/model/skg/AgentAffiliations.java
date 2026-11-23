package eu.sshopencloud.marketplace.model.skg;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class AgentAffiliations {

    private String affiliation;
}
