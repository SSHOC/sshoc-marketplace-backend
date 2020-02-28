package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelation;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelationRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ConceptRelationService {

    private final ConceptRelationRepository conceptRelationRepository;

    public List<ConceptRelation> getAllConceptRelations() {
        return conceptRelationRepository.findAll(Sort.by(Sort.Order.asc("ord")));
    }

    public ConceptRelation validate(String prefix, String relationCode) throws DataViolationException {
        if (relationCode == null) {
            throw new DataViolationException(prefix + "code", relationCode);
        }
        Optional<ConceptRelation> result = conceptRelationRepository.findById(relationCode);
        if (!result.isPresent()) {
            throw new DataViolationException(prefix + "code", relationCode);
        }
        return result.get();
    }

}
