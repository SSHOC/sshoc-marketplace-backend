package eu.sshopencloud.marketplace.validators.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelationRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptService;
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
public class ConceptValidator {

    private final ConceptRepository conceptRepository;

    private final ConceptRelationRepository conceptRelationRepository;

    private final ConceptService conceptService;

    private final VocabularyValidator vocabularyValidator;


    public Concept validate(ItemCategory category, ConceptId conceptId, PropertyType propertyType, List<Vocabulary> allowedVocabularies, Errors errors) {
        Concept concept;
        // either uri or code and vocabulary must be provided
        if (conceptId.getUri() == null) {
            concept = validateByCodeAndVocabularyId(conceptId.getCode(), conceptId.getVocabulary(), errors);
        } else {
            concept = validateByUri(conceptId.getUri(), errors);
        }
        if (concept == null) {
            return null;
        }
        String vocabularyCode = concept.getVocabulary().getCode();
        if (!allowedVocabularies.stream().anyMatch(v -> Objects.equals(v.getCode(), vocabularyCode))) {
            errors.rejectValue("vocabulary", "field.disallowedVocabulary", new String[]{vocabularyCode, propertyType.getCode()},
                    "Disallowed vocabulary '" + vocabularyCode + "' for property type '" + propertyType.getCode() + "'.");
            return null;
        }

        /* Deprecated - object type property is no longer present
        if (category != null) {
            if (!checkObjectType(category, propertyType, concept, conceptId.getUri(), errors)) {
                return null;
            }
        }
         */

        return concept;
    }

    private Concept validateByCodeAndVocabularyId(String code, VocabularyId vocabularyId, Errors errors) {
        Vocabulary vocabulary = null;
        if (StringUtils.isBlank(code)) {
            errors.rejectValue("code", "field.required", "Concept code is required.");
        }
        if (vocabularyId == null) {
            errors.rejectValue("vocabulary", "field.required", "Concept vocabulary is required.");
        } else {
            errors.pushNestedPath("vocabulary");
            vocabulary = vocabularyValidator.validate(vocabularyId, errors);
            errors.popNestedPath();
        }
        if (!StringUtils.isBlank(code) && vocabulary != null) {
            Optional<Concept> conceptHolder = conceptRepository.findById(eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder()
                    .code(code).vocabulary(vocabulary.getCode()).build());
            if (!conceptHolder.isPresent()) {
                // assign error to code field because vocabulary was validated earlier
                errors.rejectValue("code", "field.notExist", "Concept does not exist.");
                return null;
            } else {
                return conceptHolder.get();
            }
        } else {
            return null;
        }
    }

    private Concept validateByUri(String uri, Errors errors) {
        if (StringUtils.isBlank(uri)) {
            errors.rejectValue("uri", "field.required", "Concept uri is required.");
            return null;
        }
        Concept concept = conceptRepository.findByUri(uri);
        if (concept == null) {
            errors.rejectValue("uri", "field.notExist", "Concept does not exist.");
            return null;
        } else {
            return concept;
        }
    }

    @Deprecated
    private boolean checkObjectType(ItemCategory category, PropertyType propertyType, Concept concept, String uri, Errors errors) {
        if (propertyType.getCode().equals(ItemCategory.OBJECT_TYPE_PROPERTY_TYPE_CODE)) {
            if (!concept.getCode().equals(category.getValue())) {
                List<Concept> broaderConcepts = conceptService.getRelatedConceptsOfConcept(concept, conceptRelationRepository.getOne("broader"));
                for (Concept broaderConcept : broaderConcepts) {
                    if (broaderConcept.getCode().equals(category.getValue())) {
                        return true;
                    }
                }
                String field = (uri == null) ? "code" : "uri";
                errors.rejectValue(field, "field.incorrectObjectType", new String[]{concept.getCode(), category.getValue()},
                            "The object type '" + concept.getCode() + "' is outside the set of values for category '" + category.getValue() + "'.");
                return false;
            }
        }
        return true;
    }

}
