package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.dto.search.SearchItem;
import eu.sshopencloud.marketplace.services.items.ItemCategoryConverter;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.solr.core.query.Field;
import org.springframework.data.solr.core.query.result.FacetFieldEntry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class SearchConverter {

    public SearchItem convertIndexItem(IndexItem indexItem) {
        return SearchItem.builder()
                .id(indexItem.getId()).name(indexItem.getName()).description(indexItem.getDescription())
                .category(ItemCategoryConverter.convertCategory(indexItem.getCategory()))
                .build();
    }

    public Map<ItemCategory, Long> convertCategoryFacet(Page<FacetFieldEntry> facetResultPage) {
        return facetResultPage.getContent().stream()
                .collect(Collectors.toMap(entry -> ItemCategoryConverter.convertCategory(((FacetFieldEntry) entry).getValue()), FacetFieldEntry::getValueCount));
    }

}
