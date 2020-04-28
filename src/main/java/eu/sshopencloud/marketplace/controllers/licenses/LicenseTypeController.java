package eu.sshopencloud.marketplace.controllers.licenses;

import eu.sshopencloud.marketplace.dto.licenses.LicenseTypeDto;
import eu.sshopencloud.marketplace.services.licenses.LicenseTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LicenseTypeController {

    private final LicenseTypeService licenseTypeService;

    @GetMapping(path = "/license-types", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LicenseTypeDto>> getAllLicenseTypes() {
        return ResponseEntity.ok(licenseTypeService.getAllLicenseTypes());
    }

}
