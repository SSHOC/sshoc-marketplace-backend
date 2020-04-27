package eu.sshopencloud.marketplace.model.activities;

import eu.sshopencloud.marketplace.dto.activities.ActivityBasicDto;
import eu.sshopencloud.marketplace.model.items.Item;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;


@Entity
@Table(name = "activities")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Activity extends Item {

    @Transient
    private List<Activity> composedOf;

    @Transient
    private List<ActivityBasicDto> partOf;

}
