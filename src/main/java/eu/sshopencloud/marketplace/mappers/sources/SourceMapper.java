package eu.sshopencloud.marketplace.mappers.sources;

import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.model.sources.Source;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SourceMapper {

    SourceMapper INSTANCE = Mappers.getMapper(SourceMapper.class);

    SourceDto toDto(Source source);

    List<SourceDto> toDto(List<Source> sources);

}
