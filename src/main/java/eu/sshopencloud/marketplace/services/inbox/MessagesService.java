package eu.sshopencloud.marketplace.services.inbox;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.inbox.MessageDto;
import eu.sshopencloud.marketplace.dto.inbox.PaginatedMessages;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.inbox.Message;
import eu.sshopencloud.marketplace.repositories.inbox.MessagesRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MessagesService {

    private final MessagesRepository  messagesRepository;

    public PaginatedMessages getUserMessages(PageCoords pageCoords) {
        if (LoggedInUserHolder.getLoggedInUser() == null) {
            return PaginatedMessages.builder().messages(Collections.emptyList()).count(0).hits(0).page(0).perpage(0).pages(0).build();
        }
        PageRequest pageRequest = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage());
        Page<Message> messages = messagesRepository.findAllByRecipient(LoggedInUserHolder.getLoggedInUser(),
                pageRequest);
        return PaginatedMessages.builder().messages(Collections.emptyList()).count(messages.getContent().size()).hits(messages.getTotalElements()).page(pageRequest.getPageNumber()).perpage(pageRequest.getPageSize()).pages(messages.getTotalPages()).build();
    }

    public boolean hasMessagesToRead(User recipient) {
        if (LoggedInUserHolder.getLoggedInUser() == null) {
            return false;
        } else {
            return messagesRepository.countMessageByRecipientAndIsReadTrue(recipient) > 0;
        }
    }

    public void sendMessageToUser(MessageDto messageDto, User recipient) {
        Message message = new Message();
        message.setRecipient(recipient);
        message.setRead(false);
        message.setContent(messageDto.getContent());
        messagesRepository.save(message);
    }
}
