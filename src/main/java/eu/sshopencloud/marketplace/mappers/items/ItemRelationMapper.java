package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelationDto;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ItemRelationMapper {

    ItemRelationMapper INSTANCE = Mappers.getMapper(ItemRelationMapper.class);

    ItemRelationDto toDto(ItemRelation itemRelation);

    List<ItemRelationDto> toDto(List<ItemRelation> itemRelations);

}
