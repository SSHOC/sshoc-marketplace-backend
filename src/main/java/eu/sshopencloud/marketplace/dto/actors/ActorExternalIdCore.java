package eu.sshopencloud.marketplace.dto.actors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActorExternalIdCore {

    @NotNull
    private ActorSourceId serviceIdentifier;

    @NotNull
    private String identifier;
}
