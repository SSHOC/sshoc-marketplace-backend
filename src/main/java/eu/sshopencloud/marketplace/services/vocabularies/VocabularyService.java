package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;

    private final ConceptRepository conceptRepository;

    private final ConceptRelatedConceptService conceptRelatedConceptService;

    public PaginatedVocabularies getVocabularies(int page, int perpage) {
        Page<Vocabulary> vocabularies = vocabularyRepository.findAll(PageRequest.of(page - 1, perpage, new Sort(Sort.Direction.ASC, "label")));
        for (Vocabulary vocabulary: vocabularies) {
            List<Concept> concepts = new ArrayList<Concept>();
            for (Concept concept: conceptRepository.findConceptByVocabularyCode(vocabulary.getCode(), new Sort(Sort.Direction.ASC, "ord"))) {
                concept.setRelatedConcepts(conceptRelatedConceptService.getConceptRelatedConcepts(concept.getCode(), vocabulary.getCode()));
                concepts.add(concept);
            }
            vocabulary.setConcepts(concepts);
        }
        return new PaginatedVocabularies(vocabularies, page, perpage);
    }

    public Vocabulary getVocabulary(String code) {
        Vocabulary vocabulary = vocabularyRepository.getOne(code);
        List<Concept> concepts = new ArrayList<Concept>();
        for (Concept concept: conceptRepository.findConceptByVocabularyCode(vocabulary.getCode(), new Sort(Sort.Direction.ASC, "ord"))) {
            concept.setRelatedConcepts(conceptRelatedConceptService.getConceptRelatedConcepts(concept.getCode(), code));
            concepts.add(concept);
        }
        vocabulary.setConcepts(concepts);
        return vocabulary;
    }

    public void deleteVocabulary(String code) {
        // TODO validate uses properties - DO NOT DELETE in any of existing references
        Vocabulary vocabulary = vocabularyRepository.getOne(code);
        // TODO remove relations between related concepts
        vocabularyRepository.delete(vocabulary);
    }

}
