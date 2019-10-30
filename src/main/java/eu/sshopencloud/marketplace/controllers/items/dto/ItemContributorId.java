package eu.sshopencloud.marketplace.controllers.items.dto;

import eu.sshopencloud.marketplace.controllers.actors.dto.ActorId;
import eu.sshopencloud.marketplace.controllers.actors.dto.ActorRoleId;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItemContributorId {

    private ActorId actor;

    private ActorRoleId role;

}
