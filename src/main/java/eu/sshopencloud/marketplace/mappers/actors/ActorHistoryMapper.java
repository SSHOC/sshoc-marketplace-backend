package eu.sshopencloud.marketplace.mappers.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorHistoryDto;
import eu.sshopencloud.marketplace.model.actors.ActorHistory;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ActorHistoryMapper {

    ActorHistoryMapper INSTANCE = Mappers.getMapper(ActorHistoryMapper.class);

    ActorHistoryDto toDto(ActorHistory actorHistory);

    List<ActorHistoryDto> toDto(List<ActorHistory> actorHistory);
}
