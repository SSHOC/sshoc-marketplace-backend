package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelationDto;
import eu.sshopencloud.marketplace.mappers.items.ItemRelationMapper;
import eu.sshopencloud.marketplace.repositories.items.ItemRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemRelationService {

    private final ItemRelationRepository itemRelationRepository;

    public List<ItemRelationDto> getAllItemRelations() {
        return ItemRelationMapper.INSTANCE.toDto(itemRelationRepository.findAll(Sort.by(Sort.Order.asc("ord"))));
    }

}
