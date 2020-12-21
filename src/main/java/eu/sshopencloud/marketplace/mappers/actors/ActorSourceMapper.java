package eu.sshopencloud.marketplace.mappers.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorSourceDto;
import eu.sshopencloud.marketplace.model.actors.ActorSource;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;


@Mapper
public interface ActorSourceMapper {
    ActorSourceMapper INSTANCE = Mappers.getMapper(ActorSourceMapper.class);

    ActorSourceDto toDto(ActorSource actorRole);
    List<ActorSourceDto> toDto(List<ActorSource> actorRoles);
}
