package eu.sshopencloud.marketplace.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class SuggestedSearchPhrases {
    private String phrase;
    private List<SuggestedObject> suggestions;
}
