package eu.sshopencloud.marketplace.validators.items;

import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.domain.media.exception.MediaNotAvailableException;
import eu.sshopencloud.marketplace.dto.items.ItemMediaCore;
import eu.sshopencloud.marketplace.dto.items.MediaDetailsId;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemMedia;
import eu.sshopencloud.marketplace.model.items.ItemMediaType;
import liquibase.pro.packaged.U;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.*;


@Component
@RequiredArgsConstructor
public class ItemMediaFactory {

    private final MediaStorageService mediaStorageService;


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

            if (mediaStorageService.ensureMediaAvailable(mediaId)) {
                newMedia.add(new ItemMedia(item, mediaId, mediaCore.getCaption()));
            }
            else {
                errors.pushNestedPath("info");
                errors.rejectValue("mediaId", "field.notExist", String.format("Media with id %s is not available", mediaId));
                errors.popNestedPath();
            }
            errors.popNestedPath();
        }

        return newMedia;
    }

    public ItemMedia create(UUID itemMedia, Item item, Errors errors, ItemMediaType itemThumbnail) {

        if (itemMedia == null) {
            errors.pushNestedPath("info");
            errors.rejectValue(
                    "mediaId", "field.required", "The field mediaId is required"
            );
            errors.popNestedPath();
            return null;
        }

        if (mediaStorageService.ensureMediaAvailable(itemMedia)) {
            return new ItemMedia(item, itemMedia, "Thumbnail", itemThumbnail);
        } else {
            errors.pushNestedPath("info");
            errors.rejectValue("mediaId", "field.notExist", String.format("Media with id %s is not available", itemMedia));
            errors.popNestedPath();
            return null;
        }
    }
}
