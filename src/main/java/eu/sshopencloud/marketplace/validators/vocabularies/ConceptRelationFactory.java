package eu.sshopencloud.marketplace.validators.vocabularies;

import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptRelationId;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelation;
import eu.sshopencloud.marketplace.repositories.actors.ActorRoleRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ConceptRelationFactory {

    private final ConceptRelationRepository conceptRelationRepository;

    public ConceptRelation create(ConceptRelationId conceptRelationId, Errors errors) {
        if (StringUtils.isBlank(conceptRelationId.getCode())) {
            errors.rejectValue("code", "field.required", "Concept relation code is required.");
            return null;
        }
        Optional<ConceptRelation> relationHolder = conceptRelationRepository.findById(conceptRelationId.getCode());
        if (relationHolder.isEmpty()) {
            errors.rejectValue("code", "field.notExist", "Concept relation code does not exist.");
            return null;
        }

        return relationHolder.get();
    }

}
