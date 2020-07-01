package eu.sshopencloud.marketplace.repositories.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
public class SuggestedSearchPhrases {
    private String phrase;
    private List<String> suggestions;
}
