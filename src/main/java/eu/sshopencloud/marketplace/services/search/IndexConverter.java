package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.conf.datetime.SolrDateTimeFormatter;
import eu.sshopencloud.marketplace.mappers.items.ItemCategoryConverter;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorExternalId;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import eu.sshopencloud.marketplace.model.items.ItemExternalId;
import eu.sshopencloud.marketplace.model.search.IndexActor;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.sources.projection.DetailedSourceView;
import eu.sshopencloud.marketplace.services.text.LineBreakConverter;
import eu.sshopencloud.marketplace.services.text.MarkdownConverter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.common.SolrInputDocument;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;

@UtilityClass
@Slf4j
public class IndexConverter {

    public static final String DOC_CONTENT_TYPE_ITEM = "item";
    public static final String DOC_CONTENT_TYPE_SOURCE = "source";


    public SolrInputDocument convertItem(Item item, int relatedItems, List<DetailedSourceView> detailedSources) {
        SolrInputDocument sid = new SolrInputDocument();

        String descriptionText = MarkdownConverter.convertMarkdownToText(item.getDescription());
        String labelText = LineBreakConverter.removeLineBreaks(item.getLabel());

        sid.addField(IndexItem.ID_FIELD, item.getId());
        sid.addField(IndexItem.DOC_CONTENT_TYPE_FIELD, DOC_CONTENT_TYPE_ITEM);
        sid.addField(IndexItem.PERSISTENT_ID_FIELD, item.getPersistentId());
        sid.addField(IndexItem.LABEL_FIELD, item.getLabel());
        sid.addField(IndexItem.LABEL_TEXT_FIELD, labelText);
        sid.addField(IndexItem.LABEL_TEXT_EN_FIELD, item.getLabel());
        sid.addField(IndexItem.DESCRIPTION_FIELD, item.getDescription());
        sid.addField(IndexItem.DESCRIPTION_TEXT_FIELD, descriptionText);
        sid.addField(IndexItem.DESCRIPTION_TEXT_EN_FIELD, descriptionText);
        sid.addField(IndexItem.CATEGORY_FIELD, ItemCategoryConverter.convertCategory(item.getCategory()));
        sid.addField(IndexItem.CONTEXT_FIELD, ItemCategoryConverter.convertCategoryForAutocompleteContext(item.getCategory()));
        sid.addField(IndexItem.STATUS_FIELD, item.getStatus().getValue());
        sid.addField(IndexItem.OWNER_FIELD, item.getInformationContributor().getUsername());
        sid.addField(IndexItem.RELATED_ITEMS_FIELD, relatedItems);

        detailedSources.stream().map(DetailedSourceView::getLabel).distinct().forEach(label -> sid.addField(IndexItem.SOURCE_FIELD, label));

        for (DetailedSourceView detailedSource : detailedSources) {
            SolrInputDocument child = new SolrInputDocument();
            child.addField(IndexItem.ID_FIELD, item.getId().toString() + "-" + detailedSource.getId().toString() + "-" +
                    detailedSource.getSourceItemId());
            child.addField(IndexItem.DOC_CONTENT_TYPE_FIELD, DOC_CONTENT_TYPE_SOURCE);
            child.addField(IndexItem.SOURCE_LABEL_FIELD, detailedSource.getLabel());
            child.addField(IndexItem.SOURCE_ITEM_ID_FIELD, detailedSource.getSourceItemId());
            child.addField(IndexItem.PERSISTENT_ID_FIELD, item.getPersistentId());
            sid.addField(IndexItem.DETAILED_SOURCE, child);
        }

        sid.addField(IndexItem.LAST_INFO_UPDATE_FIELD,
                SolrDateTimeFormatter.formatDateTime(item.getLastInfoUpdate().withZoneSameInstant(ZoneOffset.UTC)));

        for (ItemContributor itemContributor : item.getContributors()) {
            String contributor = getItemContributorName(itemContributor);
            sid.addField(IndexItem.CONTRIBUTOR_FIELD, contributor);
            sid.addField(IndexItem.CONTRIBUTOR_TEXT_FIELD, contributor);
        }

        for (ItemExternalId itemExternalId : item.getExternalIds()) {
            sid.addField(IndexItem.EXTERNAL_IDENTIFIER_FIELD, itemExternalId.getIdentifier());
        }

        for (Property property : item.getProperties()) {
            if ("keyword".equals(property.getType().getCode())) {
                String keyword = getPropertyValue(property);
                sid.addField(IndexItem.KEYWORD_TEXT_FIELD, keyword);
            }
        }

        constructDynamicProperties(item.getProperties()).forEach(
                (key, value) -> value.forEach(v -> sid.addField(IndexItem.DYNAMIC_PROPERTY.replace("*", key), v)));

        Optional.ofNullable(item.getAccessibleAt()).ifPresent(aaList -> aaList.forEach(aa -> sid.addField(IndexItem.ACCESSIBLE_AT, aa)));
        Optional.ofNullable(item.getThumbnail()).ifPresent(thumbnail -> sid.setField(IndexItem.THUMBNAIL_ID, thumbnail.getMediaId().toString()));

        return sid;
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
                    assert property.getValue() != null;
                    return property.getValue();
                case INT:
                    assert property.getValue() != null;
                    return new BigInteger(property.getValue()).toString();
                case FLOAT:
                    assert property.getValue() != null;
                    return new BigDecimal(property.getValue()).toString();
                case DATE:
                    assert property.getValue() != null;
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


    public SolrInputDocument covertConcept(Concept concept, Vocabulary vocabulary, List<PropertyType> propertyTypes) {
        SolrInputDocument sid = new SolrInputDocument();
        sid.addField(IndexConcept.ID_FIELD, vocabulary.getCode() + "-" + concept.getCode());
        sid.addField(IndexConcept.CODE_FIELD, concept.getCode());
        sid.addField(IndexConcept.VOCABULARY_CODE_FIELD, vocabulary.getCode());
        sid.addField(IndexConcept.LABEL_FIELD, concept.getLabel());
        sid.addField(IndexConcept.NOTATION_FIELD, concept.getNotation());
        sid.addField(IndexConcept.DEFINITION_FIELD, concept.getDefinition());
        sid.addField(IndexConcept.URI_FIELD, concept.getUri());
        sid.addField(IndexConcept.CANDIDATE_FIELD, concept.isCandidate());
        propertyTypes.stream().map(PropertyType::getCode).forEach(pc -> sid.addField(IndexConcept.TYPES_FIELD, pc));
        return sid;
    }

    public SolrInputDocument covertActor(Actor actor) {
        SolrInputDocument sid = new SolrInputDocument();

        sid.addField(IndexActor.ID_FIELD, actor.getId().toString());
        sid.addField(IndexActor.NAME_FIELD, actor.getName());
        sid.addField(IndexActor.EMAIL_FIELD, actor.getEmail());
        sid.addField(IndexActor.WEBSITE_FIELD, actor.getWebsite());
        actor.getExternalIds().stream().map(ActorExternalId::getIdentifier)
                .forEach(id -> sid.addField(IndexActor.EXTERNAL_IDENTIFIER_FIELD, id));

        return sid;
    }


}
