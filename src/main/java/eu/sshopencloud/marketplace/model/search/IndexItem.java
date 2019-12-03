package eu.sshopencloud.marketplace.model.search;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

@SolrDocument(collection = IndexItem.COLLECTION_NAME)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndexItem {

    public static final String COLLECTION_NAME = "marketplace-items";

    public static final String ID_FIELD = "id";

    public static final String LABEL_FIELD = "label";
    public static final String LABEL_TEXT_FIELD = "label_text";
    public static final String LABEL_TEXT_EN_FIELD = "label_text_en";

    public static final String DESCRIPTION_FIELD = "description";
    public static final String DESCRIPTION_TEXT_FIELD = "description_text";
    public static final String DESCRIPTION_TEXT_EN_FIELD = "description_text_en";

    public static final String CATEGORY_FIELD = "category";

    public static final String LAST_INFO_UPDATE_FIELD = "modified_on";


    @Id
    @Indexed(name = ID_FIELD, type = "string")
    private Long id;

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

    @Indexed(name = CATEGORY_FIELD, type = "string")
    private String category;

    @Indexed(name = LAST_INFO_UPDATE_FIELD, type = "pdate")
    private String lastInfoUpdate;

}