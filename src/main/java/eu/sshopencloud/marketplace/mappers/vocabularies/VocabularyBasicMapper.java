package eu.sshopencloud.marketplace.mappers.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyBasicDto;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.projection.VocabularyBasicView;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface VocabularyBasicMapper {

    VocabularyBasicMapper INSTANCE = Mappers.getMapper(VocabularyBasicMapper.class);

    VocabularyBasicDto toDto(Vocabulary vocabulary);

    List<VocabularyBasicDto> toDto(List<Vocabulary> vocabularies);

    VocabularyBasicDto toDtoBasic(VocabularyBasicView vocabulary);

    List<VocabularyBasicDto> toDtoBasic(List<VocabularyBasicView> vocabularies);

}
