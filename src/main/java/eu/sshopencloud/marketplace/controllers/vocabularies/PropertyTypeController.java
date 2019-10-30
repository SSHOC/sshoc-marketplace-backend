package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PropertyTypeController {

    @Value("${marketplace.pagination.default-perpage}")
    private Integer defualtPerpage;

    private final PropertyTypeService propertyTypeService;

    @GetMapping(path = "/property-types", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PropertyType>> getPropertyTypes(@RequestParam(value = "q", required = false) String q) {
        List<PropertyType> propertyTypes = propertyTypeService.getPropertyTypes(q, defualtPerpage);
        return ResponseEntity.ok(propertyTypes);
    }

}
