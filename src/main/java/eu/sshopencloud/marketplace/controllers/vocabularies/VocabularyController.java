package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.services.vocabularies.PaginatedVocabularies;
import eu.sshopencloud.marketplace.services.vocabularies.VocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VocabularyController {

    @Value("${marketplace.pagination.default-perpage}")
    private Integer defualtPerpage;

    @Value("${marketplace.pagination.maximal-perpage}")
    private Integer maximalPerpage;

    private final VocabularyService vocabularyService;

    @GetMapping(path = "/vocabularies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedVocabularies> getVocabularies(@RequestParam(value = "page", required = false) Integer page,
                                                                 @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        perpage = perpage == null ? defualtPerpage : perpage;
        if (perpage > maximalPerpage) {
            throw new PageTooLargeException(maximalPerpage);
        }
        page = page == null ? 1 : page;

        PaginatedVocabularies vocabularies = vocabularyService.getVocabularies(page, perpage);
        return ResponseEntity.ok(vocabularies);
    }

    @GetMapping(path = "/vocabularies/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Vocabulary> getVocabulary(@PathVariable("code") String code) {
        Vocabulary vocabulary = vocabularyService.getVocabulary(code);
        return ResponseEntity.ok(vocabulary);
    }

}
