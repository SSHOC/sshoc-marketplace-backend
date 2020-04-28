package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelatedItemDto;
import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper
public interface ItemRelatedItemMapper {

    ItemRelatedItemMapper INSTANCE = Mappers.getMapper(ItemRelatedItemMapper.class);

    ItemRelatedItemDto toDto(ItemRelatedItem itemRelatedItem);

}
