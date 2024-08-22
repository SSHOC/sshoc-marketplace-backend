package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeVocabulary;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeVocabularyId;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeVocabularyRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import eu.sshopencloud.marketplace.services.vocabularies.event.VocabulariesChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AllowedVocabulariesService {

    private final PropertyTypeVocabularyRepository propertyTypeVocabularyRepository;
    private final VocabularyRepository vocabularyRepository;

    private final ApplicationEventPublisher eventPublisher;


    public void updateForPropertyType(List<String> allowedVocabularies, PropertyType propertyType) {
        List<PropertyTypeVocabulary> propertyVocabularies =
                propertyTypeVocabularyRepository.findByPropertyTypeCode(propertyType.getCode());

        List<Vocabulary> oldPropertyVocabularies = propertyVocabularies.stream()
                .map(PropertyTypeVocabulary::getVocabulary)
                .collect(Collectors.toList());

        List<Vocabulary> newPropertyVocabularies = allowedVocabularies.stream()
                .map(this::loadVocabulary)
                .collect(Collectors.toList());

        vocabulariesToRemove(oldPropertyVocabularies, newPropertyVocabularies)
                .forEach(vocabulary ->
                        propertyTypeVocabularyRepository.deleteById(
                                new PropertyTypeVocabularyId(propertyType.getCode(), vocabulary.getCode())
                        )
                );

        for (String vocabularyCode : allowedVocabularies) {
            Vocabulary vocabulary = loadVocabulary(vocabularyCode);
            PropertyTypeVocabulary propertyTypeVocabulary = new PropertyTypeVocabulary(propertyType, vocabulary);

            propertyTypeVocabularyRepository.save(propertyTypeVocabulary);
        }

        List<Vocabulary> toReindex = vocabulariesToReindex(oldPropertyVocabularies, newPropertyVocabularies);
        eventPublisher.publishEvent(new VocabulariesChangedEvent(toReindex));

    }

    private Vocabulary loadVocabulary(String vocabularyCode) {
        return vocabularyRepository.findById(vocabularyCode)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Vocabulary.class.getName() + " with code " + vocabularyCode));
    }

    private List<Vocabulary> vocabulariesToReindex(List<Vocabulary> oldVocabularies, List<Vocabulary> newVocabularies) {
        List<Vocabulary> toReindex = vocabulariesToRemove(oldVocabularies, newVocabularies);
        toReindex.addAll(vocabulariesToAdd(oldVocabularies, newVocabularies));

        return toReindex;
    }

    private List<Vocabulary> vocabulariesToRemove(List<Vocabulary> oldVocabularies, List<Vocabulary> newVocabularies) {
        Set<String> newCodes = newVocabularies.stream().map(Vocabulary::getCode).collect(Collectors.toSet());
        return oldVocabularies.stream().filter(voc -> !newCodes.contains(voc.getCode())).collect(Collectors.toList());
    }

    private List<Vocabulary> vocabulariesToAdd(List<Vocabulary> oldVocabularies, List<Vocabulary> newVocabularies) {
        Set<String> oldCodes = oldVocabularies.stream().map(Vocabulary::getCode).collect(Collectors.toSet());
        return newVocabularies.stream().filter(voc -> !oldCodes.contains(voc.getCode())).collect(Collectors.toList());
    }
}
