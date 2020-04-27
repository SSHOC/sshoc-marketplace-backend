package eu.sshopencloud.marketplace.dto.items;

import eu.sshopencloud.marketplace.dto.actors.ActorDto;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleDto;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class ItemContributorDto {

    private ActorDto actor;

    private ActorRoleDto role;

}
