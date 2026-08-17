package eu.sshopencloud.marketplace.mappers.collections;

import eu.sshopencloud.marketplace.dto.collections.CollectionDto;
import eu.sshopencloud.marketplace.model.collections.Collection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Mapper used for list of collections only. It doesn't map collection items (that are wrapped into paginated object)
 */
@Mapper
public interface CollectionListMapper {

    CollectionListMapper INSTANCE = Mappers.getMapper(CollectionListMapper.class);

    @Mapping(target = "collectionItems", ignore = true)
    CollectionDto toDto(Collection collection);

    List<CollectionDto> toDto(List<Collection> collections);
}
