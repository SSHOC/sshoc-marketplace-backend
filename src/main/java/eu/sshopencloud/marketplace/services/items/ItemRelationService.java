package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.items.*;
import eu.sshopencloud.marketplace.mappers.items.ItemRelationMapper;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
import eu.sshopencloud.marketplace.repositories.items.ItemRelationRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemRelationService {

    private final ItemRelationRepository itemRelationRepository;

    private final ItemRelatedItemService itemRelatedItemService;

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

    public ItemRelationDto createItemRelation(ItemRelationCore itemRelationCore) {

        Integer ord = itemRelationCore.getOrd();
        validateItemRelationPosition(ord);

        if (ord == null)
            itemRelationCore.setOrd(getMaxOrdForItemRelation());


        return ItemRelationMapper.INSTANCE.toDto(create(itemRelationCore));
    }

    public ItemRelationDto updateItemRelation(String relationCode, ItemRelationCore itemRelationCore) {
        ItemRelation itemRelation = itemRelationRepository.getItemRelationByCode(relationCode);

        itemRelation.setLabel(itemRelationCore.getLabel());
        Integer ord = itemRelationCore.getOrd();

        if (ord != null) {
            validateItemRelationPosition(itemRelationCore.getOrd());
            itemRelation.setOrd(itemRelationCore.getOrd());
            reorderItemRelations(itemRelationCore.getCode(), itemRelationCore.getOrd());
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


        if(!Objects.isNull(itemRelation.getInverseOf())){
            ItemRelation inverseOfItemRelation = itemRelationRepository.getItemRelationByInverseOfCode(itemRelation.getCode());
            inverseOfItemRelation.setInverseOf(null);
            if(forceRemoval) {
                itemRelation.setInverseOf(null);
                itemRelationRepository.delete(inverseOfItemRelation);
                if (itemRelatedItemService.existByRelation(inverseOfItemRelation))
                    itemRelatedItemService.removeItemRelatedItemByRelation(inverseOfItemRelation);
            }else itemRelationRepository.save(inverseOfItemRelation);
        }

        itemRelationRepository.delete(itemRelation);

        reorderItemRelations(relationCode, null);
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

    private void reorderItemRelations(String itemRelationCode, Integer itemRelationOrd) {
        List<ItemRelation> itemRelations = loadItemRelation();
        int ord = 1;

        for (ItemRelation itemRelation : itemRelations) {
            if (itemRelation.getCode().equals(itemRelationCode))
                continue;

            if (itemRelationOrd != null && ord == itemRelationOrd)
                ord++;

            if (itemRelation.getOrd() != ord)
                itemRelation.setOrd(ord);

            ord++;
        }
    }

    private List<ItemRelation> loadItemRelation() {
        Sort order = Sort.by(Sort.Order.asc("ord"));
        return itemRelationRepository.findAll(order);
    }

    private int getMaxOrdForItemRelation() {
        long itemRelationsCount = itemRelationRepository.count();
        return (int) itemRelationsCount + 1;
    }

    public ItemRelation create(ItemRelationCore itemRelationCore) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(itemRelationCore, "ItemRelation");

        ItemRelation itemRelation = new ItemRelation();

        if (StringUtils.isBlank(itemRelationCore.getCode())) {
            errors.rejectValue("code", "field.required", "Item relation code is required.");
        } else itemRelation.setCode(itemRelationCore.getCode());

        if (StringUtils.isBlank(itemRelationCore.getLabel())) {
            errors.rejectValue("label", "field.required", "Item relation label is required.");
        } else itemRelation.setLabel(itemRelationCore.getLabel());

        if (Objects.isNull(itemRelationCore.getOrd())) {
            errors.rejectValue("ord", "field.required", "Item ord not present in creation");
        } else itemRelation.setOrd(itemRelationCore.getOrd());

        itemRelationRepository.save(itemRelation);

        if (Objects.isNull(itemRelationCore.getInverseOf()) || itemRelationCore.getInverseOf().isEmpty()) {

            itemRelation.setInverseOf(null);

        } else {
            ItemRelation inverseOfItemRelation = itemRelationRepository.getItemRelationByCode(itemRelationCore.getInverseOf());

            if (!Objects.isNull(inverseOfItemRelation) && Objects.isNull(inverseOfItemRelation.getInverseOf())) {

                if (!Objects.isNull(inverseOfItemRelation.getInverseOf()))
                    errors.rejectValue("inverseOf", "field.isAlreadyInUse", "Item relation with inverse of already is assigned.");
                else {
                    itemRelation.setInverseOf(inverseOfItemRelation);
                    inverseOfItemRelation.setInverseOf(itemRelation);
                    itemRelationRepository.save(inverseOfItemRelation);
                }

            } else
                errors.rejectValue("inverseOf", "field.notExist", "Item relation inverse of with given code does not exist or is already assigned.");
        }

        if (errors.hasErrors())
            throw new ValidationException(errors);

        itemRelationRepository.save(itemRelation);
        reorderItemRelations(itemRelation.getCode(), itemRelation.getOrd());

        return itemRelation;
    }


}
