package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeDto;
import eu.sshopencloud.marketplace.mappers.vocabularies.PropertyTypeMapper;
import eu.sshopencloud.marketplace.mappers.vocabularies.VocabularyBasicMapper;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeVocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PropertyTypeService {

    private final PropertyTypeRepository propertyTypeRepository;

    private final PropertyTypeVocabularyRepository propertyTypeVocabularyRepository;

    public List<PropertyTypeDto> getPropertyTypes(String q, PageCoords pageCoords) {
        ExampleMatcher queryPropertyTypeMatcher = ExampleMatcher.matchingAny()
                .withMatcher("label", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        PropertyType queryPropertyType = new PropertyType();
        queryPropertyType.setLabel(q);

        Page<PropertyType> propertyTypesPage = propertyTypeRepository.findAll(Example.of(queryPropertyType, queryPropertyTypeMatcher),
                PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("ord"))));
        List<PropertyTypeDto> propertyTypes = PropertyTypeMapper.INSTANCE.toDto(propertyTypesPage.getContent());

        for (PropertyTypeDto propertyType: propertyTypes) {
            completePropertyType(propertyType);
        }
        return propertyTypes;
    }

    public void completePropertyType(PropertyTypeDto propertyType) {
        propertyType.setAllowedVocabularies(VocabularyBasicMapper.INSTANCE.toDto(getAllowedVocabulariesForPropertyType(propertyType.getCode())));
    }

    public Map<String, PropertyType> getAllPropertyTypes() {
        return propertyTypeRepository.findAll().stream().collect(Collectors.toMap(PropertyType::getCode, propertyType -> propertyType));
    }

    public PropertyType getPropertyType(String code) {
        Optional<PropertyType> propertyType = propertyTypeRepository.findById(code);
        if (!propertyType.isPresent()) {
            throw new EntityNotFoundException("Unable to find " + PropertyType.class.getName() + " with code " + code);
        }
        return propertyType.get();
    }

    public List<Vocabulary> getAllowedVocabulariesForPropertyType(String propertyTypeCode) {
        List<PropertyTypeVocabulary> propertyTypeVocabularies = propertyTypeVocabularyRepository.findByPropertyTypeCode(propertyTypeCode);
        return propertyTypeVocabularies.stream().map(PropertyTypeVocabulary::getVocabulary).collect(Collectors.toList());
    }

    public List<Vocabulary> getAllowedVocabulariesForPropertyType(PropertyType propertyType) {
        return getAllowedVocabulariesForPropertyType(propertyType.getCode());
    }

    public List<PropertyType> getAllowedPropertyTypesForVocabulary(String vocabularyCode) {
        List<PropertyTypeVocabulary> propertyTypeVocabularies = propertyTypeVocabularyRepository.findByVocabularyCode(vocabularyCode);
        return propertyTypeVocabularies.stream().map(PropertyTypeVocabulary::getPropertyType).collect(Collectors.toList());
    }

    public List<PropertyType> getAllowedPropertyTypesForVocabulary(Vocabulary vocabulary) {
        return getAllowedPropertyTypesForVocabulary(vocabulary.getCode());
    }

}
