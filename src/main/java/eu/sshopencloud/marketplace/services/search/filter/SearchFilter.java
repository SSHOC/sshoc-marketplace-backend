package eu.sshopencloud.marketplace.services.search.filter;

import eu.sshopencloud.marketplace.model.search.IndexItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SearchFilter {

    CATEGORY(IndexItem.CATEGORY_FIELD, FilterType.VALUES_SELECTION_FILTER);

    private String key;

    private FilterType type;

    public static SearchFilter ofKey(String filterName) {
        for (SearchFilter filter : SearchFilter.values()) {
            if (filter.key.equalsIgnoreCase(filterName)) {
                return filter;
            }
        }
        return null;
    }

}
