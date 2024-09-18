package eu.sshopencloud.marketplace.validators.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.ConceptCore;
import eu.sshopencloud.marketplace.dto.vocabularies.RelatedConceptCore;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.util.UriUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ConceptFactory {

    private final ConceptRepository conceptRepository;
    private final ConceptRelationFactory conceptRelationFactory;
    private final VocabularyFactory vocabularyFactory;


    public String resolveCode(ConceptCore conceptCore, Vocabulary vocabulary) {
        if (StringUtils.isNotBlank(conceptCore.getCode())) {
            return conceptCore.getCode();
        } else if (StringUtils.isNotBlank(conceptCore.getUri())) {
            if (conceptCore.getUri().startsWith(vocabulary.getNamespace())) {
                return conceptCore.getUri().substring(vocabulary.getNamespace().length());
            }
        }
        return null;
    }

    public Concept create(ConceptCore conceptCore, Vocabulary vocabulary, String code) throws ValidationException {
        Concept concept = getOrCreateConcept(code, vocabulary);

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(conceptCore, "Concept");

        concept.setCode(conceptCore.getCode());
        concept.setVocabulary(vocabulary);

        if (conceptCore.getLabel() == null) {
            concept.setLabel("");
        } else {
            concept.setLabel(conceptCore.getLabel());
        }

        if (conceptCore.getNotation() == null) {
            concept.setNotation("");
        } else {
            concept.setNotation(conceptCore.getNotation());
        }

        concept.setDefinition(conceptCore.getDefinition());

        if (StringUtils.isNotBlank(conceptCore.getUri()) &&
                !StringUtils.equals(conceptCore.getUri(), vocabulary.getNamespace() + concept.getCode()) &&
                !StringUtils.equals(conceptCore.getUri(), vocabulary.getNamespace() + URLEncoder.encode(concept.getCode(), StandardCharsets.UTF_8))) {
            errors.rejectValue("uri", "field.invalid",
                    "Uri is not consistent with vocabulary namespace and concept code.");
        } else {
            concept.setUri(vocabulary.getNamespace() + (isURLEncoded(concept.getCode()) ? concept.getCode() : UriUtils.encode(concept.getCode(), StandardCharsets.UTF_8)));
        }

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return concept;
    }

    private boolean isURLEncoded(String code) {
        try {
            String decoded = UriUtils.decode(code, StandardCharsets.UTF_8);
            String reEncoded = UriUtils.encode(decoded, StandardCharsets.UTF_8);
            if (code.equals(reEncoded)) {
                return true;
            }
        } catch (IllegalArgumentException e) {
            log.debug(e.getMessage());
            return false;
        }
        return false;
    }

    public List<ConceptRelatedConcept> createConceptRelations(Concept concept, List<RelatedConceptCore> relatedConcepts) throws ValidationException {

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(relatedConcepts, "Concept");

        List<ConceptRelatedConcept> conceptRelatedConcepts = new ArrayList<>();
        if (relatedConcepts != null) {
            for (int i = 0; i < relatedConcepts.size(); i++) {
                errors.pushNestedPath("relatedConcept[" + i + "]");
                RelatedConceptCore relatedConcept = relatedConcepts.get(i);
                ConceptRelatedConcept conceptRelatedConcept = new ConceptRelatedConcept();
                conceptRelatedConcept.setSubject(concept);

                Concept object;
                // either uri or code and vocabulary must be provided
                if (relatedConcept.getUri() == null) {
                    object = createByCodeAndVocabularyId(relatedConcept.getCode(), relatedConcept.getVocabulary(), errors);
                } else {
                    object = createByUri(relatedConcept.getUri(), errors);
                }
                conceptRelatedConcept.setObject(object);

                errors.pushNestedPath("relation");
                conceptRelatedConcept.setRelation(conceptRelationFactory.create(relatedConcept.getRelation(), errors));
                errors.popNestedPath();

                conceptRelatedConcepts.add(conceptRelatedConcept);
                errors.popNestedPath();
            }
        }

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return conceptRelatedConcepts;
    }

    public Concept createByCodeAndVocabularyId(String code, VocabularyId vocabularyId, Errors errors) {
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

    public Concept createByUri(String uri, Errors errors) {
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

    private Concept getOrCreateConcept(String code, Vocabulary vocabulary) {
        if (code != null) {
            return conceptRepository.getOne(ConceptId.builder().code(code).vocabulary(vocabulary.getCode()).build());
        }

        return new Concept();
    }

}
