package eu.sshopencloud.marketplace.model.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

import java.util.List;


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
    @Field(ID_FIELD)
    private String id;
    @Field(CODE_FIELD)
    private String code;
    @Field(VOCABULARY_CODE_FIELD)
    private String vocabularyCode;
    @Field(LABEL_FIELD)
    private String label;
    @Field(NOTATION_FIELD)
    private String notation;
    @Field(DEFINITION_FIELD)
    private String definition;
    @Field(DEFINITION_TEXT_FIELD)
    private String definitionText;
    @Field(DEFINITION_TEXT_EN_FIELD)
    private String definitionTextEn;
    @Field(URI_FIELD)
    private String uri;
    @Field(TYPES_FIELD)
    private List<String> types;
    @Field(CANDIDATE_FIELD)
    private Boolean candidate;
}
