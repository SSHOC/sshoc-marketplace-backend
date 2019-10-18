package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;

    private final ItemService itemService;

    private final ItemRelatedItemService itemRelatedItemService;

    private final ConceptRelatedConceptService conceptRelatedConceptService;

    public PaginatedVocabularies getVocabularies(int page, int perpage) {
        Page<Vocabulary> vocabularies = vocabularyRepository.findAll(PageRequest.of(page - 1, perpage, new Sort(Sort.Direction.ASC, "label")));
        for (Vocabulary vocabulary: vocabularies) {
            vocabulary.setRelatedItems(itemRelatedItemService.getItemRelatedItems(vocabulary.getId()));
            vocabulary.setOlderVersions(itemService.getOlderVersionsOfItem(vocabulary));
            vocabulary.setNewerVersions(itemService.getNewerVersionsOfItem(vocabulary));
            itemService.fillAllowedVocabulariesForPropertyTypes(vocabulary);
            for (Concept concept: vocabulary.getConcepts()) {
                concept.setRelatedConcepts(conceptRelatedConceptService.getConceptRelatedConcepts(concept.getId()));
            }
        }
        return new PaginatedVocabularies(vocabularies, page, perpage);
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

    public void deleteVocabulary(Long id) {
        // TODO validate uses properties - DO NOT DELETE in any of existing references
        Vocabulary vocabulary = vocabularyRepository.getOne(id);
        // TODO remove relations between related concepts
        itemRelatedItemService.deleteRelationsForItem(vocabulary);
        itemService.switchVersionForDelete(vocabulary);
        vocabularyRepository.delete(vocabulary);
    }

}
