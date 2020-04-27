package eu.sshopencloud.marketplace.mappers.vocabularies;


import eu.sshopencloud.marketplace.dto.vocabularies.ConceptBasicDto;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ConceptBasicMapper {

    ConceptBasicMapper INSTANCE = Mappers.getMapper(ConceptBasicMapper.class);

    ConceptBasicDto toDto(Concept concept);

    List<ConceptBasicDto> toDto(List<Concept> concepts);


}
