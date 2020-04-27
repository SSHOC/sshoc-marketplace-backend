package eu.sshopencloud.marketplace.services.activities;

import eu.sshopencloud.marketplace.dto.activities.ActivityDto;
import eu.sshopencloud.marketplace.services.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
public class PaginatedActivities extends PaginatedResult {

    private List<ActivityDto> activities;

}
