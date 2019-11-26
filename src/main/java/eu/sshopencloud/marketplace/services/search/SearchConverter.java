package eu.sshopencloud.marketplace.services.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.sshopencloud.marketplace.dto.search.CountedConcept;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.dto.search.SearchItem;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
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

}
