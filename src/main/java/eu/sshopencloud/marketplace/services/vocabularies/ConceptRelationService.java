package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelation;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ConceptRelationService {

    private final ConceptRelationRepository conceptRelationRepository;

    public List<ConceptRelation> getAllConceptRelations() {
        return conceptRelationRepository.findAll(Sort.by(Sort.Order.asc("ord")));
    }

}
