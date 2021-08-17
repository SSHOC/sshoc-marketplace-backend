package eu.sshopencloud.marketplace.dto.actors;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;

import java.util.List;

@Data
@NoArgsConstructor
public class ActorCore {

    @NotNull
    private String name;

    private List<ActorExternalIdCore> externalIds;

    private String website;

    private String email;

    private List<ActorId> affiliations;

}
