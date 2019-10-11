package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.ItemRelation;
import eu.sshopencloud.marketplace.repositories.items.ItemRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemRelationService {

    private final ItemRelationRepository itemRelationRepository;

    public List<ItemRelation> getAllItemRelations() {
        return itemRelationRepository.findAll(new Sort(Sort.Direction.ASC, "ord"));
    }

}
