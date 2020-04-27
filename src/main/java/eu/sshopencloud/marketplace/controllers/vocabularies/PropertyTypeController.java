package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeDto;
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

    private final PropertyTypeService propertyTypeService;
    @Value("${marketplace.pagination.default-perpage}")
    private Integer defualtPerpage;
    @Value("${marketplace.pagination.maximal-perpage}")
    private Integer maximalPerpage;

    @GetMapping(path = "/property-types", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PropertyTypeDto>> getPropertyTypes(@RequestParam(value = "q", required = false) String q,
                                                                  @RequestParam(value = "page", required = false) Integer page,
                                                                  @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        perpage = perpage == null ? defualtPerpage : perpage;
        if (perpage > maximalPerpage) {
            throw new PageTooLargeException(maximalPerpage);
        }
        page = page == null ? 1 : page;

        return ResponseEntity.ok(propertyTypeService.getPropertyTypes(q, page, perpage));
    }

}
