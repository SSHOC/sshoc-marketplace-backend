package eu.sshopencloud.marketplace.mappers.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyDto;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface VocabularyMapper {

    VocabularyMapper INSTANCE = Mappers.getMapper(VocabularyMapper.class);

    VocabularyDto toDto(Vocabulary vocabulary);

    List<VocabularyDto> toDto(List<Vocabulary> vocabularies);

}
