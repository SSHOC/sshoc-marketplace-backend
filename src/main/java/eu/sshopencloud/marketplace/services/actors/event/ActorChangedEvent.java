package eu.sshopencloud.marketplace.services.actors.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ActorChangedEvent {

    private Long id;

    private boolean deleted;

}
