package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeVocabularyRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import jdk.nashorn.internal.objects.NativeArray;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyTypeService {

    private final PropertyTypeRepository propertyTypeRepository;

    private final PropertyTypeVocabularyRepository propertyTypeVocabularyRepository;

    private final VocabularyRepository vocabularyRepository;

    public List<PropertyType> getAllPropertyTypes() {
        List<PropertyType> propertyTypes = propertyTypeRepository.findAll(new Sort(Sort.Direction.ASC, "ord"));
        for (PropertyType propertyType: propertyTypes) {
            propertyType.setAllowedVocabularies(getAllowedVocabulariesForPropertyType(propertyType));
        }
        return propertyTypes;
    }

    private List<VocabularyInline> getAllowedVocabulariesForPropertyType(PropertyType propertyType) {
        List<VocabularyInline> allowedVocabularies = new ArrayList<VocabularyInline>();
        List<PropertyTypeVocabulary> propertyTypeVocabularies = propertyTypeVocabularyRepository.findPropertyTypeVocabularyByPropertyTypeCode(propertyType.getCode());
        for (PropertyTypeVocabulary propertyTypeVocabulary: propertyTypeVocabularies) {
            Vocabulary vocabulary = vocabularyRepository.getOne(propertyTypeVocabulary.getVocabularyId());
            VocabularyInline allowedVocabulary = new VocabularyInline();
            allowedVocabulary.setId(vocabulary.getId());
            allowedVocabulary.setLabel(vocabulary.getLabel());
            allowedVocabularies.add(allowedVocabulary);
        }
        return allowedVocabularies;
    }


}
