package eu.sshopencloud.marketplace.model.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class IndexSource {

    @Field
    private String id;

    @Field(value = IndexItem.DOC_CONTENT_TYPE_FIELD)
    private String docContentType;

    @Field(value = IndexItem.SOURCE_LABEL_FIELD)
    private String sourceLabel;

    @Field(value = IndexItem.SOURCE_ITEM_ID_FIELD)
    private String sourceItemId;

}
