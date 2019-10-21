package eu.sshopencloud.marketplace.controllers.licenses;

import eu.sshopencloud.marketplace.model.licenses.License;
import eu.sshopencloud.marketplace.services.licenses.LicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;

    @GetMapping("/licenses")
    public ResponseEntity<List<License>> getAllLicenses(@RequestParam(value = "q", required = false) String q) {
        List<License> licenses = licenseService.getLicenses(q);
        return ResponseEntity.ok(licenses);
    }

}
