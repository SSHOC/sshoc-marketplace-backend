package eu.sshopencloud.marketplace.dto.actors;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class ActorSourceDto {
    private String code;
    private String label;
    private int ord;
    private String urlTemplate;
}
