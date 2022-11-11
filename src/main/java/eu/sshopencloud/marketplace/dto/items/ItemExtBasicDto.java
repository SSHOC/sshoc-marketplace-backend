package eu.sshopencloud.marketplace.dto.items;


import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ItemExtBasicDto extends ItemBasicDto {

    private ItemStatus status;

    private UserDto informationContributor;

}
