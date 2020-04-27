package eu.sshopencloud.marketplace.dto.activities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityBasicDto {

    private Long id;

    private String label;

    private String description;

}
