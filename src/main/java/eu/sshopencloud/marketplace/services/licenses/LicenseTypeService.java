package eu.sshopencloud.marketplace.services.licenses;

import eu.sshopencloud.marketplace.model.licenses.LicenseType;
import eu.sshopencloud.marketplace.repositories.licenses.LicenseTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LicenseTypeService {

    private final LicenseTypeRepository licenseTypeRepository;

    public List<LicenseType> getAllLicenseTypes() {
        return licenseTypeRepository.findAll(new Sort(Sort.Direction.ASC, "ord"));
    }

}