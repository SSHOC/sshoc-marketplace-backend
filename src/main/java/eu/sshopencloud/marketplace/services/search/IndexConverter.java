package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.conf.datetime.SolrDateTimeFormatter;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.services.items.ItemCategoryConverter;
import lombok.experimental.UtilityClass;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class IndexConverter {

    public IndexItem covertItem(Item item) {
        IndexItem.IndexItemBuilder builder = IndexItem.builder();
        builder.id(item.getId())
                .name(item.getLabel()).nameText(item.getLabel()).nameTextEn(item.getLabel())
                .description(item.getDescription()).descriptionText(item.getDescription()).descriptionTextEn(item.getDescription())
                .category(ItemCategoryConverter.convertCategory(item.getCategory()));
        builder.lastInfoUpdate(SolrDateTimeFormatter.formatDateTime(item.getLastInfoUpdate().withZoneSameInstant(ZoneOffset.UTC)));
        return builder.build();
    }

    public IndexConcept covertConcept(Concept concept, Vocabulary vocabulary, List<PropertyType> proopertyTypes) {
        IndexConcept.IndexConceptBuilder builder = IndexConcept.builder();
        builder.id(vocabulary.getCode() + "-" + concept.getCode())
                .code(concept.getCode())
                .vocabularyCode(vocabulary.getCode())
                .label(concept.getLabel())
                .notation(concept.getNotation())
                .definition(concept.getDefinition()).definitionText(concept.getDefinition()).definitionTextEn(concept.getDefinition())
                .uri(concept.getUri())
                .types(proopertyTypes.stream().map(PropertyType::getCode).collect(Collectors.toList()));
        return builder.build();
    }

}
