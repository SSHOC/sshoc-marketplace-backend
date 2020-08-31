package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.vocabularies.PaginatedPropertyTypes;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypesReordering;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.services.vocabularies.exception.PropertyTypeAlreadyExistsException;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/property-types")
@RequiredArgsConstructor
public class PropertyTypeController {

    private final PageCoordsValidator pageCoordsValidator;
    private final PropertyTypeService propertyTypeService;


    @GetMapping
    public ResponseEntity<PaginatedPropertyTypes> getPropertyTypes(@RequestParam(value = "q", required = false) String q,
                                                                   @RequestParam(value = "page", required = false) Integer page,
                                                                   @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {

        return ResponseEntity.ok(propertyTypeService.getPropertyTypes(q, pageCoordsValidator.validate(page, perpage)));
    }

    @PostMapping("/{code}")
    public ResponseEntity<PropertyTypeDto> postPropertyType(@PathVariable("code") String propertyTypeCode,
                                                            @RequestBody PropertyTypeCore propertyTypeData)
            throws PropertyTypeAlreadyExistsException {

        PropertyTypeDto propertyType = propertyTypeService.createPropertyType(propertyTypeCode, propertyTypeData);
        return ResponseEntity.ok(propertyType);
    }

    @PutMapping("/{code}")
    public ResponseEntity<PropertyTypeDto> putPropertyType(@PathVariable("code") String propertyTypeCode,
                                                           @RequestBody PropertyTypeCore propertyTypeData) {

        PropertyTypeDto propertyType = propertyTypeService.updatePropertyType(propertyTypeCode, propertyTypeData);
        return ResponseEntity.ok(propertyType);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<PropertyTypeDto> deletePropertyType(@PathVariable("code") String propertyTypeCode) {
        propertyTypeService.removePropertyType(propertyTypeCode);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reorder")
    public ResponseEntity<Void> reorderPropertyTypes(@RequestBody PropertyTypesReordering reordering) {
        propertyTypeService.reorderPropertyTypes(reordering);
        return ResponseEntity.ok().build();
    }
}
