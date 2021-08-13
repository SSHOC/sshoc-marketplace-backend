package eu.sshopencloud.marketplace.model.search;

import lombok.*;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

import javax.persistence.Id;
import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
public class IndexItemContributor implements Serializable {

   // public static final String COLLECTION_NAME = "marketplace-item-contributors";

    public static final String ITEM_FIELD = "item_s";
    public static final String ACTOR_FIELD = "actor_s";
    public static final String ACTOR_ROLE_FIELD = "role_s";
    public static final String ORDER_FIELD = "ord_s";


    // Concatenation of Persistent ID and actorID
    @Id
    private String id;

    @Indexed("item_s")
    private String item;

    @Indexed("actor_s")
    private Long actor;

    @Indexed("role_s")
    private String role;

    @Indexed("ord_s")
    private Integer ord;
}
