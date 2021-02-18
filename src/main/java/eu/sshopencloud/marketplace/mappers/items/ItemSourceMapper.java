package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.items.ItemSourceDto;
import eu.sshopencloud.marketplace.model.items.ItemSource;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;


@Mapper
public interface ItemSourceMapper {
    ItemSourceMapper INSTANCE = Mappers.getMapper(ItemSourceMapper.class);

    ItemSourceDto toDto(ItemSource itemSource);
    List<ItemSourceDto> toDto(List<ItemSource> itemSources);
}
