package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.items.*;
import eu.sshopencloud.marketplace.mappers.items.ItemRelationMapper;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
import eu.sshopencloud.marketplace.repositories.items.ItemRelationRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.items.ItemRelationFactory;
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
import java.util.Locale;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemRelationService {

    private final ItemRelationRepository itemRelationRepository;

    private final ItemRelationFactory itemRelationFactory;

    private final ItemRelatedItemService itemRelatedItemService;

    public List<ItemRelationDto> getAllItemRelations() {
        return ItemRelationMapper.INSTANCE.toDto(itemRelationRepository.findAll(Sort.by(Sort.Order.asc("ord"))));
    }

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

        ItemRelation itemRelation = create(itemRelationCore);

        return ItemRelationMapper.INSTANCE.toDto(itemRelation);
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
            if (Objects.isNull(itemRelation.getInverseOf()) || itemRelation.getInverseOf().getCode().equals(itemRelationCore.getInverseOf())) {
                ItemRelation inverseItemRelation = itemRelationRepository.getItemRelationByCode(itemRelationCore.getInverseOf());
                if (!Objects.isNull(inverseItemRelation)) {
                    itemRelation.setInverseOf(inverseItemRelation);
                    if (Objects.isNull(inverseItemRelation.getInverseOf())) {
                        inverseItemRelation.setInverseOf(itemRelation);
                        itemRelationRepository.save(inverseItemRelation);
                    }
                }
            }
        } else {
            itemRelation.setInverseOf(null);
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

    public void reorderItemRelations(ItemRelationReordering reordering) {
        int maxOrd = getMaxOrdForItemRelation();

        reordering.getShifts().forEach(shift -> {
            int targetOrd = shift.getOrd();

            if (targetOrd < 1 || targetOrd > maxOrd)
                throw new IllegalArgumentException(String.format("Invalid shift ord value: %d", targetOrd));
        });

        List<ItemRelation> itemRelations = loadItemRelation();
        reordering.getShifts().forEach(shift -> shiftItemRelation(itemRelations, shift));

        renumberItemRelations(itemRelations);
    }

    private void shiftItemRelation(List<ItemRelation> itemRelations, ItemRelationReorder shift) {
        ItemRelation itemRelation = itemRelations.stream()
                .filter(itemRel -> itemRel.getCode().equals(shift.getCode()))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                String.format("Item relation with code '%s' does not exist", shift.getCode())
                        )
                );

        itemRelations.remove(itemRelation);
        itemRelations.add(shift.getOrd() - 1, itemRelation);
    }

    private void renumberItemRelations(List<ItemRelation> itemRelations) {
        int ord = 1;

        for (ItemRelation itemRelation : itemRelations) {
            if (ord != itemRelation.getOrd())
                itemRelation.setOrd(ord);

            ord += 1;
        }
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

        ItemRelation inverseOfItemRelation = new ItemRelation();

        if (!Objects.isNull(itemRelationCore.getInverseOf()) && !itemRelationCore.getInverseOf().isEmpty()) {
            inverseOfItemRelation.setOrd(itemRelationCore.getOrd() + 1);
            //inverseOfItemRelation.setInverseOf(itemRelation);
            inverseOfItemRelation.setLabel(StringUtils.capitalize(itemRelationCore.getInverseOf().replaceAll("-", " ")));
            inverseOfItemRelation.setCode(itemRelationCore.getInverseOf());


            itemRelationRepository.save(inverseOfItemRelation);
            reorderItemRelations(inverseOfItemRelation.getCode(), inverseOfItemRelation.getOrd());
            itemRelation.setInverseOf(inverseOfItemRelation);
            //save this instance

        } else errors.rejectValue("inverseOf", "field.notExist", "Item relation inverse of does not exist.");


        if (errors.hasErrors())
            throw new ValidationException(errors);

        itemRelationRepository.save(itemRelation);
        inverseOfItemRelation.setInverseOf(itemRelation);
        itemRelationRepository.save(inverseOfItemRelation);

        reorderItemRelations(itemRelation.getCode(), itemRelation.getOrd());

        //reorderItemRelations(itemRelation.getInverseOf().getCode(), itemRelation.getInverseOf().getOrd());

        return itemRelation;
    }


}
