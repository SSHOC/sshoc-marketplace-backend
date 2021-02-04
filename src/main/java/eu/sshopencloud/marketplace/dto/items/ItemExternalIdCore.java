package eu.sshopencloud.marketplace.dto.items;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemExternalIdCore {

    @NotNull
    private String serviceIdentifier;

    @NotNull
    private String identifier;
}
