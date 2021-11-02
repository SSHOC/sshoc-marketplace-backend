package eu.sshopencloud.marketplace.model.sources;

import lombok.Data;
import lombok.NoArgsConstructor;
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
    @SequenceGenerator(name = "source_generator", sequenceName = "sources_id_seq", allocationSize = 50)
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
