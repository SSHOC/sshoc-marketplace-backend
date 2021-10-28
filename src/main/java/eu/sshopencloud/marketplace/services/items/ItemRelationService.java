package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.domain.common.BaseOrderableEntityService;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.items.*;
import eu.sshopencloud.marketplace.mappers.items.ItemRelationMapper;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
import eu.sshopencloud.marketplace.repositories.items.ItemRelationRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.items.ItemRelationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

import javax.persistence.EntityNotFoundException;
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
        ItemRelation itemRelation = itemRelationRepository.getItemRelationByCode(code);
        return ItemRelationMapper.INSTANCE.toDto(itemRelation);
    }

    //done
    public ItemRelationDto createItemRelation(ItemRelationCore itemRelationCore) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(itemRelationCore, "ItemRelation");

        ItemRelation itemRelation = itemRelationFactory.create(itemRelationCore, new ItemRelation(), errors);

        placeEntryAtPosition(itemRelation, itemRelationCore.getOrd(), true);

        itemRelationRepository.save(itemRelation);

        if (!Objects.isNull(itemRelation.getInverseOf())) {
            ItemRelation inverseOfItemRelation = itemRelationRepository.getItemRelationByCode(itemRelationCore.getInverseOf());
            inverseOfItemRelation.setInverseOf(itemRelation);
            itemRelationRepository.save(inverseOfItemRelation);
        }

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return ItemRelationMapper.INSTANCE.toDto(itemRelation);
    }

    //Eliza
    public ItemRelationDto updateItemRelation(String relationCode, ItemRelationCore itemRelationCore) {
        ItemRelation itemRelation = itemRelationRepository.getItemRelationByCode(relationCode);

        itemRelation.setLabel(itemRelationCore.getLabel());
        Integer ord = itemRelationCore.getOrd();

        if (ord != null) {
            validateItemRelationPosition(itemRelationCore.getOrd());
            itemRelation.setOrd(itemRelationCore.getOrd());
            placeEntryAtPosition(itemRelation, itemRelation.getOrd(), false);
            // reorderItemRelations(itemRelationCore.getCode(), itemRelationCore.getOrd());
        }

        if (!Objects.isNull(itemRelationCore.getInverseOf()) && !itemRelationCore.getInverseOf().isEmpty()) {
            ItemRelation inverseItemRelation = itemRelationRepository.getItemRelationByCode(itemRelationCore.getInverseOf());
            if (!Objects.isNull(inverseItemRelation)) {
                itemRelation.setInverseOf(inverseItemRelation);
                inverseItemRelation.setInverseOf(itemRelation);
                itemRelationRepository.save(inverseItemRelation);
            }
        }

        itemRelationRepository.save(itemRelation);
        return ItemRelationMapper.INSTANCE.toDto(itemRelation);
    }

    public void deleteItemRelation(String relationCode, boolean forceRemoval) {
        ItemRelation itemRelation = itemRelationRepository.getItemRelationByCode(relationCode);
        if (Objects.isNull(itemRelation))
            throw new EntityNotFoundException(String.format("Item relation with code = '%s' not found", relationCode));

        if (itemRelatedItemService.existByRelation(itemRelation)) {
            if (!forceRemoval) {
                throw new IllegalArgumentException(
                        String.format(
                                "Cannot remove item relation '%s' since there already exist items with this relation to other item. " +
                                        "Use force=true parameter to remove the item relation and the associated properties as well.",
                                relationCode
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


        itemRelationRepository.deleteItemRelations(itemRelation.getCode());
        removeEntryFromPosition(itemRelation.getCode());
    }

    private void validateItemRelationPosition(Integer ord) {
        if (ord == null)
            return;

        long itemRelationsCount = itemRelationRepository.count();
        if (ord < 1 || ord > itemRelationsCount + 1) {
            throw new IllegalArgumentException(
                    String.format("Invalid position index: %d (maximum possible: %d)", ord, itemRelationsCount + 1)
            );
        }
    }

    @Override
    protected JpaRepository getEntityRepository() {
        return itemRelationRepository;
    }
}
