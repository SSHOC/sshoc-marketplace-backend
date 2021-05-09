package eu.sshopencloud.marketplace.dto.actors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActorExternalIdDto {
    private ActorSourceDto serviceIdentifier;
    private String identifier;
}
