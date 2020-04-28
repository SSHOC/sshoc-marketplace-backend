package eu.sshopencloud.marketplace.mappers.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.ConceptRelationDto;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelation;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ConceptRelationMapper {

    ConceptRelationMapper INSTANCE = Mappers.getMapper(ConceptRelationMapper.class);

    ConceptRelationDto toDto(ConceptRelation conceptRelation);

    List<ConceptRelationDto> toDto(List<ConceptRelation> conceptRelations);

}
