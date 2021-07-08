package eu.sshopencloud.marketplace.validators.items;

import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.dto.items.ItemMediaCore;
import eu.sshopencloud.marketplace.dto.items.MediaDetailsId;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemMedia;
import eu.sshopencloud.marketplace.model.items.ItemMediaType;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.*;


@Component
@RequiredArgsConstructor
public class ItemMediaFactory {

    private final MediaStorageService mediaStorageService;

    private final ConceptService conceptService;

    public List<ItemMedia> create(List<ItemMediaCore> itemMedia, Item item, Errors errors) {
        List<ItemMedia> newMedia = new ArrayList<>();
        Set<UUID> processedMediaIds = new HashSet<>();

        //Eliza - here validation ??
        if (itemMedia == null)
            return newMedia;

        for (int i = 0; i < itemMedia.size(); ++i) {
            String nestedPath = String.format("media[%d]", i);
            errors.pushNestedPath(nestedPath);

            ItemMediaCore mediaCore = itemMedia.get(i);
            MediaDetailsId info = mediaCore.getInfo();
            UUID mediaId = info != null ? info.getMediaId() : null;
            ConceptId concept = mediaCore.getConcept();

            if (mediaId == null) {
                errors.pushNestedPath("info");
                errors.rejectValue(
                        "mediaId", "field.required", "The field mediaId is required"
                );
                errors.popNestedPath();
                errors.popNestedPath();
                continue;
            }

            if (processedMediaIds.contains(mediaId)) {
                errors.pushNestedPath("info");
                errors.rejectValue(
                        "mediaId", "field.duplicateEntry",
                        String.format("Duplicate item media with id: %s", mediaCore.getInfo().getMediaId())
                );
                errors.popNestedPath();
                errors.popNestedPath();
                continue;
            }

            processedMediaIds.add(mediaId);

            Concept conceptTmp = null;

            if (!Objects.isNull(concept)) {
                if (!Objects.isNull(concept.getUri())) {

                    conceptTmp = conceptService.getConceptByUri(concept.getUri());

                    if (Objects.isNull(conceptTmp)) {
                        errors.pushNestedPath("info");
                        errors.rejectValue("concept.URI ", "field.notExist", String.format("Concept with URI %s is not available", concept.getUri()));
                        errors.popNestedPath();
                    }

                } else {
                    if (!Objects.isNull(concept.getCode()) && !Objects.isNull(concept.getVocabulary().getCode())) {

                        conceptTmp = conceptService.getConceptByCodeAndVocabularyCode(concept.getCode(), concept.getVocabulary().getCode());

                        if (Objects.isNull(conceptTmp)) {
                            errors.pushNestedPath("info");
                            errors.rejectValue("concept ", "field.notExist", String.format("Concept with code %s and vocabulary code %s is not available", concept.getCode(), concept.getVocabulary().getCode()));
                            errors.popNestedPath();
                        }
                    }
                }

                //validate if vocabulary code is in enum - list of licences

                if (!conceptTmp.getVocabulary().getCode().equals("software-license")) {
                    errors.pushNestedPath("info");
                    errors.rejectValue("concept ", "field.notAppropriate", String.format("Concept with vocabulary code %s not available for license", concept.getVocabulary().getCode()));
                    errors.popNestedPath();
                }


            }


            if (mediaStorageService.ensureMediaAvailable(mediaId)) {
                newMedia.add(new ItemMedia(item, mediaId, mediaCore.getCaption()));
            } else {
                errors.pushNestedPath("info");
                errors.rejectValue("mediaId", "field.notExist", String.format("Media with id %s is not available", mediaId));
                errors.popNestedPath();
            }
            errors.popNestedPath();
        }

        return newMedia;
    }

    public ItemMedia create(UUID itemMediaId, Item item, Errors errors, ItemMediaType itemThumbnail, String caption, ConceptId concept) {

        if (itemMediaId == null) {
            errors.pushNestedPath("info");
            errors.rejectValue(
                    "mediaId", "field.required", "The field mediaId is required"
            );
            errors.popNestedPath();
            return null;
        }

        Concept concept1 = null;

        if (!Objects.isNull(concept)) {
            if (!concept.getUri().isEmpty()) {

                concept1 = conceptService.getConceptByUri(concept.getUri());

                if (concept1.equals(null)) {
                    errors.pushNestedPath("info");
                    errors.rejectValue("concept.URI ", "field.notExist", String.format("Concept with URI %s is not available", concept.getUri()));
                    errors.popNestedPath();
                }
            } else {
                if (!concept.getCode().isEmpty() && !concept.getVocabulary().getCode().isEmpty()) {
                    concept1 = conceptService.getConceptByCodeAndVocabularyCode(concept.getCode(), concept.getVocabulary().getCode());

                    if (concept1.equals(null)) {
                        errors.pushNestedPath("info");
                        errors.rejectValue("concept ", "field.notExist", String.format("Concept with code %s and vocabulary code %s is not available", concept.getCode(), concept.getVocabulary().getCode()));
                        errors.popNestedPath();
                    }
                    if (!concept1.getVocabulary().getCode().equals("software-license")) {
                        errors.pushNestedPath("info");
                        errors.rejectValue("concept ", "field.notAppropriate", String.format("Concept with vocabulary code %s not available for license", concept.getVocabulary().getCode()));
                        errors.popNestedPath();
                    }
                }

            }
        }
        //idea - create enum with vocabulary code available for license

        if (mediaStorageService.ensureMediaAvailable(itemMediaId)) {
            if (concept1 == null) return new ItemMedia(item, itemMediaId, caption, itemThumbnail);
            else return new ItemMedia(item, itemMediaId, caption, itemThumbnail, concept1);
        } else {
            errors.pushNestedPath("info");
            errors.rejectValue("mediaId", "field.notExist", String.format("Media with id %s is not available", itemMediaId));
            errors.popNestedPath();
            return null;
        }
    }

}
