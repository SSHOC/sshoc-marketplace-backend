package eu.sshopencloud.marketplace.dto.search;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchItem {

    private Long id;

    private String name;

    private String description;

    private ItemCategory category;

}
