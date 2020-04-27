package eu.sshopencloud.marketplace.mappers.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorRoleDto;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ActorRoleMapper {

    ActorRoleMapper INSTANCE = Mappers.getMapper(ActorRoleMapper.class);

    ActorRoleDto toDto(ActorRole actorRole);

    List<ActorRoleDto> toDto(List<ActorRole> actorRoles);

}
