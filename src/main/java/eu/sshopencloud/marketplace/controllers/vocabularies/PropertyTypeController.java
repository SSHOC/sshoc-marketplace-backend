package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeDto;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PropertyTypeController {

    private final PageCoordsValidator pageCoordsValidator;

    private final PropertyTypeService propertyTypeService;


    @GetMapping(path = "/property-types", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PropertyTypeDto>> getPropertyTypes(@RequestParam(value = "q", required = false) String q,
                                                                  @RequestParam(value = "page", required = false) Integer page,
                                                                  @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(propertyTypeService.getPropertyTypes(q, pageCoordsValidator.validate(page, perpage)));
    }

}
