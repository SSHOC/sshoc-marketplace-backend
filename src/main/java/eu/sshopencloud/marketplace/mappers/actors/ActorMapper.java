package eu.sshopencloud.marketplace.mappers.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorDto;
import eu.sshopencloud.marketplace.model.actors.Actor;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ActorMapper {

    ActorMapper INSTANCE = Mappers.getMapper(ActorMapper.class);

    ActorDto toDto(Actor actor);

    List<ActorDto> toDto(List<Actor> actors);

}
