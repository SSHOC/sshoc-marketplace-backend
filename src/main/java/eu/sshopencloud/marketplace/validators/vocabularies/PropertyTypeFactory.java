package eu.sshopencloud.marketplace.validators.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PropertyTypeFactory {

    private final PropertyTypeRepository propertyTypeRepository;


    public PropertyType create(PropertyTypeId propertyTypeId, Errors errors) {
        if (StringUtils.isBlank(propertyTypeId.getCode())) {
            errors.rejectValue("code", "field.required", "Property type code is required.");
            return null;
        }
        Optional<PropertyType> propertyTypeHolder = propertyTypeRepository.findById(propertyTypeId.getCode());
        if (propertyTypeHolder.isEmpty()) {
            errors.rejectValue("code", "field.notExist", "Property type does not exist.");
            return null;
        }

        return propertyTypeHolder.get();
    }
}
