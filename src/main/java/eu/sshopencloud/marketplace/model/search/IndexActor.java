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

    public static final String EXTERNAL_IDENTIFIER_FIELD = "external_identifiers";

    //public static final String CONTRIBUTOR_FIELD = "contributor";
    //public static final String CONTRIBUTOR_TEXT_FIELD = "contributor_text";

    public static final String AFFILIATION_FIELD = "affiliation";
    public static final String AFFILIATION_TEXT_FIELD = "affiliation_text";


    @Id
    @Indexed
    private String id;


    @Indexed("name_s")
    private String name;

    @Indexed("website_s")
    private String website;

    @Indexed("email_s")
    private String email;

    @Indexed("external_identifier_ss")
    private List<String> externalIdentifier;


    @Indexed("index_item_contributor")
    @ChildDocument
    private List<IndexItemContributor> indexItemContributor;

    @Indexed("root_b")
    private boolean root;

    /*
    @Indexed(name = CONTRIBUTOR_TEXT_FIELD, type = "text_general_rev")
    @Singular("contributorText")
    private List<String> contributorsText;
 */

    @Indexed( "affiliation_ss")
    private List<String> affiliation;


    @Indexed( "affiliation_text_ss")
    private List<String> affiliationText;



}
