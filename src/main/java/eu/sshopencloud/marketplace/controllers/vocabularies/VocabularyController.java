package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyBasicDto;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PaginatedVocabularies;
import eu.sshopencloud.marketplace.services.vocabularies.exception.VocabularyAlreadyExistsException;
import eu.sshopencloud.marketplace.services.vocabularies.VocabularyService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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


    @Operation(summary = "Get all vocabularies in pages")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedVocabularies> getVocabularies(@RequestParam(value = "page", required = false) Integer page,
                                                                 @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(vocabularyService.getVocabularies(pageCoordsValidator.validate(page, perpage)));
    }

    @Operation(summary = "Get vocabulary for given code")
    @GetMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VocabularyDto> getVocabulary(@PathVariable("code") String code,
                                                       @RequestParam(value = "page", required = false) Integer page,
                                                       @RequestParam(value = "perpage", required = false) Integer perPage)
            throws PageTooLargeException {

        PageCoords pageCoords = pageCoordsValidator.validate(page, perPage);
        return ResponseEntity.ok(vocabularyService.getVocabulary(code, pageCoords));
    }

    @Operation(summary = "Create vocabulary from file")
    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VocabularyBasicDto> createVocabulary(@RequestParam("ttl") MultipartFile vocabularyFile)
            throws IOException, VocabularyAlreadyExistsException {

        VocabularyBasicDto vocabulary = vocabularyService.createUploadedVocabulary(vocabularyFile);
        return ResponseEntity.ok(vocabulary);
    }

    @Operation(summary = "Update vocabulary for given code and file")
    @PutMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VocabularyBasicDto> updateVocabulary(
            @PathVariable("code") String vocabularyCode,
            @RequestParam("ttl") MultipartFile vocabularyFile,
            @RequestParam(value = "force", required = false, defaultValue = "false") boolean force)
            throws IOException {

        VocabularyBasicDto vocabulary = vocabularyService.updateUploadedVocabulary(vocabularyCode, vocabularyFile, force);
        return ResponseEntity.ok(vocabulary);
    }

    @Operation(summary = "Delete vocabulary for given code")
    @DeleteMapping(path = "/{code}")
    public ResponseEntity<Void> deleteVocabulary(@PathVariable("code") String vocabularyCode,
                                                 @RequestParam(value = "force", required = false, defaultValue = "false") boolean force) {

        vocabularyService.removeVocabulary(vocabularyCode, force);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Get vocabulary SKOS format with given filename")
    @GetMapping(path = "export/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> exportVocabularyFile(@PathVariable("code") String vocabularyCode)
            throws IOException {

        InputStreamResource resource = new InputStreamResource(vocabularyService.exportVocabulary(vocabularyCode));

        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + vocabularyCode + "_exported.ttl");
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");


        return ResponseEntity.ok()
                .headers(header)
                .contentType(MediaType.parseMediaType("application/txt"))
                .body(resource);

    }
}
