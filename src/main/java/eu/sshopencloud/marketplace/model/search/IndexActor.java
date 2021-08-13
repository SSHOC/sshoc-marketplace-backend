package eu.sshopencloud.marketplace.model.search;

import lombok.*;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.ChildDocument;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@SolrDocument(collection = IndexActor.COLLECTION_NAME)
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

    @Id
    @Indexed(name = ID_FIELD, type = "string")
    private String id;

    @Indexed(name = NAME_FIELD, type = "string")
    private String name;

    @Indexed(name = WEBSITE_FIELD, type = "string")
    private String website;

    @Indexed(name = EMAIL_FIELD, type = "string")
    private String email;

    @Indexed(name = EXTERNAL_IDENTIFIER_FIELD, type = "strings")
    private List<String> externalIdentifier;

    /*
    @ChildDocument
    private List<IndexActorExternalId> externalIdentifier;

    @ChildDocument
    private List<IndexItemContributor> indexItemContributor;

    @Indexed("root_b")
    private boolean root;

    @ChildDocument
    private List<IndexActor> affiliation;

*/

}
