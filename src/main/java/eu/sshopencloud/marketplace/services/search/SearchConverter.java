package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.dto.search.*;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.mappers.items.ItemCategoryConverter;
import lombok.experimental.UtilityClass;
import org.springframework.data.solr.core.query.result.FacetFieldEntry;

import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class SearchConverter {

    public SearchItem convertIndexItem(IndexItem indexItem) {
        return SearchItem.builder()
                .id(indexItem.getId()).label(indexItem.getLabel()).description(indexItem.getDescription())
                .category(ItemCategoryConverter.convertCategory(indexItem.getCategory()))
                .build();
    }

    public LabeledCheckedCount convertCategoryFacet(FacetFieldEntry entry, List<ItemCategory> categories) {
        String code = entry.getValue();
        ItemCategory category = ItemCategoryConverter.convertCategory(code);

        return convertCategoryFacet(category, entry.getValueCount(), categories);
    }

    public LabeledCheckedCount convertCategoryFacet(ItemCategory category, long count, List<ItemCategory> categories) {
        List<ItemCategory> checkedCategories = (categories != null) ? categories : Collections.emptyList();

        return LabeledCheckedCount.builder()
                .count(count)
                .checked(checkedCategories.contains(category))
                .label(category.getLabel())
                .build();
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
