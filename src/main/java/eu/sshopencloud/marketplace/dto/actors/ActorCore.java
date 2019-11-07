package eu.sshopencloud.marketplace.dto.actors;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ActorCore {

    private String name;

    private String website;

    private String email;

    private List<ActorId> affiliations;

}
