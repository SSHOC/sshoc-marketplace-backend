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

    private final PropertyService propertyService;

    private final AllowedVocabulariesService allowedVocabulariesService;


    public PaginatedPropertyTypes getPropertyTypes(String q, PageCoords pageCoords) {
        Page<PropertyType> propertyTypesPage = queryPropertyTypes(q, pageCoords);

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

    private Page<PropertyType> queryPropertyTypes(String q, PageCoords pageCoords) {
        if (q == null) {
            return propertyTypeRepository.findAll(
                    PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("ord")))
            );
        }

        ExampleMatcher queryPropertyTypeMatcher = ExampleMatcher.matchingAny()
                .withMatcher("label", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        PropertyType queryPropertyType = new PropertyType();
        queryPropertyType.setLabel(q);

        return propertyTypeRepository.findAll(
                Example.of(queryPropertyType, queryPropertyTypeMatcher),
                PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("ord")))
        );
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

    private int getMaxOrdForPropertyType() {
        long propertyTypesCount = propertyTypeRepository.count();
        return (int) propertyTypesCount + 1;
    }

    public PropertyTypeDto createPropertyType(PropertyTypeCore propertyTypeCore)
            throws PropertyTypeAlreadyExistsException {

        String code = propertyTypeCore.getCode();
        if (code == null)
            throw new IllegalArgumentException("Property type code is not present");

        if (propertyTypeRepository.existsById(code))
            throw new PropertyTypeAlreadyExistsException(code);

        if (!PropertyTypeClass.CONCEPT.equals(propertyTypeCore.getType()) && propertyTypeCore.getAllowedVocabularies() != null)
            throw new IllegalArgumentException("Allowed vocabularies are suitable only for property types with concept values");

        Integer ord = propertyTypeCore.getOrd();
        validatePropertyTypePosition(ord);

        if (ord == null)
            ord = getMaxOrdForPropertyType();

        PropertyType propertyType = new PropertyType(code, propertyTypeCore.getType(), propertyTypeCore.getLabel(), ord);
        propertyType = propertyTypeRepository.save(propertyType);

        reorderPropertyTypes(code, ord);

        if (propertyTypeCore.getAllowedVocabularies() != null)
            allowedVocabulariesService.updateForPropertyType(propertyTypeCore.getAllowedVocabularies(), propertyType);

        PropertyTypeDto dto = PropertyTypeMapper.INSTANCE.toDto(propertyType);
        completePropertyType(dto);

        return dto;
    }

    public PropertyTypeDto updatePropertyType(String code, PropertyTypeCore propertyTypeCore) {
        PropertyType propertyType = loadPropertyType(code);

        propertyType.setLabel(propertyTypeCore.getLabel());

        Integer ord = propertyTypeCore.getOrd();
        if (ord != null) {
            validatePropertyTypePosition(ord);

            propertyType.setOrd(ord);
            reorderPropertyTypes(code, ord);
        }

        if (propertyTypeCore.getType() != null && !propertyType.getType().equals(propertyTypeCore.getType()))
            throw new IllegalArgumentException("Property type value class (type) is immutable");

        if (propertyTypeCore.getAllowedVocabularies() != null)
            allowedVocabulariesService.updateForPropertyType(propertyTypeCore.getAllowedVocabularies(), propertyType);

        PropertyTypeDto dto = PropertyTypeMapper.INSTANCE.toDto(propertyType);
        completePropertyType(dto);

        return dto;
    }

    public void removePropertyType(String propertyTypeCode, boolean forceRemoval) {
        PropertyType propertyType = loadPropertyType(propertyTypeCode);

        if (propertyService.existPropertiesOfType(propertyType)) {
            if (!forceRemoval) {
                throw new IllegalArgumentException(
                        String.format(
                                "Cannot remove property type '%s' since there already exist properties of this type. " +
                                        "Use force=true parameter to remove the property type and the associated properties as well.",
                                propertyTypeCode
                        )
                );
            }

            propertyService.removePropertiesOfType(propertyType);
        }

        allowedVocabulariesService.updateForPropertyType(Collections.emptyList(), propertyType);
        propertyTypeRepository.delete(propertyType);

        reorderPropertyTypes(propertyTypeCode, null);
    }

    private void validatePropertyTypePosition(Integer ord) {
        if (ord == null)
            return;

        long propertyTypesCount = propertyTypeRepository.count();
        if (ord < 1 || ord > propertyTypesCount + 1) {
            throw new IllegalArgumentException(
                    String.format("Invalid position index: %d (maximum possible: %d)", ord, propertyTypesCount + 1)
            );
        }
    }

    private void reorderPropertyTypes(String propertyTypeCode, Integer propertyTypeOrd) {
        List<PropertyType> propertyTypes = loadPropertyTypes();
        int ord = 1;

        for (PropertyType propertyType : propertyTypes) {
            if (propertyType.getCode().equals(propertyTypeCode))
                continue;

            if (propertyTypeOrd != null && ord == propertyTypeOrd)
                ord++;

            if (propertyType.getOrd() != ord)
                propertyType.setOrd(ord);

            ord++;
        }
    }

    public void reorderPropertyTypes(PropertyTypesReordering reordering) {
        int maxOrd = getMaxOrdForPropertyType();

        reordering.getShifts().forEach(shift -> {
            int targetOrd = shift.getOrd();

            if (targetOrd < 1 || targetOrd > maxOrd)
                throw new IllegalArgumentException(String.format("Invalid shift ord value: %d", targetOrd));
        });

        List<PropertyType> propertyTypes = loadPropertyTypes();
        reordering.getShifts().forEach(shift -> shiftPropertyType(propertyTypes, shift));

        renumberPropertyTypes(propertyTypes);
    }

    private void shiftPropertyType(List<PropertyType> propertyTypes, PropertyTypeReorder shift) {
        PropertyType propertyType = propertyTypes.stream()
                .filter(propType -> propType.getCode().equals(shift.getCode()))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                String.format("Property type with code '%s' does not exist", shift.getCode())
                        )
                );

        propertyTypes.remove(propertyType);
        propertyTypes.add(shift.getOrd() - 1, propertyType);
    }

    private void renumberPropertyTypes(List<PropertyType> propertyTypes) {
        int ord = 1;

        for (PropertyType propertyType : propertyTypes) {
            if (ord != propertyType.getOrd())
                propertyType.setOrd(ord);

            ord += 1;
        }
    }

    private List<PropertyType> loadPropertyTypes() {
        return propertyTypeRepository.findAll(Sort.by(Sort.Order.asc("ord")));
    }
}
