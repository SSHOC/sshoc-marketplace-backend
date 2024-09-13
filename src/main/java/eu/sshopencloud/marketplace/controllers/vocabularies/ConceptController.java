package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.*;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptService;
import eu.sshopencloud.marketplace.services.vocabularies.exception.ConceptAlreadyExistsException;
import eu.sshopencloud.marketplace.services.vocabularies.exception.VocabularyIsClosedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/vocabularies")
@RequiredArgsConstructor
public class ConceptController {

    private final ConceptService conceptService;


    @Operation(summary = "Get concept for given vocabulary code and concept code")
    @GetMapping(path = "/{vocabulary-code}/concepts/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConceptDto> getConcept(@PathVariable("vocabulary-code") String vocabularyCode,
                                                 @PathVariable("code") String code) {
        return ResponseEntity.ok(conceptService.getConcept(code, vocabularyCode));
    }

    @Operation(summary = "Create concept for given vocabulary code")
    @PostMapping(path = "/{vocabulary-code}/concepts", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConceptDto> createConcept(@Parameter(
            description = "Created concept",
            required = true,
            schema = @Schema(implementation = ConceptCore.class)) @Valid @RequestBody ConceptCore newConcept,
                                                    @PathVariable("vocabulary-code") String vocabularyCode,
                                                    @RequestParam(value = "candidate", defaultValue = "true") boolean candidate)
            throws ConceptAlreadyExistsException, VocabularyIsClosedException {

        return ResponseEntity.ok(conceptService.createConcept(newConcept, vocabularyCode, candidate));
    }

    @Operation(summary = "Update concept for given vocabulary code and concept code")
    @PutMapping(path = "/{vocabulary-code}/concepts/{code}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConceptDto> updateConcept(@Parameter(
            description = "Updated concept",
            required = true,
            schema = @Schema(implementation = ConceptCore.class)) @Valid @RequestBody ConceptCore updatedConcept,
                                                    @PathVariable("vocabulary-code") String vocabularyCode,
                                                    @PathVariable("code") String code) {

        return ResponseEntity.ok(conceptService.updateConcept(code, updatedConcept, vocabularyCode));
    }

    @Operation(summary = "Commit concept for given vocabulary code and concept code")
    @PutMapping(path = "/{vocabulary-code}/concepts/{code}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConceptDto> commitConcept(@PathVariable("vocabulary-code") String vocabularyCode,
                                                    @PathVariable("code") String code) {

        return ResponseEntity.ok(conceptService.commitConcept(code, vocabularyCode));
    }

    @Operation(summary = "Delete concept for given vocabulary code and concept code")
    @DeleteMapping("/{vocabulary-code}/concepts/{code}")
    public ResponseEntity<Void> deleteConcept(@PathVariable("vocabulary-code") String vocabularyCode,
                                              @PathVariable("code") String code,
                                              @RequestParam(value = "force", required = false, defaultValue = "false") boolean force) {

        conceptService.removeConcept(code, vocabularyCode, force);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Merge concepts")
    @PostMapping(path= "/{vocabulary-code}/concepts/{code}/merge")
    public ResponseEntity<ConceptDto> mergeConcepts(@PathVariable("vocabulary-code") String vocabularyCode, @PathVariable("code") String code,
            @RequestParam("with") List<String> with)
            throws VocabularyIsClosedException {

        return ResponseEntity.ok(conceptService.mergeConcepts(code,vocabularyCode,with));
    }


}
