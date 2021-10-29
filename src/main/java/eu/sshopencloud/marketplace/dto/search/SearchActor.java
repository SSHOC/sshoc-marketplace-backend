package eu.sshopencloud.marketplace.dto.search;

import eu.sshopencloud.marketplace.dto.actors.ActorDto;
import eu.sshopencloud.marketplace.dto.actors.ActorExternalIdDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchActor {

    private Long id;
    private String name;
    private List<ActorExternalIdDto> externalIds;
    private String website;
    private String email;
    private List<ActorDto> affiliations;

}
