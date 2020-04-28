package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.ConceptDto;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ConceptController {

    private final ConceptService conceptService;

    @GetMapping(path = "/object-type-concepts/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ConceptDto>> getObjectTypeConcepts(@PathVariable("category") ItemCategory category) {
        return ResponseEntity.ok(conceptService.getObjectTypeConcepts(category));
    }

}
