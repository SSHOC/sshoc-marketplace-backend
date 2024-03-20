package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.conf.datetime.SolrDateTimeFormatter;
import eu.sshopencloud.marketplace.dto.search.*;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.mappers.items.ItemCategoryConverter;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.model.search.IndexActor;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import lombok.experimental.UtilityClass;
import org.apache.solr.client.solrj.response.FacetField;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class SearchConverter {

    public SearchItem convertIndexItem(IndexItem indexItem) {
        return SearchItem.builder()
                .id(Long.valueOf(indexItem.getVersionId()))
                .persistentId(indexItem.getPersistentId())
                .label(indexItem.getLabel())
                .version(indexItem.getVersion())
                .description(indexItem.getDescription())
                .category(ItemCategoryConverter.convertCategory(indexItem.getCategory()))
                .status(ItemStatus.of(indexItem.getStatus()))
                .owner(indexItem.getOwner())
                .lastInfoUpdate(SolrDateTimeFormatter.formatDateTime(ZonedDateTime.of(
                                LocalDateTime.ofInstant(indexItem.getLastInfoUpdate().toInstant(), ZoneOffset.UTC), ZoneOffset.UTC)))
                .build();
    }

    public SearchItemBasic convertIndexItemBasic(IndexItem indexItem) {
        return SearchItemBasic.builder()
                .id(Long.valueOf(indexItem.getVersionId()))
                .persistentId(indexItem.getPersistentId())
                .label(indexItem.getLabel())
                .version(indexItem.getVersion())
                .category(ItemCategoryConverter.convertCategory(indexItem.getCategory()))
                .lastInfoUpdate(SolrDateTimeFormatter.formatDateTime(ZonedDateTime.of(
                        LocalDateTime.ofInstant(indexItem.getLastInfoUpdate().toInstant(), ZoneOffset.UTC), ZoneOffset.UTC)))
                .build();
    }

    public SearchActor convertNotRestrictedIndexActor(IndexActor indexActor) {
        return SearchActor.builder()
                .id(Long.valueOf(indexActor.getId()))
                .name(indexActor.getName())
                .email(indexActor.getEmail())
                .website(indexActor.getWebsite())
                .build();
    }

    public SearchActor convertRestrictedIndexActor(IndexActor indexActor) {
        return SearchActor.builder()
                .id(Long.valueOf(indexActor.getId()))
                .name(indexActor.getName())
                .website(indexActor.getWebsite())
                .build();
    }


    public SearchActor convertIndexActor(IndexActor indexActor) {
        if(LoggedInUserHolder.getLoggedInUser() ==null || !LoggedInUserHolder.getLoggedInUser().isModerator()) return convertRestrictedIndexActor(indexActor);
                else
                    return convertNotRestrictedIndexActor(indexActor);

    }

    public LabeledCheckedCount convertCategoryFacet(FacetField.Count entry, List<ItemCategory> categories) {
        String code = entry.getName();
        ItemCategory category = ItemCategoryConverter.convertCategory(code);

        return convertCategoryFacet(category, entry.getCount(), categories);
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
                .candidate(Optional.ofNullable(indexConcept.getCandidate()).orElse(false))
                .types(Optional.ofNullable(indexConcept.getTypes()).orElse(List.of()).stream()
                        .map(type -> {
                            PropertyTypeId propertyType = new PropertyTypeId();
                            propertyType.setCode(type);
                            return propertyType;
                        })
                        .collect(Collectors.toList()))
                .build();
    }

    public CountedPropertyType convertPropertyTypeFacet(FacetField.Count entry, List<String> types, Map<String, PropertyType> propertyTypes) {
        String code = entry.getName();
        PropertyType propertyType = propertyTypes.get(code);
        CountedPropertyType.CountedPropertyTypeBuilder builder = CountedPropertyType.builder();
        builder.code(code).label(propertyType.getLabel());
        builder.count(entry.getCount());
        if (types == null) {
            builder.checked(false);
        } else {
            builder.checked(types.contains(code));
        }
        return builder.build();
    }

}
