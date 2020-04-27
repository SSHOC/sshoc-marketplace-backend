package eu.sshopencloud.marketplace.mappers.licenses;

import eu.sshopencloud.marketplace.dto.licenses.LicenseTypeDto;
import eu.sshopencloud.marketplace.model.licenses.LicenseType;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface LicenseTypeMapper {

    LicenseTypeMapper INSTANCE = Mappers.getMapper(LicenseTypeMapper.class);

    LicenseTypeDto toDto(LicenseType licenseType);

    List<LicenseTypeDto> toDto(List<LicenseType> licenseTypes);

}
