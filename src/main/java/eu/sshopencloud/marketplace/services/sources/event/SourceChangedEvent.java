package eu.sshopencloud.marketplace.services.sources.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SourceChangedEvent {

    private Long id;

    private boolean deleted;

}
