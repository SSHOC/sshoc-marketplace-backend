package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.items.ItemCommentDto;
import eu.sshopencloud.marketplace.model.items.ItemComment;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ItemCommentMapper {

    ItemCommentMapper INSTANCE = Mappers.getMapper(ItemCommentMapper.class);

    ItemCommentDto toDto(ItemComment itemComment);

    List<ItemCommentDto> toDto(List<ItemComment> itemComments);

}
