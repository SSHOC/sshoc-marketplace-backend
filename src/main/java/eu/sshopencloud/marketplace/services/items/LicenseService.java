package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.License;
import eu.sshopencloud.marketplace.repositories.items.LicenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepository licenseRepository;

    public List<License> getAllLicenses() {
        return licenseRepository.findAll(new Sort(Sort.Direction.ASC, "label"));
    }

}
