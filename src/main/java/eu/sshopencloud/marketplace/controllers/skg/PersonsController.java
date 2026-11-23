package eu.sshopencloud.marketplace.controllers.skg;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.model.skg.JsonLdDocument;
import eu.sshopencloud.marketplace.services.skg.SkgPersonsService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("skgPersonsController")
@RequestMapping("/skg/persons")
@Tag(name = "SKG-IF")
public class PersonsController {

    private final PageCoordsValidator pageCoordsValidator;
    private final SkgPersonsService skgPersonsService;

    public PersonsController(SkgPersonsService skgPersonsService, @Qualifier("skgPageCoordsValidator") PageCoordsValidator pageCoordsValidator) {
        this.pageCoordsValidator = pageCoordsValidator;
        this.skgPersonsService = skgPersonsService;
    }


    @Operation(summary = "Get all persons in SKG format")
    @GetMapping(produces = "application/ld+json")
    public ResponseEntity<JsonLdDocument> getPersons(@RequestParam(value = "page", required = false) Integer page,
                                                      @RequestParam(value = "page_size", required = false) Integer pageSize) throws PageTooLargeException {
        PageCoords pageData = pageCoordsValidator.validate(page, pageSize);

        return ResponseEntity.ok(skgPersonsService.getPersons(pageData));
    }



}
