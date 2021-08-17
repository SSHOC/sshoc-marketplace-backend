package eu.sshopencloud.marketplace.dto.actors;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@NoArgsConstructor
public class ActorDto {

    @NotNull
    private Long id;

    @NotNull
    private String name;

    private List<ActorExternalIdDto> externalIds;

    private String website;

    private String email;

    private List<ActorDto> affiliations;

}
