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
import org.springframework.validation.Errors;

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

    public ItemRelation create(ItemRelationCore itemRelationCore, ItemRelation itemRelation, Errors errors) throws ValidationException {

        if (StringUtils.isBlank(itemRelationCore.getCode())) {
            errors.pushNestedPath("code");
            errors.rejectValue("code", "field.required", "Item relation code is required.");
            errors.popNestedPath();
        } else itemRelation.setCode(itemRelationCore.getCode());

        if (StringUtils.isBlank(itemRelationCore.getLabel())) {
            errors.pushNestedPath("label");
            errors.rejectValue("label", "field.required", "Item relation label is required.");
            errors.popNestedPath();
        } else itemRelation.setLabel(itemRelationCore.getLabel());

        if (Objects.isNull(itemRelationCore.getOrd())) {
            errors.pushNestedPath("ord");
            errors.rejectValue("ord", "field.required", "Item ord not present in creation");
            errors.popNestedPath();
        }

        if (Objects.isNull(itemRelationCore.getInverseOf()) || itemRelationCore.getInverseOf().isEmpty()) {
            itemRelation.setInverseOf(null);
        } else {
            ItemRelation inverseOfItemRelation = itemRelationRepository.getItemRelationByCode(itemRelationCore.getInverseOf());

            if (!Objects.isNull(inverseOfItemRelation) && Objects.isNull(inverseOfItemRelation.getInverseOf())) {

                if (!Objects.isNull(inverseOfItemRelation.getInverseOf())) {
                    errors.pushNestedPath("inverseOf");
                    errors.rejectValue("inverseOf", "field.isAlreadyInUse", "Item relation with inverse of already is assigned.");
                    errors.popNestedPath();
                } else {
                    itemRelation.setInverseOf(inverseOfItemRelation);
                }
            } else {
                errors.pushNestedPath("inverseOf");
                errors.rejectValue("inverseOf", "field.notExist", "Item relation inverse of with given code does not exist or is already assigned.");
                errors.popNestedPath();
            }
        }

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return itemRelation;
    }

}
