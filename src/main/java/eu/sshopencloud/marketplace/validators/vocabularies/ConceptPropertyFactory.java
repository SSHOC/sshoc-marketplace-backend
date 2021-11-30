package eu.sshopencloud.marketplace.validators.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ConceptPropertyFactory {

    private final ConceptFactory conceptFactory;

    public Concept create(ConceptId conceptId, PropertyType propertyType, List<Vocabulary> allowedVocabularies, Errors errors) {
        Concept concept;
        // either uri or code and vocabulary must be provided
        if (conceptId.getUri() == null) {
            concept = conceptFactory.createByCodeAndVocabularyId(conceptId.getCode(), conceptId.getVocabulary(), errors);
        } else {
            concept = conceptFactory.createByUri(conceptId.getUri(), errors);
        }
        if (concept == null) {
            return null;
        }
        String vocabularyCode = concept.getVocabulary().getCode();
        if (allowedVocabularies.stream().noneMatch(v -> Objects.equals(v.getCode(), vocabularyCode))) {
            errors.rejectValue("vocabulary", "field.disallowedVocabulary", new String[]{vocabularyCode, propertyType.getCode()},
                    "Disallowed vocabulary '" + vocabularyCode + "' for property type '" + propertyType.getCode() + "'.");
            return null;
        }

        return concept;
    }

}
