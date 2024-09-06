package eu.sshopencloud.marketplace.repositories.sources.projection;


import lombok.Data;

import java.io.Serializable;
import java.util.Map;

// Projection that is simultaneously extensions
@Data
public class DetailedSourceView implements Serializable {

    public static final String ID_FIELD_NAME = "id";
    public static final String LABEL_FIELD_NAME = "label";
    public static final String SOURCE_ITEM_ID_FIELD_NAME = "source_item_id";

    private Long id;
    private String label;
    private String sourceItemId;

    public DetailedSourceView(Map<String, Object> values) {
        this.id = values.get(ID_FIELD_NAME) != null ? ((Long) values.get(ID_FIELD_NAME)).longValue(): null;
        this.label = values.get(LABEL_FIELD_NAME) != null ? (String) values.get(LABEL_FIELD_NAME) : null;
        this.sourceItemId = values.get(SOURCE_ITEM_ID_FIELD_NAME) != null ? (String) values.get(SOURCE_ITEM_ID_FIELD_NAME) : null;
    }

}
