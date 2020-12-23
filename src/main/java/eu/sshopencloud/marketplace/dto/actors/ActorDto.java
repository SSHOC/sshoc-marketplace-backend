package eu.sshopencloud.marketplace.dto.actors;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ActorDto {

    private Long id;

    private String name;

    private List<ActorExternalIdDto> externalIds;

    private String website;

    private String email;

    private List<ActorDto> affiliations;

}
