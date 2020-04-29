package eu.sshopencloud.marketplace.model.sources;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.ZonedDateTime;


@Entity
@Table(name = "sources")
@Data
@NoArgsConstructor
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "source_generator")
    @SequenceGenerator(name = "item_generator", sequenceName = "items_id_seq", allocationSize = 50)
    private Long id;

    @Basic
    @Column(nullable = false)
    private String label;

    @Basic
    @Column(nullable = false)
    private String url;

    @Basic
    @Column(nullable = false)
    private String domain;

    @Basic
    @Column(nullable = true)
    private ZonedDateTime lastHarvestedDate;

}
