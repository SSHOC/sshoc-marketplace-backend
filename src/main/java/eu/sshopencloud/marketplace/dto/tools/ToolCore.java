package eu.sshopencloud.marketplace.dto.tools;

import eu.sshopencloud.marketplace.dto.items.ItemCore;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ToolCore extends ItemCore {

    private String repository;

}
