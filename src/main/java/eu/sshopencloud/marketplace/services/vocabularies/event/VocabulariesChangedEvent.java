package eu.sshopencloud.marketplace.services.vocabularies.event;

import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


@Data
@AllArgsConstructor
public class VocabulariesChangedEvent {

    private List<Vocabulary> changedVocabularies;
}
