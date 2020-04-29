package eu.sshopencloud.marketplace.services.licenses;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.licenses.LicenseDto;
import eu.sshopencloud.marketplace.dto.licenses.PaginatedLicenses;
import eu.sshopencloud.marketplace.mappers.licenses.LicenseMapper;
import eu.sshopencloud.marketplace.model.licenses.License;
import eu.sshopencloud.marketplace.repositories.licenses.LicenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepository licenseRepository;

    public PaginatedLicenses getLicenses(String q, PageCoords pageCoords) {
        ExampleMatcher queryLicenseMatcher = ExampleMatcher.matchingAny()
                .withMatcher("code", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("label", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        License queryLicense = new License();
        queryLicense.setCode(q);
        queryLicense.setLabel(q);

        Page<License> licensesPage = licenseRepository.findAll(Example.of(queryLicense, queryLicenseMatcher),
                PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));

        List<LicenseDto> licenses = licensesPage.stream().map(LicenseMapper.INSTANCE::toDto).collect(Collectors.toList());

        return PaginatedLicenses.builder().licenses(licenses)
                .count(licensesPage.getContent().size()).hits(licensesPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(licensesPage.getTotalPages())
                .build();
    }

}
