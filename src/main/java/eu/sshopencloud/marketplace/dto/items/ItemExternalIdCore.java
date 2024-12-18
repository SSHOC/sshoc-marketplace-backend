package eu.sshopencloud.marketplace.dto.items;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemExternalIdCore {

    @NotNull
    private ItemExternalIdId identifierService;

    @NotNull
    private String identifier;
}
