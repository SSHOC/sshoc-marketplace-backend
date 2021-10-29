package eu.sshopencloud.marketplace.dto.items;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRelationCore {

    private String code;

    private String label;

    private String inverseOf;

    @Nullable
    private Integer ord;
}
