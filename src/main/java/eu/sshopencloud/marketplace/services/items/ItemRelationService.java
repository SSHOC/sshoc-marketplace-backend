package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
import eu.sshopencloud.marketplace.repositories.items.ItemRelationRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemRelationService {

    private final ItemRelationRepository itemRelationRepository;

    public List<ItemRelation> getAllItemRelations() {
        return itemRelationRepository.findAll(new Sort(Sort.Direction.ASC, "ord"));
    }

    public ItemRelation validate(String prefix, ItemRelationId itemRelation) throws DataViolationException {
        if (itemRelation.getCode() == null) {
            throw new DataViolationException(prefix + "code", itemRelation.getCode());
        }
        Optional<ItemRelation> result = itemRelationRepository.findById(itemRelation.getCode());
        if (!result.isPresent()) {
            throw new DataViolationException(prefix + "code", itemRelation.getCode());
        }
        return result.get();
    }

}
