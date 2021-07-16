package eu.sshopencloud.marketplace.validators.items;

import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.dto.items.ItemMediaCore;
import eu.sshopencloud.marketplace.dto.items.MediaDetailsId;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemMedia;
import eu.sshopencloud.marketplace.model.items.ItemMediaType;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.validators.vocabularies.ConceptFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.*;


@Component
@RequiredArgsConstructor
public class ItemMediaFactory {

    private static final String LICENSE_PROPERTY_CODE = "license";

    private final MediaStorageService mediaStorageService;

    private final ConceptFactory conceptFactory;

    private final PropertyTypeService propertyTypeService;

    public List<ItemMedia> create(List<ItemMediaCore> itemMedia, Item item, Errors errors) {
        List<ItemMedia> newMedia = new ArrayList<>();
        Set<UUID> processedMediaIds = new HashSet<>();

        if (itemMedia == null)
            return newMedia;

        for (int i = 0; i < itemMedia.size(); ++i) {
            String nestedPath = String.format("media[%d]", i);
            errors.pushNestedPath(nestedPath);

            ItemMediaCore mediaCore = itemMedia.get(i);
            MediaDetailsId info = mediaCore.getInfo();
            UUID mediaId = info != null ? info.getMediaId() : null;

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

            Concept licenseConcept = null;
            if (Objects.nonNull(mediaCore.getConcept())) {
                errors.pushNestedPath("concept");
                licenseConcept = createLicenseConcept(mediaCore.getConcept(), errors);
                if (licenseConcept == null) {
                    errors.popNestedPath();
                    errors.popNestedPath();
                    continue;
                }
                errors.popNestedPath();
            }

            if (mediaStorageService.ensureMediaAvailable(mediaId)) {
                if (licenseConcept == null) newMedia.add(new ItemMedia(item, mediaId, mediaCore.getCaption()));
                else newMedia.add(new ItemMedia(item, mediaId, mediaCore.getCaption(), licenseConcept));
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

        Concept licenseConcept = null;
        if (Objects.nonNull(concept)) {
            errors.pushNestedPath("concept");
            licenseConcept = createLicenseConcept(concept, errors);
            errors.popNestedPath();
        }

        if (mediaStorageService.ensureMediaAvailable(itemMediaId)) {
            if (licenseConcept == null) return new ItemMedia(item, itemMediaId, caption, itemThumbnail);
            else return new ItemMedia(item, itemMediaId, caption, itemThumbnail, licenseConcept);
        } else {
            errors.pushNestedPath("info");
            errors.rejectValue("mediaId", "field.notExist", String.format("Media with id %s is not available", itemMediaId));
            errors.popNestedPath();
            return null;
        }
    }


    private Concept createLicenseConcept(ConceptId concept, Errors errors) {
        PropertyType licensePropertyType = propertyTypeService.loadPropertyTypeOrNull(LICENSE_PROPERTY_CODE);
        if (licensePropertyType != null) {
            List<Vocabulary> allowedVocabularies = propertyTypeService.getAllowedVocabulariesForPropertyType(licensePropertyType);
            return conceptFactory.create(concept, licensePropertyType, allowedVocabularies, errors);
        } else {
            errors.rejectValue("concept ", "field.configError", String.format("No '%s' property defined in the system. Only licenses from allowed vocabularies for this property are acceptable", LICENSE_PROPERTY_CODE));
            return null;
        }
    }

}
