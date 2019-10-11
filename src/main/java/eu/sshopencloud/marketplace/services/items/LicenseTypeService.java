package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.LicenseType;
import eu.sshopencloud.marketplace.repositories.items.LicenseTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LicenseTypeService {

    private final LicenseTypeRepository licenseTypeRepository;

    public List<LicenseType> getAllLicenseTypes() {
        return licenseTypeRepository.findAll(new Sort(Sort.Direction.ASC, "ord"));
    }

}
