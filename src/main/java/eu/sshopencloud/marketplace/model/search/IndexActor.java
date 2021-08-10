package eu.sshopencloud.marketplace.model.search;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.util.List;

@SolrDocument(collection = IndexActor.COLLECTION_NAME)
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class IndexActor {
    public static final String COLLECTION_NAME = "marketplace-actors";

    public static final String ID_FIELD = "id";

    public static final String NAME_FIELD = "name";

    public static final String WEBSITE_FIELD = "website";

    public static final String EMAIL_FIELD = "email";

    public static final String EXTERNAL_IDENTIFIER_FIELD = "external_identifier";

    public static final String CONTRIBUTOR_FIELD = "contributor";
    public static final String CONTRIBUTOR_TEXT_FIELD = "contributor_text";

    public static final String AFFILIATION_FIELD = "affiliation";
    public static final String AFFILIATION_TEXT_FIELD = "affiliation_text";


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
    @Singular
    private List<String> externalIdentifiers;

    @Indexed(name = CONTRIBUTOR_FIELD, type = "strings")
    @Singular
    private List<String> contributors;

    @Indexed(name = CONTRIBUTOR_TEXT_FIELD, type = "text_general_rev")
    @Singular("contributorText")
    private List<String> contributorsText;

    @Indexed(name = AFFILIATION_FIELD, type = "strings")
    @Singular
    private List<String> affiliations;

    @Indexed(name = AFFILIATION_TEXT_FIELD, type = "text_general_rev")
    @Singular("affiliationText")
    private List<String> affiliationsText;

}
