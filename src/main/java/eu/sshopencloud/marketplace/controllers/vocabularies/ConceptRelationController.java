package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.ConceptRelationDto;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ConceptRelationController {

    private final ConceptRelationService conceptRelationService;

    @GetMapping(path = "/concept-relations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ConceptRelationDto>> getAllConceptRelations() {
        return ResponseEntity.ok(conceptRelationService.getAllConceptRelations());
    }

}
