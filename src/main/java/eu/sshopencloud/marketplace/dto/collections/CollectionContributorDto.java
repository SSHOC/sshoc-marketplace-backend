package eu.sshopencloud.marketplace.dto.collections;

import eu.sshopencloud.marketplace.dto.actors.ActorDto;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleDto;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CollectionContributorDto {

    private ActorDto actor;

    private ActorRoleDto role;
}
