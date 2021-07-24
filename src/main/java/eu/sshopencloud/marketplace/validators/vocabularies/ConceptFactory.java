package eu.sshopencloud.marketplace.validators.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.ConceptCore;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import eu.sshopencloud.marketplace.services.vocabularies.VocabularyService;
import eu.sshopencloud.marketplace.validators.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;


@Service
@Transactional
@RequiredArgsConstructor
public class ConceptFactory {

    private final ConceptRepository conceptRepository;

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

    public Concept create(ConceptCore conceptCore, Vocabulary vocabulary, boolean candidate, String code) throws ValidationException {
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

        if (StringUtils.isBlank(conceptCore.getUri())) {
            concept.setUri(vocabulary.getNamespace() + concept.getCode());
        } else {
            if (conceptCore.getUri().equals(vocabulary.getNamespace() + concept.getCode())) {
                concept.setUri(conceptCore.getUri());
            } else {
                errors.rejectValue("uri", "field.invalid", "Uri is not consistent with vocabulary namespace and concept code.");
            }
        }

        concept.setCandidate(candidate);

        concept.setOrd(countConceptsInVocabulary(vocabulary) + 1);

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return concept;
    }

    private int countConceptsInVocabulary(Vocabulary vocabulary) {
        ExampleMatcher queryConceptMatcher = ExampleMatcher.matching()
                .withMatcher("vocabulary", ExampleMatcher.GenericPropertyMatchers.exact());
        Concept queryConcept = new Concept();
        queryConcept.setVocabulary(vocabulary);
        return (int) conceptRepository.count(Example.of(queryConcept, queryConceptMatcher));
    }


    private Concept getOrCreateConcept(String code, Vocabulary vocabulary) {
        if (code != null) {
            return conceptRepository.getOne(ConceptId.builder().code(code).vocabulary(vocabulary.getCode()).build());
        }

        return new Concept();
    }

}
