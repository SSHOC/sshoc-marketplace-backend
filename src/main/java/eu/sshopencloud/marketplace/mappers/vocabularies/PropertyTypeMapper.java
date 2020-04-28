package eu.sshopencloud.marketplace.mappers.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeDto;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PropertyTypeMapper {

    PropertyTypeMapper INSTANCE = Mappers.getMapper(PropertyTypeMapper.class);

    PropertyTypeDto toDto(PropertyType propertyType);

    List<PropertyTypeDto> toDto(List<PropertyType> propertyTypes);

}
