package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.model.items.Service;
import eu.sshopencloud.marketplace.model.items.Tool;
import eu.sshopencloud.marketplace.services.items.ServiceService;
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
public class ServiceController {

    private final ServiceService serviceService;

    @GetMapping("/services")
    public ResponseEntity<List<Service>> getAllServices() {
        List<Service> services = serviceService.getAllServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<Service> getService(@PathVariable("id") long id) {
        Service service = serviceService.getService(id);
        return ResponseEntity.ok(service);
    }

}
