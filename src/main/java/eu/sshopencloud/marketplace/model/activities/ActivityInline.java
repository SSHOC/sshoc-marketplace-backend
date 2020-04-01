package eu.sshopencloud.marketplace.model.activities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityInline {

    private Long id;

    private String label;

    private String description;

}
