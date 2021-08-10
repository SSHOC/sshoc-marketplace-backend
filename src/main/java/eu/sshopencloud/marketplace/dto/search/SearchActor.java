package eu.sshopencloud.marketplace.dto.search;

import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorExternalId;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
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
    private String website;
    private String email;

    private List<ActorExternalId> externalIds;
    private List<Actor> affiliations;
    private List<ItemContributor> contributorTo;

}
