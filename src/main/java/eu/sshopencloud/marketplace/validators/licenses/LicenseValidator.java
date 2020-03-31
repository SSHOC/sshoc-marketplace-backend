package eu.sshopencloud.marketplace.validators.licenses;

import eu.sshopencloud.marketplace.dto.licenses.LicenseId;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.licenses.License;
import eu.sshopencloud.marketplace.repositories.licenses.LicenseRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LicenseValidator {

    private final LicenseRepository licenseRepository;

    public List<License> validate(List<LicenseId> licenseIds, Item item, Errors errors, String nestedPath) {
        List<License> licenses = new ArrayList<License>();
        if (licenseIds != null) {
            for (int i = 0; i < licenseIds.size(); i++) {
                errors.pushNestedPath(nestedPath + "[" + i + "]");
                License license = validate(licenseIds.get(i), errors);
                if (license != null) {
                    licenses.add(license);
                }
                errors.popNestedPath();
            }
        }
        if (item.getLicenses() != null) {
            item.getLicenses().clear();
        }
        return licenses;
    }

    public License validate(LicenseId licenseId, Errors errors) {
        if (StringUtils.isBlank(licenseId.getCode())) {
            errors.rejectValue("code", "field.required", "License code is required.");
            return null;
        }
        Optional<License> licenseHolder = licenseRepository.findById(licenseId.getCode());
        if (!licenseHolder.isPresent()) {
            errors.rejectValue("code", "field.notExist", "License does not exist.");
            return null;
        } else {
            return licenseHolder.get();
        }
    }

}
