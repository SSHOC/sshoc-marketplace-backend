package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.licenses.License;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeVocabularyRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import jdk.nashorn.internal.objects.NativeArray;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
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

    public List<PropertyType> getPropertyTypes(String q) {
        ExampleMatcher queryPropertyTypeMatcher = ExampleMatcher.matchingAny()
                .withMatcher("label", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        PropertyType queryPropertyType = new PropertyType();
        queryPropertyType.setLabel(q);

        List<PropertyType> propertyTypes = propertyTypeRepository.findAll(Example.of(queryPropertyType, queryPropertyTypeMatcher), new Sort(Sort.Direction.ASC, "ord"));
        for (PropertyType propertyType: propertyTypes) {
            propertyType.setAllowedVocabularies(getAllowedVocabulariesForPropertyType(propertyType));
        }
        return propertyTypes;
    }

    public List<VocabularyInline> getAllowedVocabulariesForPropertyType(PropertyType propertyType) {
        List<VocabularyInline> allowedVocabularies = new ArrayList<VocabularyInline>();
        List<PropertyTypeVocabulary> propertyTypeVocabularies = propertyTypeVocabularyRepository.findPropertyTypeVocabularyByPropertyTypeCode(propertyType.getCode());
        for (PropertyTypeVocabulary propertyTypeVocabulary: propertyTypeVocabularies) {
            Vocabulary vocabulary = vocabularyRepository.getOne(propertyTypeVocabulary.getVocabularyCode());
            VocabularyInline allowedVocabulary = new VocabularyInline();
            allowedVocabulary.setCode(vocabulary.getCode());
            allowedVocabulary.setLabel(vocabulary.getLabel());
            allowedVocabularies.add(allowedVocabulary);
        }
        return allowedVocabularies;
    }


}
