package eu.sshopencloud.marketplace.controllers.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ActorRoleConverter {

    public ActorRole convert(ActorRoleId actorRole) {
        ActorRole result = new ActorRole();
        result.setCode(actorRole.getCode());
        return result;
    }

}
