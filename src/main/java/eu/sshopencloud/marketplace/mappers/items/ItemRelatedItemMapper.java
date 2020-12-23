package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelatedItemDto;
import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;


@Mapper
public interface ItemRelatedItemMapper {

    ItemRelatedItemMapper INSTANCE = Mappers.getMapper(ItemRelatedItemMapper.class);

    @Mappings({
            @Mapping(source = "subject.versionedItem.persistentId", target = "subject.persistentId"),
            @Mapping(source = "object.versionedItem.persistentId", target = "object.persistentId")
    })
    ItemRelatedItemDto toDto(ItemRelatedItem itemRelatedItem);

}
