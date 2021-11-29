package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.ConceptRelationDto;
import eu.sshopencloud.marketplace.mappers.vocabularies.ConceptRelationMapper;
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

    public List<ConceptRelationDto> getAllConceptRelations() {
        return ConceptRelationMapper.INSTANCE.toDto(conceptRelationRepository.findAll(Sort.by(Sort.Order.asc("ord"))));
    }

    public List<ConceptRelation> getConceptRelations() {
        return conceptRelationRepository.findAll(Sort.by(Sort.Order.asc("ord")));
    }

}
