package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.conf.datetime.SolrDateTimeFormatter;
import eu.sshopencloud.marketplace.mappers.sources.SourceConverter;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.mappers.items.ItemCategoryConverter;
import eu.sshopencloud.marketplace.services.text.MarkdownConverter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class IndexConverter {

    public IndexItem covertItem(Item item) {
        IndexItem.IndexItemBuilder builder = IndexItem.builder();
        String descriptionText = MarkdownConverter.convertMarkdownToText(item.getDescription());
        builder.id(item.getId())
                .label(item.getLabel()).labelText(item.getLabel()).labelTextEn(item.getLabel())
                .description(item.getDescription()).descriptionText(descriptionText).descriptionTextEn(descriptionText)
                .category(ItemCategoryConverter.convertCategory(item.getCategory()))
                .source(SourceConverter.convertSource(item.getSource()));
        builder.lastInfoUpdate(SolrDateTimeFormatter.formatDateTime(item.getLastInfoUpdate().withZoneSameInstant(ZoneOffset.UTC)));
        for (Property property : item.getProperties()) {
            switch (property.getType().getCode()) {
                case "activity":
                    builder.activity(getPropertyValue(property));
                    break;
                case "keyword":
                    String keyword = getPropertyValue(property);
                    builder.keyword(keyword).keywordsText(keyword);
                    break;
            }
        }
        return builder.build();
    }

    private String getPropertyValue(Property property) {
        if (property.getConcept() != null) {
            return property.getConcept().getLabel();
        } else {
            return property.getValue();
        }
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
