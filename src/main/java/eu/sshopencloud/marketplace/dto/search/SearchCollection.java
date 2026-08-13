package eu.sshopencloud.marketplace.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchCollection {

    private Long id;
    private String title;
    private String description;
    private String visible;
    private int itemsCount;
    private Date createdAt;
    private Date updatedAt;
}
