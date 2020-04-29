package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PaginatedVocabularies;
import eu.sshopencloud.marketplace.services.vocabularies.VocabularyService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VocabularyController {

    private final PageCoordsValidator pageCoordsValidator;

    private final VocabularyService vocabularyService;

    @GetMapping(path = "/vocabularies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedVocabularies> getVocabularies(@RequestParam(value = "page", required = false) Integer page,
                                                                 @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(vocabularyService.getVocabularies(pageCoordsValidator.validate(page, perpage)));
    }

    @GetMapping(path = "/vocabularies/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VocabularyDto> getVocabulary(@PathVariable("code") String code) {
        return ResponseEntity.ok(vocabularyService.getVocabulary(code));
    }

}
