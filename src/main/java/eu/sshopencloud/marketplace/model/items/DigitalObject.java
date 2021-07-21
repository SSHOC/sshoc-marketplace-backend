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

    @Column(nullable = true)
    private ZonedDateTime dateCreated;

    @Column(nullable = true)
    private ZonedDateTime dateLastUpdated;


    public DigitalObject(DigitalObject baseDigitalObject) {
        super(baseDigitalObject);

        this.dateCreated = baseDigitalObject.getDateCreated();
        this.dateLastUpdated = baseDigitalObject.getDateLastUpdated();
    }

}
    /*
    public DigitalObject(ZonedDateTime dateLastUpdated, ZonedDateTime dateCreated) {
        super(baseDigitalObject);

        this.dateCreated = dateLastUpdated;
        this.dateLastUpdated = dateCreated;
    }*?
}*/
