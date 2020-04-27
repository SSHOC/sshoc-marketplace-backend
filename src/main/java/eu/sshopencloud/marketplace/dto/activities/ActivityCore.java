package eu.sshopencloud.marketplace.dto.activities;

import eu.sshopencloud.marketplace.dto.items.ItemCore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ActivityCore extends ItemCore {

    private List<Long> composedOf;

}
