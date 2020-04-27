package eu.sshopencloud.marketplace.controllers.licenses;

import eu.sshopencloud.marketplace.dto.licenses.LicenseDto;
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
    public ResponseEntity<List<LicenseDto>> getLicenses(@RequestParam(value = "q", required = false) String q) {
        return ResponseEntity.ok(licenseService.getLicenses(q, defualtPerpage));
    }

}
