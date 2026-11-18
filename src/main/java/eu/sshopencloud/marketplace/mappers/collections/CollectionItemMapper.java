package eu.sshopencloud.marketplace.mappers.collections;

import eu.sshopencloud.marketplace.dto.collections.CollectionItemDto;
import eu.sshopencloud.marketplace.model.collections.CollectionItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CollectionItemMapper {

    CollectionItemMapper INSTANCE = Mappers.getMapper(CollectionItemMapper.class);

    @Mapping(source = "item.versionedItem.persistentId", target = "persistentId")
    CollectionItemDto toDto(CollectionItem collectionItem);

    List<CollectionItemDto> toDto(List<CollectionItem> collectionItems);
}
