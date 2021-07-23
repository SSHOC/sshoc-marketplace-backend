package eu.sshopencloud.marketplace.validators.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ConceptPropertyFactory {

    private final ConceptRepository conceptRepository;
    private final VocabularyFactory vocabularyFactory;


    public Concept create(ConceptId conceptId, PropertyType propertyType, List<Vocabulary> allowedVocabularies, Errors errors) {
        Concept concept;
        // either uri or code and vocabulary must be provided
        if (conceptId.getUri() == null) {
            concept = createByCodeAndVocabularyId(conceptId.getCode(), conceptId.getVocabulary(), errors);
        } else {
            concept = createByUri(conceptId.getUri(), errors);
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

    private Concept createByCodeAndVocabularyId(String code, VocabularyId vocabularyId, Errors errors) {
        Vocabulary vocabulary = null;
        if (StringUtils.isBlank(code)) {
            errors.rejectValue("code", "field.required", "Concept code is required.");
        }
        if (vocabularyId == null) {
            errors.rejectValue("vocabulary", "field.required", "Concept vocabulary is required.");
        } else {
            errors.pushNestedPath("vocabulary");
            vocabulary = vocabularyFactory.create(vocabularyId, errors);
            errors.popNestedPath();
        }
        if (!StringUtils.isBlank(code) && vocabulary != null) {
            Optional<Concept> conceptHolder = conceptRepository.findById(eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder()
                    .code(code).vocabulary(vocabulary.getCode()).build());

            if (conceptHolder.isEmpty()) {
                // assign error to code field because vocabulary was validated earlier
                errors.rejectValue("code", "field.notExist", "Concept does not exist.");
                return null;
            }

            return conceptHolder.get();
        }

        return null;
    }

    private Concept createByUri(String uri, Errors errors) {
        if (StringUtils.isBlank(uri)) {
            errors.rejectValue("uri", "field.required", "Concept uri is required.");
            return null;
        }
        Concept concept = conceptRepository.findByUri(uri);
        if (concept == null) {
            errors.rejectValue("uri", "field.notExist", "Concept does not exist.");
            return null;
        }

        return concept;
    }
}
