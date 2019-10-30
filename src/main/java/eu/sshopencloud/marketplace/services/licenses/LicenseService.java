package eu.sshopencloud.marketplace.services.licenses;

import eu.sshopencloud.marketplace.model.licenses.License;
import eu.sshopencloud.marketplace.repositories.licenses.LicenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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

}
