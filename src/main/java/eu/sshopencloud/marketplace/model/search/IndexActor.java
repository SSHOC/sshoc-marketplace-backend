package eu.sshopencloud.marketplace.model.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

import java.io.Serializable;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class IndexActor implements Serializable {
    public static final String COLLECTION_NAME = "marketplace-actors";
    public static final String ID_FIELD = "id";
    public static final String NAME_FIELD = "name";
    public static final String WEBSITE_FIELD = "website";
    public static final String EMAIL_FIELD = "email";
    public static final String EXTERNAL_IDENTIFIER_FIELD = "external_identifier";

    @Field(ID_FIELD)
    private String id;
    @Field(NAME_FIELD)
    private String name;
    @Field(WEBSITE_FIELD)
    private String website;
    @Field(EMAIL_FIELD)
    private String email;
    @Field(EXTERNAL_IDENTIFIER_FIELD)
    private List<String> externalIdentifiers;

}
