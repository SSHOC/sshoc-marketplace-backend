package eu.sshopencloud.marketplace.model.items;

import eu.sshopencloud.marketplace.model.auth.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "item_comments")
@Data
@NoArgsConstructor
public class ItemComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_comment_generator")
    @SequenceGenerator(name = "item_comment_generator", sequenceName = "item_comments_id_seq", allocationSize = 50)
    private Long id;

    @Basic
    @Column(nullable = false, length = 4096)
    private String body;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(foreignKey = @ForeignKey(name="item_comment_creator_id_fk"))
    private User creator;

    @Basic
    @Column(nullable = false)
    @CreationTimestamp
    private ZonedDateTime dateCreated;

    @Basic
    @Column(nullable = true)
    private ZonedDateTime dateLastUpdated;

}
