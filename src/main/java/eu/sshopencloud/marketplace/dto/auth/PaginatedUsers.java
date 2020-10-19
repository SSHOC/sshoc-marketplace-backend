package eu.sshopencloud.marketplace.dto.auth;

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
public class PaginatedUsers extends PaginatedResult<UserDto> {

    private List<UserDto> users;

    @Override
    @JsonGetter("users")
    public List<UserDto> getResults() {
        return users;
    }
}
