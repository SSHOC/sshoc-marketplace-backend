package eu.sshopencloud.marketplace.model.search;

import lombok.*;
import org.apache.solr.client.solrj.beans.Field;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class IndexItem {
    public static final String COLLECTION_NAME = "marketplace-items";

    public static final String ID_FIELD = "id";

    public static final String DOC_CONTENT_TYPE_FIELD = "doc_content_type";

    public static final String PERSISTENT_ID_FIELD = "persistent_id";

    public static final String LABEL_FIELD = "label";
    public static final String LABEL_TEXT_FIELD = "label_text";
    public static final String LABEL_TEXT_EN_FIELD = "label_text_en";

    public static final String VERSION_FIELD = "version";

    public static final String DESCRIPTION_FIELD = "description";
    public static final String DESCRIPTION_TEXT_FIELD = "description_text";
    public static final String DESCRIPTION_TEXT_EN_FIELD = "description_text_en";

    public static final String CONTRIBUTOR_FIELD = "contributor";
    public static final String CONTRIBUTOR_TEXT_FIELD = "contributor_text";

    public static final String EXTERNAL_IDENTIFIER_FIELD = "external_identifier";

    public static final String CATEGORY_FIELD = "category";
    public static final String STATUS_FIELD = "status";
    public static final String CONTEXT_FIELD = "context";

    public static final String OWNER_FIELD = "owner";

    public static final String LAST_INFO_UPDATE_FIELD = "modified_on";

    public static final String SOURCE_FIELD = "source";

    public static final String SOURCE_LABEL_FIELD = "source_label";
    public static final String SOURCE_ITEM_ID_FIELD = "source_item_id";

    public static final String KEYWORD_TEXT_FIELD = "keyword_text";

    public static final String RELATED_ITEMS_FIELD = "related_items";

    public static final String FACETING_DYNAMIC_PROPERTY_KEYWORD_TAG = "keyword";
    public static final String FACETING_DYNAMIC_PROPERTY_ACTIVITY_TAG = "activity";
    public static final String FACETING_DYNAMIC_PROPERTY_LANGUAGE_TAG = "language";
    public static final Map<String, String> DYNAMIC_PROPERTIES_FACET_FIELDS = Map.ofEntries(
            entry(FACETING_DYNAMIC_PROPERTY_KEYWORD_TAG, "dynamic_property_keyword_ss"),
            entry(FACETING_DYNAMIC_PROPERTY_ACTIVITY_TAG, "dynamic_property_activity_ss"),
            entry(FACETING_DYNAMIC_PROPERTY_LANGUAGE_TAG, "dynamic_property_language_ss"));
    public static final String DETAILED_SOURCE = "detailed_source";
    public static final String DYNAMIC_PROPERTY = "dynamic_property_*";
    public static final String ACCESSIBLE_AT = "accessible_at";
    public static final String THUMBNAIL_ID = "thumbnail_id";

    @Field(ID_FIELD)
    private String versionId;

    @Field(DOC_CONTENT_TYPE_FIELD)
    private String docContentType;

    @Field(PERSISTENT_ID_FIELD)
    private String persistentId;

    @Field(LABEL_FIELD)
    private String label;

    @Field(LABEL_TEXT_FIELD)
    private String labelText;

    @Field(LABEL_TEXT_EN_FIELD)
    private String labelTextEn;

    @Field(VERSION_FIELD)
    private String version;

    @Field(DESCRIPTION_FIELD)
    private String description;

    @Field(DESCRIPTION_TEXT_FIELD)
    private String descriptionText;

    @Field(DESCRIPTION_TEXT_EN_FIELD)
    private String descriptionTextEn;

    @Field(CONTRIBUTOR_FIELD)
    private List<String> contributors;

    @Field(CONTRIBUTOR_TEXT_FIELD)
    private List<String> contributorsText;

    @Field(EXTERNAL_IDENTIFIER_FIELD)
    private List<String> externalIdentifiers;

    @Field(CATEGORY_FIELD)
    private String category;

    @Field(CONTEXT_FIELD)
    private String context;

    @Field(STATUS_FIELD)
    private String status;

    @Field(OWNER_FIELD)
    private String owner;

    @Field(LAST_INFO_UPDATE_FIELD)
    private Date lastInfoUpdate;

    @Field(SOURCE_FIELD)
    @Singular
    private List<String> sources;

    @Field(value = DETAILED_SOURCE, child = true)
    @Singular
    private List<IndexSource> detailedSources;

    @Field(KEYWORD_TEXT_FIELD)
    private List<String> keywordsTexts;

    @Field(RELATED_ITEMS_FIELD)
    private int relatedItems;

    @Field(DYNAMIC_PROPERTY)
    private Map<String, List<String>> dynamicProperties;

    @Field(ACCESSIBLE_AT)
    private List<String> accessibleAt;

    @Field(THUMBNAIL_ID)
    private String thumbnailId;
}