package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemInline;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    private final PropertyTypeService propertyTypeService;

    private final UserRepository userRepository;


    public void addInformationContributorToItem(Item item, User contributor) {
        if (contributor != null) {
            User user = userRepository.findUserByUsername(contributor.getUsername());
            if (item.getInformationContributors() != null) {
                if (!item.getInformationContributors().contains(user)) {
                    item.getInformationContributors().add(user);
                }
            } else {
                List<User> informationContributors = new ArrayList<User>();
                informationContributors.add(user);
                item.setInformationContributors(informationContributors);
            }
        }
    }

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

    public Item clearVersionForCreate(Item item) {
        if (item.getNewPrevVersion() != null) {
            Item prevnextVersion = itemRepository.findItemByPrevVersion(item.getNewPrevVersion());
            if (prevnextVersion != null) {
                prevnextVersion.setPrevVersion(null);
                prevnextVersion = itemRepository.saveAndFlush(prevnextVersion);
            }
            item.setPrevVersion(item.getNewPrevVersion());
            return prevnextVersion;
        }
        return null;
    }

    public Item clearVersionForUpdate(Item item) {
        if (item.getNewPrevVersion() != null) {
            Item prevnextVersion = itemRepository.findItemByPrevVersion(item.getNewPrevVersion());
            if (prevnextVersion != null && !(prevnextVersion.getId().equals(item.getId()))) {
                prevnextVersion.setPrevVersion(null);
                itemRepository.saveAndFlush(prevnextVersion);
            }
        }
        if (item.getNewPrevVersion() != null) {
            Item prevnextVersion = null;
            List<ItemInline> newerVersions = getNewerVersionsOfItem(item);
            if (newerVersions.stream().anyMatch(i -> Objects.equals(i.getId(), item.getNewPrevVersion().getId()))) {
                prevnextVersion = itemRepository.findItemByPrevVersion(item);
                prevnextVersion.setPrevVersion(null);
                prevnextVersion = itemRepository.saveAndFlush(prevnextVersion);
            }
            item.setPrevVersion(item.getNewPrevVersion());
            return prevnextVersion;
        } else {
            item.setPrevVersion(null);
            return null;
        }
    }

    public Item clearVersionForDelete(Item item) {
        if (item.getPrevVersion() != null) {
            item.setPrevVersion(null);
            itemRepository.saveAndFlush(item);
        }
        Item nextVersion = itemRepository.findItemByPrevVersion(item);
        if (nextVersion != null) {
            nextVersion.setPrevVersion(null);
            return itemRepository.save(nextVersion);
        }
        return null;
    }

    public void switchVersion(Item item, Item nextVersion) {
        if (nextVersion != null) {
            nextVersion.setPrevVersion(item);
            itemRepository.save(nextVersion);
        }
    }


}
