package eu.sshopencloud.marketplace.dto.activities;

import eu.sshopencloud.marketplace.dto.items.ItemDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ActivityDto extends ItemDto {

    private List<ActivityDto> composedOf;

    private List<ActivityBasicDto> partOf;

}
