package eu.sshopencloud.marketplace.dto.items;

import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItemContributorId {

    private ActorId actor;

    private ActorRoleId role;

}
