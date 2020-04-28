package eu.sshopencloud.marketplace.mappers.licenses;

import eu.sshopencloud.marketplace.dto.licenses.LicenseDto;
import eu.sshopencloud.marketplace.model.licenses.License;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface LicenseMapper {

    LicenseMapper INSTANCE = Mappers.getMapper(LicenseMapper.class);

    LicenseDto toDto(License license);

    List<LicenseDto> toDto(List<License> licenses);

}
