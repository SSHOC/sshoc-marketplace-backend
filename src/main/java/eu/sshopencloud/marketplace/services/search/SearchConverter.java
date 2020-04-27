package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.dto.search.CountedConcept;
import eu.sshopencloud.marketplace.dto.search.CountedPropertyType;
import eu.sshopencloud.marketplace.dto.search.SearchConcept;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.dto.search.SearchItem;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.mappers.items.ItemCategoryConverter;
import lombok.experimental.UtilityClass;
import org.springframework.data.solr.core.query.result.FacetFieldEntry;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@UtilityClass
public class SearchConverter {

    public SearchItem convertIndexItem(IndexItem indexItem) {
        return SearchItem.builder()
                .id(indexItem.getId()).label(indexItem.getLabel()).description(indexItem.getDescription())
                .category(ItemCategoryConverter.convertCategory(indexItem.getCategory()))
                .build();
    }

    public CountedConcept convertCategoryFacet(FacetFieldEntry entry, List<ItemCategory> categories, Map<ItemCategory, Concept> concepts) {
        String code = entry.getValue();
        ItemCategory category = ItemCategoryConverter.convertCategory(code);
        Concept concept = concepts.get(category);
        VocabularyId vocabulary = new VocabularyId();
        vocabulary.setCode(concept.getVocabulary().getCode());
        CountedConcept.CountedConceptBuilder builder = CountedConcept.builder();
        builder.code(code).vocabulary(vocabulary).label(concept.getLabel()).notation(concept.getNotation()).ord(concept.getOrd()).definition(concept.getDefinition()).uri(concept.getUri());
        builder.count(entry.getValueCount());
        if (categories == null) {
            builder.checked(false);
        } else {
            builder.checked(categories.contains(category));
        }
        return builder.build();
    }

    public static void fillMissingCategories(List<CountedConcept> countedCategories, List<ItemCategory> categories, Map<ItemCategory, Concept> concepts) {
        for (ItemCategory category : ItemCategory.values()) {
            if (countedCategories.stream().noneMatch(countedCategory -> Objects.equals(category.getValue(), countedCategory.getCode()))) {
                Concept concept = concepts.get(category);
                VocabularyId vocabulary = new VocabularyId();
                vocabulary.setCode(concept.getVocabulary().getCode());
                CountedConcept.CountedConceptBuilder builder = CountedConcept.builder();
                builder.code(category.getValue()).vocabulary(vocabulary).label(concept.getLabel()).notation(concept.getNotation()).ord(concept.getOrd()).definition(concept.getDefinition()).uri(concept.getUri());
                builder.count(0);
                if (categories == null) {
                    builder.checked(false);
                } else {
                    builder.checked(categories.contains(category));
                }
                countedCategories.add(builder.build());
            }
        }
    }

    public SearchConcept convertIndexConcept(IndexConcept indexConcept) {
        VocabularyId vocabulary = new VocabularyId();
        vocabulary.setCode(indexConcept.getVocabularyCode());
        return SearchConcept.builder()
                .code(indexConcept.getCode()).vocabulary(vocabulary).label(indexConcept.getLabel()).notation(indexConcept.getNotation()).definition(indexConcept.getDefinition())
                .uri(indexConcept.getUri())
                .types(indexConcept.getTypes().stream()
                        .map(type -> {
                            PropertyTypeId propertyType = new PropertyTypeId();
                            propertyType.setCode(type);
                            return propertyType;
                        })
                        .collect(Collectors.toList()))
                .build();
    }

    public CountedPropertyType convertPropertyTypeFacet(FacetFieldEntry entry, List<String> types, Map<String, PropertyType> propertyTypes) {
        String code = entry.getValue();
        PropertyType propertyType = propertyTypes.get(code);
        CountedPropertyType.CountedPropertyTypeBuilder builder = CountedPropertyType.builder();
        builder.code(code).label(propertyType.getLabel());
        builder.count(entry.getValueCount());
        if (types == null) {
            builder.checked(false);
        } else {
            builder.checked(types.contains(code));
        }
        return builder.build();
    }

}
