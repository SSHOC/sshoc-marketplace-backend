package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.domain.common.BaseOrderableEntityService;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.items.*;
import eu.sshopencloud.marketplace.mappers.items.ItemRelationMapper;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
import eu.sshopencloud.marketplace.repositories.items.ItemRelationRepository;
import eu.sshopencloud.marketplace.validators.items.ItemRelationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemRelationService extends BaseOrderableEntityService<ItemRelation, String> {

    private final ItemRelationRepository itemRelationRepository;

    private final ItemRelatedItemService itemRelatedItemService;

    private final ItemRelationFactory itemRelationFactory;

    public PaginatedItemRelation getItemRelations(PageCoords pageCoords) {

        Page<ItemRelation> itemRelationsPage = itemRelationRepository.findAll(PageRequest.of(
                pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("ord"))));

        List<ItemRelationDto> itemRelations = ItemRelationMapper.INSTANCE.toDto(itemRelationsPage.getContent());

        return PaginatedItemRelation.builder()
                .itemRelations(itemRelations)
                .page(pageCoords.getPage())
                .perpage(pageCoords.getPerpage())
                .pages(itemRelationsPage.getTotalPages())
                .hits(itemRelationsPage.getTotalElements())
                .count(itemRelationsPage.getNumberOfElements())
                .build();

    }

    public ItemRelationDto getItemRelation(String code) {
        ItemRelation itemRelation = itemRelationRepository.findById(code).orElseThrow(
                () -> new EntityNotFoundException(String.format("Item relation with code = '%s' not found", code))
        );
        return ItemRelationMapper.INSTANCE.toDto(itemRelation);
    }

    public ItemRelationDto createItemRelation(ItemRelationCore itemRelationCore) {
        ItemRelation itemRelation = itemRelationFactory.create(itemRelationCore, null);

        placeEntryAtPosition(itemRelation, itemRelationCore.getOrd(), true);

        itemRelationRepository.save(itemRelation);

        if (!Objects.isNull(itemRelation.getInverseOf())) {
            ItemRelation inverseItemRelation = itemRelationRepository.getItemRelationByCode(itemRelationCore.getInverseOf());
            inverseItemRelation.setInverseOf(itemRelation);
            itemRelationRepository.save(inverseItemRelation);
        }

        return ItemRelationMapper.INSTANCE.toDto(itemRelation);
    }

    public ItemRelationDto updateItemRelation(String code, ItemRelationCore itemRelationCore) {
        ItemRelation currentInverseItemRelation = itemRelationRepository.getItemRelationByCode(code).getInverseOf();

        ItemRelation itemRelation = itemRelationFactory.create(itemRelationCore, code);

        placeEntryAtPosition(itemRelation, itemRelationCore.getOrd(), false);

        itemRelationRepository.save(itemRelation);

        if (currentInverseItemRelation != null && (itemRelation.getInverseOf() == null || !currentInverseItemRelation.getCode().equals(itemRelation.getInverseOf().getCode()))) {
            currentInverseItemRelation.setInverseOf(null);
            itemRelationRepository.save(currentInverseItemRelation);
        }
        if (!Objects.isNull(itemRelation.getInverseOf())) {
            ItemRelation inverseItemRelation = itemRelationRepository.getItemRelationByCode(itemRelationCore.getInverseOf());
            inverseItemRelation.setInverseOf(itemRelation);
            itemRelationRepository.save(inverseItemRelation);
        }

        return ItemRelationMapper.INSTANCE.toDto(itemRelation);
    }

    public void deleteItemRelation(String code, boolean forceRemoval) {
        ItemRelation itemRelation = itemRelationRepository.findById(code).orElseThrow(
                () -> new EntityNotFoundException(String.format("Item relation with code = '%s' not found", code))
        );

        if (itemRelatedItemService.existByRelation(itemRelation)) {
            if (!forceRemoval) {
                throw new IllegalArgumentException(
                        String.format(
                                "Cannot remove item relation '%s' since there already exist items with this relation to other item. " +
                                        "Use force=true parameter to remove the item relation and the associated properties as well.",
                                code
                        )
                );
            } else {
                itemRelatedItemService.removeItemRelatedItemByRelation(itemRelation);
            }
        }

        if (!Objects.isNull(itemRelation.getInverseOf())) {
            ItemRelation inverseItemRelation = itemRelationRepository.getItemRelationByCode(itemRelation.getInverseOf().getCode());
            inverseItemRelation.setInverseOf(null);
            itemRelationRepository.save(inverseItemRelation);
        }

        itemRelationRepository.delete(itemRelation);
        removeEntryFromPosition(itemRelation.getCode());
    }

    @Override
    protected JpaRepository getEntityRepository() {
        return itemRelationRepository;
    }
}
