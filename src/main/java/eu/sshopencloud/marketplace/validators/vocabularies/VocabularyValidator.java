package eu.sshopencloud.marketplace.validators.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class VocabularyValidator {

    private final VocabularyRepository vocabularyRepository;


    public Vocabulary validate(VocabularyId vocabularyId, Errors errors) {
        if (StringUtils.isBlank(vocabularyId.getCode())) {
            errors.rejectValue("code", "field.required", "Vocabulary code is required.");
            return null;
        }
        Optional<Vocabulary> vocabularyHolder = vocabularyRepository.findById(vocabularyId.getCode());
        if (!vocabularyHolder.isPresent()) {
            errors.rejectValue("code", "field.notExist", "Vocabulary does not exist.");
            return null;
        } else {
            return vocabularyHolder.get();
        }
    }

}
