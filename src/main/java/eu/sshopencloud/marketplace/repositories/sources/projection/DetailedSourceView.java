package eu.sshopencloud.marketplace.repositories.sources.projection;


import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

// Projection that is simultaneously extensions
@Data
public class DetailedSourceView implements Serializable {

    public static final String ID_FIELD_NAME = "id";
    //public static final String DOMAIN_FIELD_NAME = "domain";
    public static final String LABEL_FIELD_NAME = "label";
    //public static final String LAST_HARVESTED_DATE_FIELD_NAME = "last_harvested_date";
    //public static final String URL_FIELD_NAME = "url";
    //public static final String URL_TEMPLATE_FIELD_NAME = "url_template";
    public static final String SOURCE_ITEM_ID_FIELD_NAME = "source_item_id";

    private Long id;
    //private String domain;
    private String label;
    //private ZonedDateTime lastHarvestedDate;
    //private String url;
    //private String urlTemplate;
    private String sourceItemId;

    public DetailedSourceView(Map<String, Object> values) {
        this.id = values.get(ID_FIELD_NAME) != null ? ((BigInteger) values.get(ID_FIELD_NAME)).longValue(): null;
        //this.domain = values.get(DOMAIN_FIELD_NAME) != null ? (String) values.get(DOMAIN_FIELD_NAME) : null;
        this.label = values.get(LABEL_FIELD_NAME) != null ? (String) values.get(LABEL_FIELD_NAME) : null;
        //this.lastHarvestedDate = values.get(LAST_HARVESTED_DATE_FIELD_NAME) != null ? ((Timestamp) values.get(LAST_HARVESTED_DATE_FIELD_NAME)).toInstant().atZone(ZoneId.systemDefault()) : null;
        //this.url = values.get(URL_FIELD_NAME) != null ? (String) values.get(URL_FIELD_NAME) : null;
        //this.urlTemplate = values.get(URL_TEMPLATE_FIELD_NAME) != null ? (String) values.get(URL_TEMPLATE_FIELD_NAME) : null;
        this.sourceItemId = values.get(SOURCE_ITEM_ID_FIELD_NAME) != null ? (String) values.get(SOURCE_ITEM_ID_FIELD_NAME) : null;
    }

}
