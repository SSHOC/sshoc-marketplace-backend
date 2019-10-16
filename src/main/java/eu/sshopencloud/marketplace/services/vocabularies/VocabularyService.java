package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;

    private final ItemRelatedItemService itemRelatedItemService;

    private final ConceptRelatedConceptService conceptRelatedConceptService;

    public List<Vocabulary> getAllVocabularies() {
        return vocabularyRepository.findAll(new Sort(Sort.Direction.ASC, "label"));
    }

    public Vocabulary getVocabulary(Long id) {
        Vocabulary vocabulary = vocabularyRepository.getOne(id);
        vocabulary.setRelatedItems(itemRelatedItemService.getItemRelatedItems(id));
        for (Concept concept: vocabulary.getConcepts()) {
            concept.setRelatedConcepts(conceptRelatedConceptService.getConceptRelatedConcepts(concept.getId()));
        }
        return vocabulary;
    }

}
