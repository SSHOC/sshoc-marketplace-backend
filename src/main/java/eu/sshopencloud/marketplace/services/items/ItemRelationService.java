package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelationCore;
import eu.sshopencloud.marketplace.dto.items.ItemRelationDto;
import eu.sshopencloud.marketplace.dto.items.ItemRelationReorder;
import eu.sshopencloud.marketplace.dto.items.ItemRelationReordering;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeReorder;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypesReordering;
import eu.sshopencloud.marketplace.mappers.items.ItemRelationMapper;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.repositories.items.ItemRelationRepository;
import eu.sshopencloud.marketplace.validators.items.ItemRelationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemRelationService {

    private final ItemRelationRepository itemRelationRepository;

    private final ItemRelationFactory itemRelationFactory;

    public List<ItemRelationDto> getAllItemRelations() {
        return ItemRelationMapper.INSTANCE.toDto(itemRelationRepository.findAll(Sort.by(Sort.Order.asc("ord"))));
    }

    public ItemRelationDto getItemRelation(String code) {
        ItemRelation itemRelation = itemRelationRepository.getItemRelationByCode(code);
        return ItemRelationMapper.INSTANCE.toDto(itemRelation);
    }

    public ItemRelationDto createItemRelation(ItemRelationCore itemRelationCore) {
        ItemRelation itemRelation = itemRelationFactory.create(itemRelationCore);
        itemRelationRepository.save(itemRelation);
        return ItemRelationMapper.INSTANCE.toDto(itemRelation);
    }

    public ItemRelationDto updateItemRelation(ItemRelationCore itemRelationCore) {
        ItemRelation itemRelation = new ItemRelation();

        if(!Objects.isNull(itemRelationCore.getOrd())){
            validateItemRelationPosition(itemRelationCore.getOrd());
            itemRelation.setOrd(itemRelationCore.getOrd());
            reorderItemRelations(itemRelationCore.getCode(), itemRelationCore.getOrd());
        }
        return ItemRelationMapper.INSTANCE.toDto(itemRelation);
    }

    //Eliza - flaga !!!
    public void deleteItemRelation(String code){
        ItemRelation itemRelation = itemRelationRepository.getItemRelationByCode(code);
        if(!Objects.isNull(itemRelation)) itemRelationRepository.delete(itemRelation);
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
        List<ItemRelation> itemRelations =  loadItemRelation();
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

    public void reorderPropertyTypes(ItemRelationReordering reordering) {
        int maxOrd = getMaxOrdForItemRelation();

        reordering.getShifts().forEach(shift -> {
            int targetOrd = shift.getOrd();

            if (targetOrd < 1 || targetOrd > maxOrd)
                throw new IllegalArgumentException(String.format("Invalid shift ord value: %d", targetOrd));
        });

        List<ItemRelation> propertyTypes = loadItemRelation() ;
        reordering.getShifts().forEach(shift -> shiftItemRelation(propertyTypes, shift));

        renumberItemRelations(propertyTypes);
    }

    private void shiftItemRelation(List<ItemRelation> itemRelations, ItemRelationReorder shift) {
        ItemRelation itemRelation = itemRelations.stream()
                .filter( itemRel -> itemRel.getCode().equals(shift.getCode()))
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
        long propertyTypesCount = itemRelationRepository.count();
        return (int) propertyTypesCount + 1;
    }


}
