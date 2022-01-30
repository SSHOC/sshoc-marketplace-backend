package eu.sshopencloud.marketplace.dto.actors;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
public class ActorHistoryDto {

    @NotNull
    private Long id;

    ActorCore actor;

    private ZonedDateTime dateCreated;

    private String history;

}
