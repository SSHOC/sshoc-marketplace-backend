package eu.sshopencloud.marketplace.controllers.licenses;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.licenses.PaginatedLicenses;
import eu.sshopencloud.marketplace.services.licenses.LicenseService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LicenseController {

    private final PageCoordsValidator pageCoordsValidator;

    private final LicenseService licenseService;

    @GetMapping(path = "/licenses", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedLicenses> getLicenses(@RequestParam(value = "q", required = false) String q,
                                                         @RequestParam(value = "page", required = false) Integer page,
                                                         @RequestParam(value = "perpage", required = false) Integer perpage)
        throws PageTooLargeException {
        return ResponseEntity.ok(licenseService.getLicenses(q, pageCoordsValidator.validate(page, perpage)));
    }

}
