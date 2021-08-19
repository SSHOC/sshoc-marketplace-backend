package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PaginatedPropertyTypes;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypesReordering;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.services.vocabularies.exception.PropertyTypeAlreadyExistsException;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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


    @Operation(summary = "Get all property types in pages")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedPropertyTypes> getPropertyTypes(@RequestParam(value = "q", required = false) String q,
                                                                   @RequestParam(value = "page", required = false) Integer page,
                                                                   @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {

        return ResponseEntity.ok(propertyTypeService.getPropertyTypes(q, pageCoordsValidator.validate(page, perpage)));
    }

    @Operation(summary = "Get property type by code ")
    @GetMapping(value = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PropertyTypeDto> getPropertyType(@PathVariable("code") String code) {
        PropertyTypeDto propertyType = propertyTypeService.getPropertyType(code);
        return ResponseEntity.ok(propertyType);
    }

    @Operation(summary = "Create property type ")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PropertyTypeDto> createPropertyType(@Parameter(
            description = "Created property type",
            required = true,
            schema = @Schema(implementation = PropertyTypeCore.class)) @RequestBody PropertyTypeCore propertyTypeData)
            throws PropertyTypeAlreadyExistsException {

        PropertyTypeDto propertyType = propertyTypeService.createPropertyType(propertyTypeData);
        return ResponseEntity.ok(propertyType);
    }

    @Operation(summary = "Update property type by code")
    @PutMapping(value = "/{code}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PropertyTypeDto> updatePropertyType(@PathVariable("code") String propertyTypeCode,
                                                              @Parameter(
                                                                      description = "Updated property type",
                                                                      required = true,
                                                                      schema = @Schema(implementation = PropertyTypeCore.class)) @RequestBody PropertyTypeCore propertyTypeData) {

        PropertyTypeDto propertyType = propertyTypeService.updatePropertyType(propertyTypeCode, propertyTypeData);
        return ResponseEntity.ok(propertyType);
    }

    @Operation(summary = "Delete property type by code")
    @DeleteMapping("/{code}")
    public ResponseEntity<PropertyTypeDto> deletePropertyType(
            @PathVariable("code") String propertyTypeCode,
            @RequestParam(value = "force", required = false, defaultValue = "false") boolean force) {

        propertyTypeService.removePropertyType(propertyTypeCode, force);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reorganize property type order")
    @PostMapping(value = "/reorder", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> reorderPropertyTypes(@Parameter(
            description = "Reordered property type",
            required = true,
            schema = @Schema(implementation = PropertyTypesReordering.class)) @RequestBody PropertyTypesReordering reordering) {
        propertyTypeService.reorderPropertyTypes(reordering);
        return ResponseEntity.ok().build();
    }
}
