package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PropertyTypeController {

    private final PropertyTypeService propertyTypeService;

    @GetMapping(path = "/property-types", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PropertyType>> getPropertyTypes(@RequestParam(value = "q", required = false) String q) {
        List<PropertyType> propertyTypes = propertyTypeService.getPropertyTypes(q);
        return ResponseEntity.ok(propertyTypes);
    }

}
