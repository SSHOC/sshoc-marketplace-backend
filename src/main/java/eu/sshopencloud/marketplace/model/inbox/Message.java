package eu.sshopencloud.marketplace.model.inbox;

import eu.sshopencloud.marketplace.model.auth.User;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "messages")
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_generator")
    @SequenceGenerator(name = "message_generator", sequenceName = "message_id_seq", allocationSize = 1)
    private Long id;

    private String content;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    private User recipient;

    private boolean isRead = false;
}
