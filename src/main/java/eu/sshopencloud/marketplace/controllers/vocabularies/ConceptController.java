package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import eu.sshopencloud.marketplace.dto.vocabularies.*;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptService;
import eu.sshopencloud.marketplace.services.vocabularies.exception.ConceptAlreadyExistsException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/vocabularies")
@RequiredArgsConstructor
public class ConceptController {

    private final ConceptService conceptService;

    @GetMapping(path = "/{vocabulary-code}/concepts/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConceptDto> getConcept(@PathVariable("vocabulary-code") String vocabularyCode,
                                                 @PathVariable("code") String code) {
        return ResponseEntity.ok(conceptService.getConcept(code, vocabularyCode));
    }

    @PostMapping(path = "/{vocabulary-code}/concepts", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConceptDto> createConcept(@Parameter(
            description = "Created concept",
            required = true,
            schema = @Schema(implementation = ConceptCore.class)) @RequestBody ConceptCore newConcept,
                                                    @PathVariable("vocabulary-code") String vocabularyCode,
                                                    @RequestParam(value = "candidate", defaultValue = "true") boolean candidate) throws ConceptAlreadyExistsException {

        return ResponseEntity.ok(conceptService.createConcept(newConcept, vocabularyCode, candidate));
    }

    @PutMapping(path = "/{vocabulary-code}/concepts/{code}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConceptDto> updateConcept(@Parameter(
            description = "Updated concept",
            required = true,
            schema = @Schema(implementation = ConceptCore.class)) @RequestBody ConceptCore updatedConcept,
                                                    @PathVariable("vocabulary-code") String vocabularyCode,
                                                    @PathVariable("code") String code) {

        return ResponseEntity.ok(conceptService.updateConcept(code, updatedConcept, vocabularyCode));
    }

    @PutMapping(path = "/{vocabulary-code}/concepts/{code}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConceptDto> commitConcept(@PathVariable("vocabulary-code") String vocabularyCode,
                                                    @PathVariable("code") String code) {

        return ResponseEntity.ok(conceptService.commitConcept(code, vocabularyCode));
    }

    @DeleteMapping("/{vocabulary-code}/concepts/{code}")
    public ResponseEntity<Void> deleteConcept(@PathVariable("vocabulary-code") String vocabularyCode,
                                              @PathVariable("code") String code,
                                              @RequestParam(value = "force", required = false, defaultValue = "false") boolean force) {

        conceptService.removeConcept(code, vocabularyCode, force);
        return ResponseEntity.ok().build();
    }

}
