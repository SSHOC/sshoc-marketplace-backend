package eu.sshopencloud.marketplace.services.licenses;

import eu.sshopencloud.marketplace.dto.licenses.LicenseTypeDto;
import eu.sshopencloud.marketplace.mappers.licenses.LicenseTypeMapper;
import eu.sshopencloud.marketplace.repositories.licenses.LicenseTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LicenseTypeService {

    private final LicenseTypeRepository licenseTypeRepository;

    public List<LicenseTypeDto> getAllLicenseTypes() {
        return LicenseTypeMapper.INSTANCE.toDto(licenseTypeRepository.findAll(Sort.by(Sort.Order.asc("ord"))));
    }

}
