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
    public static final String NAME_FIELD = "name_s";
    public static final String WEBSITE_FIELD = "website_s";
    public static final String EMAIL_FIELD = "email_s";
    public static final String EXTERNAL_IDENTIFIER_FIELD = "external_id_identifier_ss";

    @Id
    @Indexed(ID_FIELD)
    private String id;

    @Indexed(NAME_FIELD)
    private String name;

    @Indexed(WEBSITE_FIELD)
    private String website;

    @Indexed(EMAIL_FIELD)
    private String email;

    @Indexed(EXTERNAL_IDENTIFIER_FIELD)
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
