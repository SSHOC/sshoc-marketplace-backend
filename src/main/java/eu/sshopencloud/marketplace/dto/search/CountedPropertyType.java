package eu.sshopencloud.marketplace.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CountedPropertyType {

    private String code;

    private String label;

    private long count;

    private boolean checked;

}
