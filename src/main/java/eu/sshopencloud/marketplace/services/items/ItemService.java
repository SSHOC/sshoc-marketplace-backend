package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
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

    private final ItemRelatedItemService itemRelatedItemService;

    private final PropertyTypeService propertyTypeService;

    private final UserRepository userRepository;


    public void addInformationContributorToItem(Item item, User contributor) {
        if (contributor != null) {
            User user = userRepository.findByUsername(contributor.getUsername());
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

    public List<ItemBasicDto> getNewerVersionsOfItem(Long itemId) {
        // TODO change to recursive subordinates query in ItemRepository
        List<ItemBasicDto> versions = new ArrayList<>();
        Item nextVersion = itemRepository.findByPrevVersionId(itemId);
        while (nextVersion != null) {
            ItemBasicDto version = new ItemBasicDto();
            version.setId(nextVersion.getId());
            version.setLabel(nextVersion.getLabel());
            version.setVersion(nextVersion.getVersion());
            versions.add(version);
            nextVersion = itemRepository.findByPrevVersion(nextVersion);
        }
        return versions;
    }

    public List<ItemBasicDto> getOlderVersionsOfItem(Long itemId) {
        // TODO change to recursive subordinates query in ItemRepository
        List<ItemBasicDto> versions = new ArrayList<ItemBasicDto>();
        Item prevVersion = itemRepository.getOne(itemId).getPrevVersion();
        while (prevVersion != null) {
            ItemBasicDto version = new ItemBasicDto();
            version.setId(prevVersion.getId());
            version.setLabel(prevVersion.getLabel());
            version.setVersion(prevVersion.getVersion());
            versions.add(version);
            prevVersion = prevVersion.getPrevVersion();
        }
        return versions;
    }

    public boolean isNewestVersion(Item item) {
        Item nextVersion = itemRepository.findByPrevVersion(item);
        return (nextVersion == null);
    }

    @SuppressWarnings("unchecked")
    public <T extends ItemDto> T completeItem(ItemDto item) {
        item.setRelatedItems(itemRelatedItemService.getItemRelatedItems(item.getId()));
        item.setOlderVersions(getOlderVersionsOfItem(item.getId()));
        item.setNewerVersions(getNewerVersionsOfItem(item.getId()));
        for (PropertyDto property: item.getProperties()) {
            propertyTypeService.completePropertyType(property.getType());
        }
        return (T) item;
    }

    public Item clearVersionForCreate(Item item) {
        if (item.getNewPrevVersion() != null) {
            Item prevnextVersion = itemRepository.findByPrevVersion(item.getNewPrevVersion());
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
            Item prevnextVersion = itemRepository.findByPrevVersion(item.getNewPrevVersion());
            if (prevnextVersion != null && !(prevnextVersion.getId().equals(item.getId()))) {
                prevnextVersion.setPrevVersion(null);
                itemRepository.saveAndFlush(prevnextVersion);
            }
        }
        if (item.getNewPrevVersion() != null) {
            Item prevnextVersion = null;
            List<ItemBasicDto> newerVersions = getNewerVersionsOfItem(item.getId());
            if (newerVersions.stream().anyMatch(i -> Objects.equals(i.getId(), item.getNewPrevVersion().getId()))) {
                prevnextVersion = itemRepository.findByPrevVersion(item);
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
        Item nextVersion = itemRepository.findByPrevVersion(item);
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
