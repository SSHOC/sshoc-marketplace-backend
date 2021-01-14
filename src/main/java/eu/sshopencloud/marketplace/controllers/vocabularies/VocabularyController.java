package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyBasicDto;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PaginatedVocabularies;
import eu.sshopencloud.marketplace.services.vocabularies.exception.VocabularyAlreadyExistsException;
import eu.sshopencloud.marketplace.services.vocabularies.exception.VocabularyDoesNotExistException;
import eu.sshopencloud.marketplace.services.vocabularies.VocabularyService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/vocabularies")
@RequiredArgsConstructor
public class VocabularyController {

    private final PageCoordsValidator pageCoordsValidator;
    private final VocabularyService vocabularyService;


    @GetMapping
    public ResponseEntity<PaginatedVocabularies> getVocabularies(@RequestParam(value = "page", required = false) Integer page,
                                                                 @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(vocabularyService.getVocabularies(pageCoordsValidator.validate(page, perpage)));
    }

    @GetMapping("/{code}")
    public ResponseEntity<VocabularyDto> getVocabulary(@PathVariable("code") String code,
                                                       @RequestParam(value = "page", required = false) Integer page,
                                                       @RequestParam(value = "perpage", required = false) Integer perPage)
            throws PageTooLargeException {

        PageCoords pageCoords = pageCoordsValidator.validate(page, perPage);
        return ResponseEntity.ok(vocabularyService.getVocabulary(code, pageCoords));
    }

    @PostMapping
    public ResponseEntity<VocabularyBasicDto> createVocabulary(@RequestParam("ttl") MultipartFile vocabularyFile)
            throws IOException, VocabularyAlreadyExistsException {

        VocabularyBasicDto vocabulary = vocabularyService.createUploadedVocabulary(vocabularyFile);
        return ResponseEntity.ok(vocabulary);
    }

    @PutMapping("/{code}")
    public ResponseEntity<VocabularyBasicDto> updateVocabulary(
            @PathVariable("code") String vocabularyCode,
            @RequestParam("ttl") MultipartFile vocabularyFile,
            @RequestParam(value = "force", required = false, defaultValue = "false") boolean force)
            throws IOException, VocabularyDoesNotExistException {

        VocabularyBasicDto vocabulary = vocabularyService.updateUploadedVocabulary(vocabularyCode, vocabularyFile, force);
        return ResponseEntity.ok(vocabulary);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteVocabulary(@PathVariable("code") String vocabularyCode,
                                                 @RequestParam(value = "force", required = false, defaultValue = "false") boolean force)
            throws VocabularyDoesNotExistException {

        vocabularyService.removeVocabulary(vocabularyCode, force);
        return ResponseEntity.ok().build();
    }
}
