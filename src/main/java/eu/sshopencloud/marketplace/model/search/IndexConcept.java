package eu.sshopencloud.marketplace.model.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.util.List;

@SolrDocument(collection = IndexConcept.COLLECTION_NAME)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndexConcept {

    public static final String COLLECTION_NAME = "marketplace-concepts";

    public static final String ID_FIELD = "id";

    public static final String CODE_FIELD = "code";

    public static final String VOCABULARY_CODE_FIELD = "vocabulary_code";

    public static final String LABEL_FIELD = "label";
    public static final String LABEL_TEXT_FIELD = "label_text";
    public static final String LABEL_TEXT_EN_FIELD = "label_text_en";

    public static final String NOTATION_FIELD = "notation";

    public static final String DEFINITION_FIELD = "definition";
    public static final String DEFINITION_TEXT_FIELD = "definition_text";
    public static final String DEFINITION_TEXT_EN_FIELD = "definition_text_en";

    public static final String URI_FIELD = "uri";

    public static final String TYPES_FIELD = "types";

    public static final String CANDIDATE_FIELD = "candidate";


    // Concatenation of vocabularyCode and code with dash separator
    @Id
    @Indexed(name = ID_FIELD, type = "string")
    private String id;

    @Indexed(name = CODE_FIELD, type = "string")
    private String code;

    @Indexed(name = VOCABULARY_CODE_FIELD, type = "string")
    private String vocabularyCode;

    @Indexed(name = LABEL_FIELD, type = "string")
    private String label;

    @Indexed(name = NOTATION_FIELD, type = "string")
    private String notation;

    @Indexed(name = DEFINITION_FIELD, type = "string")
    private String definition;

    @Indexed(name = DEFINITION_TEXT_FIELD, type = "text_general_rev")
    private String definitionText;

    @Indexed(name = DEFINITION_TEXT_EN_FIELD, type = "text_en")
    private String definitionTextEn;

    @Indexed(name = URI_FIELD, type = "string")
    private String uri;

    @Indexed(name = CANDIDATE_FIELD, type = "boolean")
    private Boolean candidate;

    @Indexed(name = TYPES_FIELD, type = "strings")
    private List<String> types;

}
