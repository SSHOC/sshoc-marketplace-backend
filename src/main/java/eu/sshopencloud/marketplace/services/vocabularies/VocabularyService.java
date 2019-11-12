package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;

    private final ConceptRepository conceptRepository;

    private final ConceptRelatedConceptService conceptRelatedConceptService;

    public PaginatedVocabularies getVocabularies(int page, int perpage) {
        Page<Vocabulary> vocabularies = vocabularyRepository.findAll(PageRequest.of(page - 1, perpage, new Sort(Sort.Direction.ASC, "label")));
        for (Vocabulary vocabulary: vocabularies) {
            complete(vocabulary);
        }

        return PaginatedVocabularies.builder().vocabularies(vocabularies.getContent())
                .count(vocabularies.getContent().size()).hits(vocabularies.getTotalElements()).page(page).perpage(perpage).pages(vocabularies.getTotalPages())
                .build();
    }

    public Vocabulary getVocabulary(String code) {
        Optional<Vocabulary> vocabulary = vocabularyRepository.findById(code);
        if (!vocabulary.isPresent()) {
            throw new EntityNotFoundException("Unable to find " + Vocabulary.class.getName() + " with code " + code);
        }
        return complete(vocabulary.get());
    }

    private Vocabulary complete(Vocabulary vocabulary) {
        List<Concept> concepts = new ArrayList<Concept>();
        for (Concept concept: conceptRepository.findConceptByVocabularyCode(vocabulary.getCode(), new Sort(Sort.Direction.ASC, "ord"))) {
            Concept conceptWithoutVocabulary = ConceptConverter.convertWithoutVocabulary(concept);
            conceptWithoutVocabulary.setRelatedConcepts(conceptRelatedConceptService.getConceptRelatedConcepts(concept.getCode(), vocabulary.getCode()));
            concepts.add(conceptWithoutVocabulary);
        }
        vocabulary.setConcepts(concepts);
        return vocabulary;
    }

}
