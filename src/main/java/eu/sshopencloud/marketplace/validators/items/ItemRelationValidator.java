package eu.sshopencloud.marketplace.validators.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
import eu.sshopencloud.marketplace.repositories.items.ItemRelationRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemRelationValidator {

    private final ItemRelationRepository itemRelationRepository;


    public ItemRelation validate(ItemRelationId itemRelationId) throws ValidationException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(itemRelationId, "ItemRelation");

        ItemRelation itemRelation = null;
        if (StringUtils.isBlank(itemRelationId.getCode())) {
            errors.rejectValue("code", "field.required", "Item relation code is required.");
        } else {
            Optional<ItemRelation> itemRelationHolder = itemRelationRepository.findById(itemRelationId.getCode());
            if (!itemRelationHolder.isPresent()) {
                errors.rejectValue("code", "field.notExist", "Item relation does not exist.");
            } else {
                itemRelation = itemRelationHolder.get();
            }
        }

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return itemRelation;
        }
    }

}
