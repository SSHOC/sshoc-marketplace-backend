package eu.sshopencloud.marketplace.model.search;

import lombok.*;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Dynamic;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.util.List;
import java.util.Map;


@SolrDocument(collection = IndexItem.COLLECTION_NAME)
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class IndexItem {
    public static final String COLLECTION_NAME = "marketplace-items";

    public static final String ID_FIELD = "id";
    public static final String PERSISTENT_ID_FIELD = "persistent_id";

    public static final String LABEL_FIELD = "label";
    public static final String LABEL_TEXT_FIELD = "label_text";
    public static final String LABEL_TEXT_EN_FIELD = "label_text_en";

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

    public static final String ACTIVITY_FIELD = "activity";

    public static final String SOURCE_FIELD = "source";

    public static final String KEYWORD_FIELD = "keyword";
    public static final String KEYWORD_TEXT_FIELD = "keyword_text";


    @Id
    @Indexed(name = ID_FIELD, type = "string")
    private Long versionId;

    @Indexed(name = PERSISTENT_ID_FIELD, type = "string")
    private String persistentId;

    @Indexed(name = LABEL_FIELD, type = "string")
    private String label;

    @Indexed(name = LABEL_TEXT_FIELD, type = "text_general_rev")
    private String labelText;

    @Indexed(name = LABEL_TEXT_EN_FIELD, type = "text_en")
    private String labelTextEn;

    @Indexed(name = DESCRIPTION_FIELD, type = "string")
    private String description;

    @Indexed(name = DESCRIPTION_TEXT_FIELD, type = "text_general_rev")
    private String descriptionText;

    @Indexed(name = DESCRIPTION_TEXT_EN_FIELD, type = "text_en")
    private String descriptionTextEn;

    @Indexed(name = CONTRIBUTOR_FIELD, type = "strings")
    @Singular
    private List<String> contributors;

    @Indexed(name = CONTRIBUTOR_TEXT_FIELD, type = "text_general_rev")
    @Singular("contributorText")
    private List<String> contributorsText;

    @Indexed(name = EXTERNAL_IDENTIFIER_FIELD, type = "strings")
    @Singular
    private List<String> externalIdentifiers;

    @Indexed(name = CATEGORY_FIELD, type = "string")
    private String category;

    @Indexed(name = CONTEXT_FIELD, type = "text_context")
    private String context;

    @Indexed(name = STATUS_FIELD, type = "string")
    private String status;

    @Indexed(name = OWNER_FIELD, type = "string")
    private String owner;

    @Indexed(name = LAST_INFO_UPDATE_FIELD, type = "pdate")
    private String lastInfoUpdate;

    @Indexed(name = ACTIVITY_FIELD, type = "strings")
    @Singular
    private List<String> activities;

    @Indexed(name = SOURCE_FIELD, type = "string")
    private String source;

    @Indexed(name = KEYWORD_FIELD, type = "strings")
    @Singular
    private List<String> keywords;

    @Indexed(name = KEYWORD_TEXT_FIELD, type = "text_general_rev")
    @Singular("keywordText")
    private  List<String> keywordsTexts;

    @Dynamic
    @Field("dynamic_property_*")
    private Map<String, List<String>> dynamicProperties;

}