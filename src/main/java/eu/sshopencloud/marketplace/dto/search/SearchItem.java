package eu.sshopencloud.marketplace.dto.search;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchItem {

    private Long id;

    private String label;

    private String description;

    private List<ItemContributor> contributors;

    private List<Property> properties;

    private ItemCategory category;

}
