package eu.sshopencloud.marketplace.services.licenses;

import eu.sshopencloud.marketplace.dto.licenses.LicenseId;
import eu.sshopencloud.marketplace.model.licenses.License;
import eu.sshopencloud.marketplace.repositories.licenses.LicenseRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepository licenseRepository;

    public List<License> getLicenses(String q, int perpage) {
        ExampleMatcher queryLicenseMatcher = ExampleMatcher.matchingAny()
                .withMatcher("code", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("label", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        License queryLicense = new License();
        queryLicense.setCode(q);
        queryLicense.setLabel(q);

        Page<License> licenses = licenseRepository.findAll(Example.of(queryLicense, queryLicenseMatcher), PageRequest.of(0, perpage, new Sort(Sort.Direction.ASC, "label")));
        return licenses.getContent();
    }


    public List<License> validate(String prefix, List<LicenseId> licenses) throws DataViolationException {
        List<License> result = new ArrayList<License>();
        if (licenses != null) {
            for (int i = 0; i < licenses.size(); i++) {
                result.add(validate(prefix + "[" + i + "].", licenses.get(i)));
            }
        }
        return result;
    }

    public License validate(String prefix, LicenseId license) throws DataViolationException {
        if (license.getCode() == null) {
            throw new DataViolationException(prefix + "code", license.getCode());
        }
        Optional<License> result = licenseRepository.findById(license.getCode());
        if (!result.isPresent()) {
            throw new DataViolationException(prefix + "code", license.getCode());
        }
        return result.get();
    }

}
