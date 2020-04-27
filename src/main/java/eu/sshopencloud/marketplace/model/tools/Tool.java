package eu.sshopencloud.marketplace.model.tools;

import eu.sshopencloud.marketplace.model.items.Item;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "tools")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Tool extends Item {

    @Basic
    @Column(nullable = true)
    private String repository;

}
