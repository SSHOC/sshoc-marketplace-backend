package eu.sshopencloud.marketplace.dto.tools;

import eu.sshopencloud.marketplace.dto.items.ItemCore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ToolCore extends ItemCore {

    private String repository;

}
