package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemInline;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.licenses.LicenseRepository;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    private final PropertyTypeService propertyTypeService;

    public List<ItemInline> getNewerVersionsOfItem(Item item) {
        // TODO change to recursive subordinates query in ItemRepository
        List<ItemInline> versions = new ArrayList<ItemInline>();
        Item nextVersion = itemRepository.findItemByPrevVersion(item);
        while (nextVersion != null) {
            ItemInline version = new ItemInline();
            version.setId(nextVersion.getId());
            version.setLabel(nextVersion.getLabel());
            version.setVersion(nextVersion.getVersion());
            versions.add(version);
            nextVersion = itemRepository.findItemByPrevVersion(nextVersion);
        }
        return versions;
    }

    public List<ItemInline> getOlderVersionsOfItem(Item item) {
        // TODO change to recursive subordinates query in ItemRepository
        List<ItemInline> versions = new ArrayList<ItemInline>();
        Item prevVersion = item.getPrevVersion();
        while (prevVersion != null) {
            ItemInline version = new ItemInline();
            version.setId(prevVersion.getId());
            version.setLabel(prevVersion.getLabel());
            version.setVersion(prevVersion.getVersion());
            versions.add(version);
            prevVersion = prevVersion.getPrevVersion();
        }
        return versions;
    }

    public boolean isNewestVersion(Item item) {
        Item nextVersion = itemRepository.findItemByPrevVersion(item);
        return (nextVersion == null);
    }

    public void fillAllowedVocabulariesForPropertyTypes(Item item) {
        for (Property property: item.getProperties()) {
            PropertyType propertyType = property.getType();
            if (propertyType != null) {
                propertyType.setAllowedVocabularies(propertyTypeService.getAllowedVocabulariesForPropertyType(propertyType));
            }
        }
    }

    public void switchVersionForCreate(Item item) {
        if (item.getPrevVersion() != null) {
            Item nextVersion = itemRepository.findItemByPrevVersion(item.getPrevVersion());
            if (nextVersion != null) {
                nextVersion.setPrevVersion(item);
                itemRepository.save(nextVersion);
            }
        }
    }


    public void switchVersionForUpdate(Item item) {
        if (item.getPrevVersion() != null) {
            // TODO switch in more comlex paths
            Item nextVersion = itemRepository.findItemByPrevVersion(item.getPrevVersion());
            if (nextVersion != null) {
                nextVersion.setPrevVersion(null);
                itemRepository.save(nextVersion);
            }
        }
    }


    public void switchVersionForDelete(Item item) {
        Item nextVersion = itemRepository.findItemByPrevVersion(item);
        if (nextVersion != null) {
            if (item.getPrevVersion() != null) {
                nextVersion.setPrevVersion(item.getPrevVersion());
            } else {
                nextVersion.setPrevVersion(null);
            }
            itemRepository.save(nextVersion);
        }
    }


}
