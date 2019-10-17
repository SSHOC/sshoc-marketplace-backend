package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;

    private final ItemService itemService;

    private final ItemRelatedItemService itemRelatedItemService;

    private final ConceptRelatedConceptService conceptRelatedConceptService;

    public List<Vocabulary> getAllVocabularies() {
        List<Vocabulary> vocabularies = vocabularyRepository.findAll(new Sort(Sort.Direction.ASC, "label"));
        for (Vocabulary vocabulary: vocabularies) {
            vocabulary.setRelatedItems(itemRelatedItemService.getItemRelatedItems(vocabulary.getId()));
            vocabulary.setOlderVersions(itemService.getOlderVersionsOfItem(vocabulary));
            vocabulary.setNewerVersions(itemService.getNewerVersionsOfItem(vocabulary));
            itemService.fillAllowedVocabulariesForPropertyTypes(vocabulary);
            for (Concept concept: vocabulary.getConcepts()) {
                concept.setRelatedConcepts(conceptRelatedConceptService.getConceptRelatedConcepts(concept.getId()));
            }
        }
        return vocabularies;
    }

    public Vocabulary getVocabulary(Long id) {
        Vocabulary vocabulary = vocabularyRepository.getOne(id);
        vocabulary.setRelatedItems(itemRelatedItemService.getItemRelatedItems(id));
        vocabulary.setOlderVersions(itemService.getOlderVersionsOfItem(vocabulary));
        vocabulary.setNewerVersions(itemService.getNewerVersionsOfItem(vocabulary));
        itemService.fillAllowedVocabulariesForPropertyTypes(vocabulary);
        for (Concept concept: vocabulary.getConcepts()) {
            concept.setRelatedConcepts(conceptRelatedConceptService.getConceptRelatedConcepts(concept.getId()));
        }
        return vocabulary;
    }

}
