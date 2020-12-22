package eu.sshopencloud.marketplace.mappers.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorExternalIdDto;
import eu.sshopencloud.marketplace.model.actors.ActorExternalId;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;


@Mapper
public interface ActorExternalIdMapper {
    ActorExternalIdMapper INSTANCE = Mappers.getMapper(ActorExternalIdMapper.class);

    ActorExternalIdDto toDto(ActorExternalId externalId);
    List<ActorExternalIdDto> toDto(List<ActorExternalId> externalIds);
}
