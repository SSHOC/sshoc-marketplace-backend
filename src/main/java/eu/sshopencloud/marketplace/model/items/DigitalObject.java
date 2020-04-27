package eu.sshopencloud.marketplace.model.items;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "digital_objects")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class DigitalObject extends Item {

    @Basic
    @Column(nullable = true)
    private ZonedDateTime dateCreated;

    @Basic
    @Column(nullable = true)
    private ZonedDateTime dateLastUpdated;

}
