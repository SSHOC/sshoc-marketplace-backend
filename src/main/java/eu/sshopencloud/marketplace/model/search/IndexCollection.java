package eu.sshopencloud.marketplace.model.search;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndexCollection {

    public static final String COLLECTION_NAME = "marketplace-collections";
    public static final String ID_FIELD = "id";
    public static final String OWNER_NAME_FIELD = "owner_name";
    public static final String OWNER_ID_FIELD = "owner_id";
    public static final String TITLE_FIELD = "title";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String VISIBLE_FIELD = "visible";


    @Field(ID_FIELD)
    private String id;
    @Field(OWNER_NAME_FIELD)
    private String ownerName;
    @Field(TITLE_FIELD)
    private String title;
    @Field(DESCRIPTION_FIELD)
    private String description;
    @Field(VISIBLE_FIELD)
    private String visible;

}
