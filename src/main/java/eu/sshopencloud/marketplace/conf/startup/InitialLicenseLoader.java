package eu.sshopencloud.marketplace.conf.startup;

import eu.sshopencloud.marketplace.model.licenses.License;
import eu.sshopencloud.marketplace.model.licenses.LicenseType;
import eu.sshopencloud.marketplace.repositories.licenses.LicenseRepository;
import eu.sshopencloud.marketplace.repositories.licenses.LicenseTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialLicenseLoader {

    private final LicenseTypeRepository licenseTypeRepository;

    private final LicenseRepository licenseRepository;

    // TODO load from ttl and SKOS ?
    public void loadLicenseData() {
        log.debug("Loading license data");

        Map<String, List<Object>> data = YamlLoader.loadYamlData("initial-data/license-data.yml");

        List<LicenseType> licenseTypes = YamlLoader.getObjects(data, "LicenseType");
        licenseTypeRepository.saveAll(licenseTypes);
        log.debug("Loaded " + licenseTypes.size()  + " LicenseType objects");

        List<License> licenses = YamlLoader.getObjects(data, "License");
        licenseRepository.saveAll(licenses);
        log.debug("Loaded " + licenses.size()  + " License objects");
    }

}
