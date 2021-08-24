package eu.sshopencloud.marketplace.model.sources;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.time.ZonedDateTime;


@Entity
@Table(name = "sources")
@Data
@NoArgsConstructor
@Nullable
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "source_generator")
    @GenericGenerator(
            name = "source_generator",
            strategy = "eu.sshopencloud.marketplace.conf.jpa.KnownIdOrSequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "sources_id_seq"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "50")
            }
    )
    private Long id;

    @Basic
    @Column(nullable = false)
    private String label;

    @Basic
    @Column(nullable = false)
    private String url;

    @Basic
    @Column(nullable = false)
    private String urlTemplate;

    @Basic
    @Column(nullable = false)
    private String domain;

    @Basic
    @Column(nullable = true)
    @Nullable
    private ZonedDateTime lastHarvestedDate;

}
