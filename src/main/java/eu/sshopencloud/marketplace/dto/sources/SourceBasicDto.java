package eu.sshopencloud.marketplace.dto.sources;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SourceBasicDto {

    private Long id;

    private String label;

    private String url;

    private String urlTemplate;

}
