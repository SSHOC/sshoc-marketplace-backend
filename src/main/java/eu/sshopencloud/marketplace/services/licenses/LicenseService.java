package eu.sshopencloud.marketplace.services.licenses;

import eu.sshopencloud.marketplace.model.licenses.License;
import eu.sshopencloud.marketplace.repositories.licenses.LicenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepository licenseRepository;

    public List<License> getLicenses(String q) {
        ExampleMatcher queryLicenseMatcher = ExampleMatcher.matchingAny()
                .withMatcher("code", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("label", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        License queryLicense = new License();
        queryLicense.setCode(q);
        queryLicense.setLabel(q);

        return licenseRepository.findAll(Example.of(queryLicense, queryLicenseMatcher), new Sort(Sort.Direction.ASC, "label"));
    }

}
