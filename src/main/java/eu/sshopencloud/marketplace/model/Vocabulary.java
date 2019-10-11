package eu.sshopencloud.marketplace.model;

import javax.persistence.Entity;
import java.io.Serializable;

@Deprecated
@Entity
public class Vocabulary extends DigitalObject implements Serializable {

    public Vocabulary(String title) {
        super(title);
    }
}
