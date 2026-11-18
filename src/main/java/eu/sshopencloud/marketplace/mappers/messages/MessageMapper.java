package eu.sshopencloud.marketplace.mappers.messages;

import eu.sshopencloud.marketplace.dto.inbox.MessageDto;
import eu.sshopencloud.marketplace.model.inbox.Message;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MessageMapper {

    MessageMapper INSTANCE = Mappers.getMapper(MessageMapper.class);

    MessageDto toDto(Message message);

    List<MessageDto> toDto(List<Message> actors);

}
