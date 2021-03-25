package eu.sshopencloud.marketplace.validators.items;

import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.domain.media.exception.MediaNotAvailableException;
import eu.sshopencloud.marketplace.dto.items.ItemMediaCore;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemMedia;
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
            UUID mediaId = mediaCore.getMediaId();

            if (processedMediaIds.contains(mediaId)) {
                errors.popNestedPath();
                errors.rejectValue(
                        nestedPath, "field.duplicateEntry",
                        String.format("Duplicate item media with id: %s", mediaCore.getMediaId())
                );
                continue;
            }

            processedMediaIds.add(mediaId);
            errors.popNestedPath();

            if (mediaStorageService.ensureMediaAvailable(mediaId)) {
                newMedia.add(new ItemMedia(item, mediaId, mediaCore.getCaption()));
            }
            else {
                errors.rejectValue(nestedPath, "field.notExist", String.format("Media with id %s not available", mediaId));
            }
        }

        return newMedia;
    }
}
