package eu.sshopencloud.marketplace.services.search.filter;

import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SearchFilter {

    CATEGORY(IndexType.ITEMS, IndexItem.CATEGORY_FIELD, FilterType.VALUES_SELECTION_FILTER, true),

    ACTIVITY(IndexType.ITEMS, IndexItem.DYNAMIC_PROPERTIES_FACET_FIELDS.get(IndexItem.FACETING_DYNAMIC_PROPERTY_ACTIVITY_TAG),
            FilterType.VALUES_SELECTION_FILTER, false),

    SOURCE(IndexType.ITEMS, IndexItem.SOURCE_FIELD, FilterType.VALUES_SELECTION_FILTER, false),

    KEYWORD(IndexType.ITEMS, IndexItem.DYNAMIC_PROPERTIES_FACET_FIELDS.get(IndexItem.FACETING_DYNAMIC_PROPERTY_KEYWORD_TAG),
            FilterType.VALUES_SELECTION_FILTER, false),

    LANGUAGE(IndexType.ITEMS, IndexItem.DYNAMIC_PROPERTIES_FACET_FIELDS.get(IndexItem.FACETING_DYNAMIC_PROPERTY_LANGUAGE_TAG),
            FilterType.VALUES_SELECTION_FILTER, false),

    PROPERTY_TYPE(IndexType.CONCEPTS, IndexConcept.TYPES_FIELD, FilterType.VALUES_SELECTION_FILTER, true),

    CANDIDATE(IndexType.CONCEPTS, IndexConcept.CANDIDATE_FIELD, FilterType.VALUES_SELECTION_FILTER, false);


    private IndexType indexType;

    private String key;

    private FilterType type;

    private boolean main;

    public static SearchFilter ofKey(String filterName, IndexType indexType) {
        for (SearchFilter filter : SearchFilter.values()) {
            if (IndexItem.DYNAMIC_PROPERTIES_FACET_FIELDS.containsKey(filterName)) {
                if (filter.key.equals(IndexItem.DYNAMIC_PROPERTIES_FACET_FIELDS.get(filterName)) && filter.getIndexType().equals(indexType) && !filter.isMain()) {
                    return filter;
                }
            }
            if (filter.key.replace('_', '-').equalsIgnoreCase(filterName) && filter.getIndexType().equals(indexType) && !filter.isMain()) {
                return filter;
            }
        }
        return null;
    }

    public static final String ITEMS_INDEX_TYPE_FILTERS = "activity, source, keyword, language";

    public static final String ITEMS_INDEX_TYPE_PROPERTIES = "status, owner, source, contributor, external-identifier";

    public static final String CONCEPT_INDEX_TYPE_FILTERS = "candidate";

}
