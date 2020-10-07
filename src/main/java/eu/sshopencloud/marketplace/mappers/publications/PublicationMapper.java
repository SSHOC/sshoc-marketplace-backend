package eu.sshopencloud.marketplace.mappers.publications;

import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.model.publications.Publication;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PublicationMapper {

    PublicationMapper INSTANCE = Mappers.getMapper(PublicationMapper.class);

    PublicationDto toDto(Publication publication);

    List<PublicationDto> toDto(List<Publication> publications);

}