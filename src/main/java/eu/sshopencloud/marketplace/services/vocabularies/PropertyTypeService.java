package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.vocabularies.*;
import eu.sshopencloud.marketplace.mappers.vocabularies.PropertyTypeMapper;
import eu.sshopencloud.marketplace.mappers.vocabularies.VocabularyBasicMapper;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeVocabularyRepository;
import eu.sshopencloud.marketplace.services.vocabularies.exception.PropertyTypeAlreadyExistsException;
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
@Transactional(rollbackFor = Throwable.class)
@RequiredArgsConstructor
public class PropertyTypeService {

    private final PropertyTypeRepository propertyTypeRepository;
    private final PropertyTypeVocabularyRepository propertyTypeVocabularyRepository;

    private VocabularyService vocabularyService;


    public PaginatedPropertyTypes getPropertyTypes(String q, PageCoords pageCoords) {
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

        return PaginatedPropertyTypes.builder()
                .propertyTypes(propertyTypes)
                .page(pageCoords.getPage())
                .perpage(pageCoords.getPerpage())
                .pages(propertyTypesPage.getTotalPages())
                .hits(propertyTypesPage.getTotalElements())
                .count(propertyTypesPage.getNumberOfElements())
                .build();
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

    public void removePropertyTypesAssociations(String vocabularyCode) {
        propertyTypeVocabularyRepository.deleteByVocabularyCode(vocabularyCode);
    }

    private void updateAllowedVocabularies(List<String> allowedVocabularies, PropertyType propertyType) {
        propertyTypeVocabularyRepository.deleteByPropertyTypeCode(propertyType.getCode());

        for (String vocabularyCode : allowedVocabularies) {
            Vocabulary vocabulary = vocabularyService.loadVocabulary(vocabularyCode);
            PropertyTypeVocabulary propertyTypeVocabulary = new PropertyTypeVocabulary(propertyType, vocabulary);

            propertyTypeVocabularyRepository.save(propertyTypeVocabulary);
        }
    }

    private int getMaxOrdForPropertyTypes() {
        Integer maxOrd = propertyTypeRepository.findMaxPropertyTypeOrd();
        return (maxOrd != null) ? maxOrd : 0;
    }

    public synchronized PropertyTypeDto createPropertyType(String code, PropertyTypeCore propertyTypeCore) throws PropertyTypeAlreadyExistsException {
        if (propertyTypeRepository.existsById(code))
            throw new PropertyTypeAlreadyExistsException(code);

        if (!PropertyTypeClass.CONCEPT.equals(propertyTypeCore.getType()) && propertyTypeCore.getAllowedVocabularies() != null)
            throw new IllegalArgumentException("Allowed vocabularies are suitable only for property types with concept values");

        int maxOrd = getMaxOrdForPropertyTypes();
        PropertyType propertyType = new PropertyType(code, propertyTypeCore.getType(), propertyTypeCore.getLabel(), maxOrd + 1);
        propertyType = propertyTypeRepository.save(propertyType);

        if (propertyTypeCore.getAllowedVocabularies() != null)
            updateAllowedVocabularies(propertyTypeCore.getAllowedVocabularies(), propertyType);

        return PropertyTypeMapper.INSTANCE.toDto(propertyType);
    }

    public PropertyTypeDto updatePropertyType(String code, PropertyTypeCore propertyTypeCore) {
        PropertyType propertyType = loadPropertyType(code);

        propertyType.setLabel(propertyTypeCore.getLabel());

        if (propertyTypeCore.getType() != null && !propertyType.getType().equals(propertyTypeCore.getType()))
            throw new IllegalArgumentException("Property type value class (type) is immutable");

        if (propertyTypeCore.getAllowedVocabularies() != null)
            updateAllowedVocabularies(propertyTypeCore.getAllowedVocabularies(), propertyType);

        return PropertyTypeMapper.INSTANCE.toDto(propertyType);
    }

    public synchronized void removePropertyType(String propertyTypeCode) {
        PropertyType propertyType = loadPropertyType(propertyTypeCode);
        int gapOrd = propertyType.getOrd();

        propertyTypeVocabularyRepository.deleteByPropertyTypeCode(propertyTypeCode);
        propertyTypeRepository.delete(propertyType);

        propertyTypeRepository.shiftSucceedingPropertyTypesOrder(gapOrd, -1);
    }

    public synchronized void reorderPropertyTypes(PropertyTypesReordering reordering) {
        reordering.getShifts().forEach(this::shiftPropertyTypeOrder);
    }

    private void shiftPropertyTypeOrder(PropertyTypeReorder shift) {
        PropertyType propertyType = loadPropertyType(shift.getCode());

        propertyType.setOrd(shift.getOrd());
        propertyTypeRepository.shiftSucceedingPropertyTypesOrder(shift.getOrd(), 1);
    }

    private PropertyType loadPropertyType(String propertyTypeCode) {
        return propertyTypeRepository.findById(propertyTypeCode)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Property type with code = '%s' not found", propertyTypeCode)));
    }
}
