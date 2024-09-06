package eu.sshopencloud.marketplace.dto.items;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatedItemCore {

    @NotNull
    private String persistentId;
    private ItemRelationId relation;
}
