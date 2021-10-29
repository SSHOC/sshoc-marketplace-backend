package eu.sshopencloud.marketplace.validators.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelationCore;
import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
import eu.sshopencloud.marketplace.repositories.items.ItemRelationRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemRelationFactory {

    private final ItemRelationRepository itemRelationRepository;


    public ItemRelation create(ItemRelationId itemRelationId) throws ValidationException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(itemRelationId, "ItemRelation");

        ItemRelation itemRelation = null;
        if (StringUtils.isBlank(itemRelationId.getCode())) {
            errors.rejectValue("code", "field.required", "Item relation code is required.");
        } else {
            Optional<ItemRelation> itemRelationHolder = itemRelationRepository.findById(itemRelationId.getCode());
            if (itemRelationHolder.isEmpty()) {
                errors.rejectValue("code", "field.notExist", "Item relation does not exist.");
            } else {
                itemRelation = itemRelationHolder.get();
            }
        }

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return itemRelation;
    }

    public ItemRelation create(ItemRelationCore itemRelationCore, String code) throws ValidationException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(itemRelationCore, "ItemRelation");

        ItemRelation itemRelation = getOrCreateItemRelation(code);

        if (code != null) {
            itemRelationCore.setCode(code);
        }
        if (StringUtils.isBlank(itemRelationCore.getCode())) {
            errors.rejectValue("code", "field.required", "Item relation code is required.");
        } else {
            itemRelation.setCode(itemRelationCore.getCode());
        }

        if (StringUtils.isBlank(itemRelationCore.getLabel())) {
            errors.rejectValue("label", "field.required", "Item relation label is required.");
        } else {
            itemRelation.setLabel(itemRelationCore.getLabel());
        }

        if (StringUtils.isBlank(itemRelationCore.getInverseOf())) {
            itemRelation.setInverseOf(null);
        } else {
            ItemRelation inverseOfItemRelation = itemRelationRepository.getItemRelationByCode(itemRelationCore.getInverseOf());

            if (Objects.isNull(inverseOfItemRelation)) {
                errors.rejectValue("inverseOf", "field.notExist", "Item relation inverse of with given code does not exist.");
            } else {
                if (!Objects.isNull(inverseOfItemRelation.getInverseOf()) && !inverseOfItemRelation.getInverseOf().getCode().equals(itemRelation.getCode())) {
                    errors.rejectValue("inverseOf", "field.isAlreadyInUse", "Item relation with inverse of is already assigned.");
                } else {
                    itemRelation.setInverseOf(inverseOfItemRelation);
                }
            }
        }

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return itemRelation;
    }

    private ItemRelation getOrCreateItemRelation(String code) {
        if (code != null) {
            return itemRelationRepository.getOne(code);
        }
        return new ItemRelation();
    }

}
