package eu.sshopencloud.marketplace.repositories.inbox;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.inbox.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessagesRepository extends JpaRepository<Message, Long>  {

    Page<Message> findAllByRecipient(User owner, PageRequest pageRequest);

    long countMessageByRecipientAndIsReadTrue(User recipient);

}
