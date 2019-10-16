package eu.sshopencloud.marketplace.controllers.tools;

import eu.sshopencloud.marketplace.model.tools.Software;
import eu.sshopencloud.marketplace.services.tools.SoftwareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SoftwareController {

    private final SoftwareService softwareService;

    @GetMapping("/software")
    public ResponseEntity<List<Software>> getAllSoftware() {
        List<Software> software = softwareService.getAllSoftware();
        return ResponseEntity.ok(software);
    }

    @GetMapping("/software/{id}")
    public ResponseEntity<Software> getSoftware(@PathVariable("id") long id) {
        Software software = softwareService.getSoftware(id);
        return ResponseEntity.ok(software);
    }

}
