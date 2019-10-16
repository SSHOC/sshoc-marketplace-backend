package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.services.vocabularies.VocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping("/vocabularies")
    public ResponseEntity<List<Vocabulary>> getAllVocabularies() {
        List<Vocabulary> vocabularies = vocabularyService.getAllVocabularies();
        return ResponseEntity.ok(vocabularies);
    }

    @GetMapping("/vocabularies/{id}")
    public ResponseEntity<Vocabulary> getVocabulary(@PathVariable("id") long id) {
        Vocabulary vocabulary = vocabularyService.getVocabulary(id);
        return ResponseEntity.ok(vocabulary);
    }

}
