package eu.sshopencloud.marketplace.services.activities;

import eu.sshopencloud.marketplace.model.activities.Activity;
import eu.sshopencloud.marketplace.services.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
public class PaginatedActivities extends PaginatedResult {

    private List<Activity> activities;

}
