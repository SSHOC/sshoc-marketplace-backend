package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.conf.datetime.SolrDateTimeFormatter;
import eu.sshopencloud.marketplace.mappers.sources.SourceConverter;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import eu.sshopencloud.marketplace.model.items.ItemExternalId;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.mappers.items.ItemCategoryConverter;
import eu.sshopencloud.marketplace.services.text.LineBreakConverter;
import eu.sshopencloud.marketplace.services.text.MarkdownConverter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class IndexConverter {

    public IndexItem convertItem(Item item) {
        IndexItem.IndexItemBuilder builder = IndexItem.builder();
        String descriptionText = MarkdownConverter.convertMarkdownToText(item.getDescription());
        String labelText = LineBreakConverter.removeLineBreaks(item.getLabel());
        builder.versionId(item.getId())
                .persistentId(item.getPersistentId())
                .label(item.getLabel())
                .labelText(labelText)
                .labelTextEn(item.getLabel())
                .description(item.getDescription())
                .descriptionText(descriptionText)
                .descriptionTextEn(descriptionText)
                .category(ItemCategoryConverter.convertCategory(item.getCategory()))
                .status(item.getStatus().getValue())
                .owner(item.getInformationContributor().getUsername())
                .source(SourceConverter.convertSource(item.getSource()));

        builder.lastInfoUpdate(SolrDateTimeFormatter.formatDateTime(item.getLastInfoUpdate().withZoneSameInstant(ZoneOffset.UTC)));

        for (ItemContributor itemContributor: item.getContributors()) {
            String contributor = getItemContributorName(itemContributor);
            builder.contributor(contributor).contributorText(contributor);
        }

        for (ItemExternalId itemExternalId: item.getExternalIds()) {
            builder.externalIdentifier(itemExternalId.getIdentifier());
        }

        for (Property property : item.getProperties()) {
            switch (property.getType().getCode()) {
                case "activity":
                    builder.activity(getPropertyValue(property));
                    break;
                case "keyword":
                    String keyword = getPropertyValue(property);
                    builder.keyword(keyword).keywordText(keyword);
                    break;
            }
        }

        builder.dynamicProperties(constructDynamicProperties(item.getProperties()));

        return builder.build();
    }

    private String getItemContributorName(ItemContributor itemContributor) {
        return itemContributor.getActor().getName();
    }

    private String getPropertyValue(Property property) {
        if (property.getConcept() != null) {
            return property.getConcept().getLabel();
        } else {
            PropertyType propertyType = property.getType();
            switch (propertyType.getType()) {
                case CONCEPT:
                case STRING:
                case URL:
                    return property.getValue();
                case INT:
                    return new BigInteger(property.getValue()).toString();
                case FLOAT:
                    return new BigDecimal(property.getValue()).toString();
                case DATE:
                    ZonedDateTime date;
                    try {
                        date = ZonedDateTime.parse(property.getValue(), ApiDateTimeFormatter.INPUT_DATE_FORMATTER);
                    } catch (DateTimeParseException e1) {
                        try {
                            LocalDateTime localDate = LocalDateTime.parse(property.getValue(), ApiDateTimeFormatter.INPUT_DATE_FORMATTER);
                            date = localDate.atZone(ZoneId.of("UTC"));
                        } catch (DateTimeParseException e2) {
                            LocalDate localDate = LocalDate.parse(property.getValue(), ApiDateTimeFormatter.SIMPLE_DATE_FORMATTER);
                            date = localDate.atTime(0, 0, 0, 0).atZone(ZoneId.of("UTC"));
                        }
                    }
                    return SolrDateTimeFormatter.formatDateTime(date);
            }
            return property.getValue();
        }
    }

    private static Map<String, List<String>> constructDynamicProperties(List<Property> properties) {
        Map<String, List<String>> dynamicProperties = new HashMap<>();
        for (Property property : properties) {
            PropertyType propertyType = property.getType();
            String dynamicFieldName = propertyType.getCode() + propertyType.getType().getDynamicFieldIndexTypeSuffix();
            if (!dynamicProperties.containsKey(dynamicFieldName)) {
                dynamicProperties.put(dynamicFieldName, new ArrayList<>());
            }
            List<String> dynamicPropertyValue = dynamicProperties.get(dynamicFieldName);
            dynamicPropertyValue.add(getPropertyValue(property));
        }
        return dynamicProperties;
    }


    public IndexConcept covertConcept(Concept concept, Vocabulary vocabulary, List<PropertyType> proopertyTypes) {
        IndexConcept.IndexConceptBuilder builder = IndexConcept.builder();
        builder.id(vocabulary.getCode() + "-" + concept.getCode())
                .code(concept.getCode())
                .vocabularyCode(vocabulary.getCode())
                .label(concept.getLabel())
                .notation(concept.getNotation())
                .definition(concept.getDefinition() != null ? concept.getDefinition() : "") // TODO change definition to an optional field
                .uri(concept.getUri())
                .types(proopertyTypes.stream().map(PropertyType::getCode).collect(Collectors.toList()));
        return builder.build();
    }

}
