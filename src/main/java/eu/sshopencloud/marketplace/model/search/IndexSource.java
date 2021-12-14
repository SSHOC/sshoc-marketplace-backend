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

    @Field(value = "source_label")
    private String sourceLabel;

    @Field(value = "source_item_id")
    private String sourceItemId;

}
