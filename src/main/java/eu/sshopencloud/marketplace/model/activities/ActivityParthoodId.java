package eu.sshopencloud.marketplace.model.activities;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ActivityParthoodId implements Serializable {

    private Long parent;

    private Long child;

}
