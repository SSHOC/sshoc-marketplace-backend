package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.MediaInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper
interface MediaInfoMapper {
    MediaInfoMapper INSTANCE = Mappers.getMapper(MediaInfoMapper.class);

    @Mapping(source = "id", target = "mediaId")
    MediaInfo toDto(MediaFile mediaFile);
}
