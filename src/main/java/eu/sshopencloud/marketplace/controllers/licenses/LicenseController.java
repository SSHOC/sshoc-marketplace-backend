package eu.sshopencloud.marketplace.controllers.licenses;

import eu.sshopencloud.marketplace.model.licenses.License;
import eu.sshopencloud.marketplace.services.licenses.LicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LicenseController {

    @Value("${marketplace.pagination.default-perpage}")
    private Integer defualtPerpage;

    private final LicenseService licenseService;

    @GetMapping(path = "/licenses", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<License>> getLicenses(@RequestParam(value = "q", required = false) String q) {
        List<License> licenses = licenseService.getLicenses(q, defualtPerpage);
        return ResponseEntity.ok(licenses);
    }

}
