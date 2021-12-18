package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.DigitalObjectDto;
import eu.sshopencloud.marketplace.dto.items.ItemDifferencesCore;
import eu.sshopencloud.marketplace.dto.items.ItemDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemsConflictComparator {

    public boolean isConflict(ItemDifferencesCore differences1, ItemDifferencesCore differences2) {
        if (isConflictOnCategories(differences1.getOther(), differences2.getOther())) {
            return true;
        }
        if (isConflictOnLabels(differences1.getOther(), differences2.getOther())) {
            return true;
        }
        if (isConflictOnVersions(differences1.getOther(), differences2.getOther())) {
            return true;
        }
        if (isConflictOnDescriptions(differences1.getOther(), differences2.getOther())) {
            return true;
        }
        if (isConflictOnContributors(differences1.getOther(), differences2.getOther())) {
            return true;
        }
        if (isConflictOnAccessibleAt(differences1.getOther(), differences2.getOther())) {
            return true;
        }
        if (isConflictOnExternalIds(differences1.getOther(), differences2.getOther())) {
            return true;
        }
        if (isConflictOnProperties(differences1.getOther(), differences2.getOther())) {
            return true;
        }
        if (isConflictOnRelatedItems(differences1.getOther(), differences2.getOther())) {
            return true;
        }
        if (isConflictOnMedia(differences1.getOther(), differences2.getOther())) {
            return true;
        }
        if (isConflictOnDigitalObjectDates(differences1.getOther(), differences2.getOther())) {
            return true;
        }
        return false;
    }

    private boolean isConflictOnCategories(ItemDto item1, ItemDto item2) {
        if (!item1.getCategory().equals(item2.getCategory()))
            return true;
        else
            return false;
    }

    private boolean isConflictOnLabels(ItemDto item1, ItemDto item2) {
        if (item1.getLabel() != null && item2.getLabel() != null && !item1.getLabel().equals(item2.getLabel()))
            return true;
        else
            return false;
    }

    private boolean isConflictOnVersions(ItemDto item1, ItemDto item2) {
        if (item1.getVersion() != null && item2.getVersion() != null && !item1.getVersion().equals(item2.getVersion()))
            return true;
        else
            return false;
    }

    private boolean isConflictOnDescriptions(ItemDto item1, ItemDto item2) {
        if (item1.getDescription() != null && item2.getDescription() != null && !item1.getDescription().equals(item2.getDescription()))
            return true;
        else
            return false;
    }

    private boolean isConflictOnContributors(ItemDto item1, ItemDto item2) {
        int item1Size = item1.getContributors() != null ? item1.getContributors().size() : 0;
        int item2Size = item2.getContributors() != null ? item2.getContributors().size() : 0;
        for (int i = 0; i < item1Size; i++) {
            if (i < item2Size) {
                if (item1.getContributors().get(i) != null && item2.getContributors().get(i) != null && !item1.getContributors().get(i).equals(item2.getContributors().get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isConflictOnAccessibleAt(ItemDto item1, ItemDto item2) {
        int item1Size = item1.getAccessibleAt() != null ? item1.getAccessibleAt().size() : 0;
        int item2Size = item2.getAccessibleAt() != null ? item2.getAccessibleAt().size() : 0;
        for (int i = 0; i < item1Size; i++) {
            if (i < item2Size) {
                if (item1.getAccessibleAt().get(i) != null && item2.getAccessibleAt().get(i) != null && !item1.getAccessibleAt().get(i).equals(item2.getAccessibleAt().get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isConflictOnExternalIds(ItemDto item1, ItemDto item2) {
        int item1Size = item1.getExternalIds() != null ? item1.getExternalIds().size() : 0;
        int item2Size = item2.getExternalIds() != null ? item2.getExternalIds().size() : 0;
        for (int i = 0; i < item1Size; i++) {
            if (i < item2Size) {
                if (item1.getExternalIds().get(i) != null && item2.getExternalIds().get(i) != null && !item1.getExternalIds().get(i).equals(item2.getExternalIds().get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isConflictOnProperties(ItemDto item1, ItemDto item2) {
        int item1Size = item1.getProperties() != null ? item1.getProperties().size() : 0;
        int item2Size = item2.getProperties() != null ? item2.getProperties().size() : 0;
        for (int i = 0; i < item1Size; i++) {
            if (i < item2Size) {
                if (item1.getProperties().get(i) != null && item2.getProperties().get(i) != null && !item1.getProperties().get(i).equals(item2.getProperties().get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isConflictOnRelatedItems(ItemDto item1, ItemDto item2) {
        int item1Size = item1.getRelatedItems() != null ? item1.getRelatedItems().size() : 0;
        int item2Size = item2.getRelatedItems() != null ? item2.getRelatedItems().size() : 0;
        for (int i = 0; i < item1Size; i++) {
            if (i < item2Size) {
                if (item1.getRelatedItems().get(i) != null && item2.getRelatedItems().get(i) != null && !item1.getRelatedItems().get(i).equals(item2.getRelatedItems().get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isConflictOnMedia(ItemDto item1, ItemDto item2) {
        int item1Size = item1.getMedia() != null ? item1.getMedia().size() : 0;
        int item2Size = item2.getMedia() != null ? item2.getMedia().size() : 0;
        for (int i = 0; i < item1Size; i++) {
            if (i < item2Size) {
                if (item1.getMedia().get(i) != null && item2.getMedia().get(i) != null && !item1.getMedia().get(i).equals(item2.getMedia().get(i))) {
                    return true;
                }
            }
        }
        if (item1.getThumbnail() != null && item2.getThumbnail() != null && !item1.getThumbnail().equals(item2.getThumbnail())) {
            return true;
        }
        return false;
    }

    private boolean isConflictOnDigitalObjectDates(ItemDto item1, ItemDto item2) {
        if (item1 instanceof DigitalObjectDto) {
            DigitalObjectDto item1DigitalObject = (DigitalObjectDto) item1;
            if (item2 instanceof DigitalObjectDto) {
                DigitalObjectDto item2DigitalObject = (DigitalObjectDto) item2;
                if (item1DigitalObject.getDateCreated() != null && ((DigitalObjectDto) item2).getDateCreated() != null && !item1DigitalObject.getDateCreated().equals(item2DigitalObject.getDateCreated())) {
                    return true;
                }
                if (item1DigitalObject.getDateLastUpdated() != null && ((DigitalObjectDto) item2).getDateLastUpdated() != null && !item1DigitalObject.getDateLastUpdated().equals(item2DigitalObject.getDateLastUpdated())) {
                    return true;
                }
            }
        }
        return false;
    }

}
