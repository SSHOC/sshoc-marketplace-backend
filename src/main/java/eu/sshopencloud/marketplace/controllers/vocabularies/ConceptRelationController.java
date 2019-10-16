package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelation;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptRelationService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/concept-relations")
    public ResponseEntity<List<ConceptRelation>> getAllConceptRelations() {
        List<ConceptRelation> conceptRelations = conceptRelationService.getAllConceptRelations();
        return ResponseEntity.ok(conceptRelations);
    }

}
