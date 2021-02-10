package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.items.ItemExternalIdDto;
import eu.sshopencloud.marketplace.model.items.ItemExternalId;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;


@Mapper
public interface ItemExternalIdMapper {
    ItemExternalIdMapper INSTANCE = Mappers.getMapper(ItemExternalIdMapper.class);

    ItemExternalIdDto toDto(ItemExternalId itemExternalId);
    List<ItemExternalIdDto> toDto(List<ItemExternalId> itemExternalIds);
}
