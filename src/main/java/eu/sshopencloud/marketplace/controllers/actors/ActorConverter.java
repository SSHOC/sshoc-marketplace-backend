package eu.sshopencloud.marketplace.controllers.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.model.actors.Actor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ActorConverter {

    public Actor convert (ActorId actor) {
        Actor result = new Actor();
        result.setId(actor.getId());
        return result;
    }

}
