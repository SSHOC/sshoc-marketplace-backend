package eu.sshopencloud.marketplace.mappers.collections;

import eu.sshopencloud.marketplace.dto.collections.CollectionDto;
import eu.sshopencloud.marketplace.model.collections.Collection;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CollectionMapper {

    CollectionMapper INSTANCE = Mappers.getMapper(CollectionMapper.class);

    CollectionDto toDto(Collection collection);

    List<CollectionDto> toDto(List<Collection> collections);
}
