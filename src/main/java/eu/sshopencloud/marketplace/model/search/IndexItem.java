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

    @Id
    @Indexed(name = "id", type = "string")
    private Long id;

    @Indexed(name = "name", type = "string")
    private String name;

    @Indexed(name = "description", type = "string")
    private String description;

    @Indexed(name = "category", type = "string")
    private String category;

    @Indexed(name = "modified_on", type = "pdate")
    private String lastInfoUpdate;

}