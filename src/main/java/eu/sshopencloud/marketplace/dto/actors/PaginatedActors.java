package eu.sshopencloud.marketplace.dto.actors;

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
public class PaginatedActors extends PaginatedResult<ActorDto> {

    private List<ActorDto> actors;

    @Override
    @JsonGetter("actors")
    public List<ActorDto> getResults() {
        return actors;
    }
}
