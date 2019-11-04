package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.VocabularyInline;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConceptService {

    private final ConceptRepository conceptRepository;

    public Concept validate(String prefix, ConceptId concept, PropertyType propertyType, List<VocabularyInline> allowedVocabularies) throws DataViolationException, ConceptDisallowedException {
        if (concept.getCode() == null) {
            throw new DataViolationException(prefix + "code", concept.getCode());
        }
        if (concept.getVocabulary() == null) {
            throw new DataViolationException(prefix + "vocabulary", "null");
        }
        if (concept.getVocabulary().getCode() == null) {
            throw new DataViolationException(prefix + "vocabulary.code", concept.getVocabulary().getCode());
        }
        if (!allowedVocabularies.stream().map(v -> v.getCode()).collect(Collectors.toList()).contains(concept.getVocabulary().getCode())) {
            throw new ConceptDisallowedException(propertyType, concept.getVocabulary().getCode());
        }
        Optional<Concept> result = conceptRepository.findById(eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(concept.getCode()).vocabulary(concept.getVocabulary().getCode()).build());
        if (!result.isPresent()) {
            throw new DataViolationException(prefix + "(code, vocabulary.code)", "(" + concept.getCode() + ", " + concept.getVocabulary().getCode() + ")");
        }
        return result.get();
    }

}
