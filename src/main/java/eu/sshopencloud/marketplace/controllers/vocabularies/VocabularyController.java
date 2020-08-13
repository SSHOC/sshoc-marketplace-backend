package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PaginatedVocabularies;
import eu.sshopencloud.marketplace.services.vocabularies.VocabularyService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
