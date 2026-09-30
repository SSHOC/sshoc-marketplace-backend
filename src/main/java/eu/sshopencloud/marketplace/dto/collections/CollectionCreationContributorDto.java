package eu.sshopencloud.marketplace.dto.collections;

import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionCreationContributorDto {

    private ActorId actor;
    private ActorRoleId role;
}
