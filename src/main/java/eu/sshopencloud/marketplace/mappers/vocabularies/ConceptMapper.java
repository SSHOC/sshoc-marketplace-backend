package eu.sshopencloud.marketplace.mappers.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.ConceptDto;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ConceptMapper {

    ConceptMapper INSTANCE = Mappers.getMapper(ConceptMapper.class);

    ConceptDto toDto(Concept concept);

    List<ConceptDto> toDto(List<Concept> concepts);

}
