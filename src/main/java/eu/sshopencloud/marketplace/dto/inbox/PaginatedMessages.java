package eu.sshopencloud.marketplace.dto.inbox;

import com.fasterxml.jackson.annotation.JsonGetter;
import eu.sshopencloud.marketplace.dto.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
public class PaginatedMessages extends PaginatedResult<MessageDto> {

    private List<MessageDto> messages;

    @Override
    @JsonGetter("messages")
    public List<MessageDto> getResults() {
        return messages;
    }
}
