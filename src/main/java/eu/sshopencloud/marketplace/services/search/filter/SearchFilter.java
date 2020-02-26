package eu.sshopencloud.marketplace.services.search.filter;

import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SearchFilter {

    CATEGORY(IndexType.ITEMS, IndexItem.CATEGORY_FIELD, FilterType.VALUES_SELECTION_FILTER),

    OBJECT_TYPE(IndexType.ITEMS, IndexItem.OBJECT_TYPE_FIELD, FilterType.VALUES_SELECTION_FILTER),

    ACTIVITY(IndexType.ITEMS, IndexItem.ACTIVITY_FIELD, FilterType.VALUES_SELECTION_FILTER),

    KEYWORD(IndexType.ITEMS, IndexItem.KEYWORD_FIELD, FilterType.VALUES_SELECTION_FILTER),

    PROPERTY_TYPE(IndexType.CONCEPTS, IndexConcept.TYPES_FIELD, FilterType.VALUES_SELECTION_FILTER);

    private IndexType indexType;

    private String key;

    private FilterType type;

    public static SearchFilter ofKey(String filterName, IndexType indexType) {
        for (SearchFilter filter : SearchFilter.values()) {
            if (filter.key.replace('_', '-').equalsIgnoreCase(filterName) && filter.getIndexType().equals(indexType)) {
                return filter;
            }
        }
        return null;
    }

}
