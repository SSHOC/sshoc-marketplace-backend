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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Throwable.class)
@RequiredArgsConstructor
public class PropertyTypeService {

    private final PropertyTypeRepository propertyTypeRepository;
    private final PropertyTypeVocabularyRepository propertyTypeVocabularyRepository;

    private final AllowedVocabulariesService allowedVocabulariesService;


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

    public PropertyTypeDto getPropertyType(String code) {
        PropertyType propertyType = loadPropertyType(code);
        PropertyTypeDto propertyTypeDto = PropertyTypeMapper.INSTANCE.toDto(propertyType);

        completePropertyType(propertyTypeDto);

        return propertyTypeDto;
    }

    public PropertyType loadPropertyType(String propertyTypeCode) {
        return propertyTypeRepository.findById(propertyTypeCode)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Property type with code = '%s' not found", propertyTypeCode)));
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

    private int getMaxOrdForPropertyTypes() {
        Integer maxOrd = propertyTypeRepository.findMaxPropertyTypeOrd();
        return (maxOrd != null) ? maxOrd : 1;
    }

    public synchronized PropertyTypeDto createPropertyType(PropertyTypeCore propertyTypeCore)
            throws PropertyTypeAlreadyExistsException {

        String code = propertyTypeCore.getCode();
        if (code == null)
            throw new IllegalArgumentException("Property type code is not present");

        if (propertyTypeRepository.existsById(code))
            throw new PropertyTypeAlreadyExistsException(code);

        if (!PropertyTypeClass.CONCEPT.equals(propertyTypeCore.getType()) && propertyTypeCore.getAllowedVocabularies() != null)
            throw new IllegalArgumentException("Allowed vocabularies are suitable only for property types with concept values");

        int maxOrd = getMaxOrdForPropertyTypes();
        PropertyType propertyType = new PropertyType(code, propertyTypeCore.getType(), propertyTypeCore.getLabel(), maxOrd + 1);
        propertyType = propertyTypeRepository.save(propertyType);

        if (propertyTypeCore.getAllowedVocabularies() != null)
            allowedVocabulariesService.updateForPropertyType(propertyTypeCore.getAllowedVocabularies(), propertyType);

        PropertyTypeDto dto = PropertyTypeMapper.INSTANCE.toDto(propertyType);
        completePropertyType(dto);

        return dto;
    }

    public PropertyTypeDto updatePropertyType(String code, PropertyTypeCore propertyTypeCore) {
        PropertyType propertyType = loadPropertyType(code);

        propertyType.setLabel(propertyTypeCore.getLabel());

        if (propertyTypeCore.getType() != null && !propertyType.getType().equals(propertyTypeCore.getType()))
            throw new IllegalArgumentException("Property type value class (type) is immutable");

        if (propertyTypeCore.getAllowedVocabularies() != null)
            allowedVocabulariesService.updateForPropertyType(propertyTypeCore.getAllowedVocabularies(), propertyType);

        PropertyTypeDto dto = PropertyTypeMapper.INSTANCE.toDto(propertyType);
        completePropertyType(dto);

        return dto;
    }

    public synchronized void removePropertyType(String propertyTypeCode) {
        PropertyType propertyType = loadPropertyType(propertyTypeCode);
        int gapOrd = propertyType.getOrd();

        allowedVocabulariesService.updateForPropertyType(Collections.emptyList(), propertyType);

        propertyTypeRepository.delete(propertyType);
        propertyTypeRepository.shiftSucceedingPropertyTypesOrder(gapOrd, -1);
    }

    public synchronized void reorderPropertyTypes(PropertyTypesReordering reordering) {
        int maxOrd = getMaxOrdForPropertyTypes();

        reordering.getShifts().forEach(shift -> {
            int targetOrd = shift.getOrd();

            if (targetOrd < 1 || targetOrd > maxOrd)
                throw new IllegalArgumentException(String.format("Invalid shift ord value: %d", targetOrd));
        });

        reordering.getShifts().forEach(this::shiftPropertyTypeOrder);
    }

    private void shiftPropertyTypeOrder(PropertyTypeReorder shift) {
        PropertyType propertyType = loadPropertyType(shift.getCode());

        propertyTypeRepository.shiftSucceedingPropertyTypesOrder(propertyType.getOrd(), -1);
        propertyTypeRepository.shiftSucceedingPropertyTypesOrder(shift.getOrd() - 1, 1);

        propertyType = propertyTypeRepository.getOne(shift.getCode());
        propertyType.setOrd(shift.getOrd());
    }
}
