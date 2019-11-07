package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeVocabularyRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PropertyTypeService {

    private final PropertyTypeRepository propertyTypeRepository;

    private final PropertyTypeVocabularyRepository propertyTypeVocabularyRepository;

    public List<PropertyType> getPropertyTypes(String q, int perpage) {
        ExampleMatcher queryPropertyTypeMatcher = ExampleMatcher.matchingAny()
                .withMatcher("label", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        PropertyType queryPropertyType = new PropertyType();
        queryPropertyType.setLabel(q);

        Page<PropertyType> propertyTypes = propertyTypeRepository.findAll(Example.of(queryPropertyType, queryPropertyTypeMatcher), PageRequest.of(0, perpage,new Sort(Sort.Direction.ASC, "ord")));
        for (PropertyType propertyType: propertyTypes.getContent()) {
            propertyType.setAllowedVocabularies(getAllowedVocabulariesForPropertyType(propertyType));
        }
        return propertyTypes.getContent();
    }

    public List<VocabularyInline> getAllowedVocabulariesForPropertyType(PropertyType propertyType) {
        List<VocabularyInline> allowedVocabularies = new ArrayList<VocabularyInline>();
        List<PropertyTypeVocabulary> propertyTypeVocabularies = propertyTypeVocabularyRepository.findPropertyTypeVocabularyByPropertyTypeCode(propertyType.getCode());
        for (PropertyTypeVocabulary propertyTypeVocabulary: propertyTypeVocabularies) {
            Vocabulary vocabulary = propertyTypeVocabulary.getVocabulary();
            VocabularyInline allowedVocabulary = new VocabularyInline();
            allowedVocabulary.setCode(vocabulary.getCode());
            allowedVocabulary.setLabel(vocabulary.getLabel());
            allowedVocabularies.add(allowedVocabulary);
        }
        return allowedVocabularies;
    }

    public PropertyType validate(String prefix, PropertyTypeId propertyType) throws DataViolationException {
        if (propertyType.getCode() == null) {
            throw new DataViolationException(prefix + "code", propertyType.getCode());
        }
        Optional<PropertyType> result = propertyTypeRepository.findById(propertyType.getCode());
        if (!result.isPresent()) {
            throw new DataViolationException(prefix + "code", propertyType.getCode());
        }
        return result.get();
    }

}
