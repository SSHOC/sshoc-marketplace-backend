package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.mappers.items.ItemConverter;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.sources.SourceRepository;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;

    private final ItemRelatedItemService itemRelatedItemService;

    private final PropertyTypeService propertyTypeService;

    private final UserRepository userRepository;

    private final SourceRepository sourceRepository;


    public List<ItemBasicDto> getItems(Long sourceId, String sourceItemId) {
        List<Item> items = itemRepository.findBySourceIdAndSourceItemId(sourceId, sourceItemId);
        return items.stream().map(ItemConverter::convertItem).collect(Collectors.toList());
    }


    public void addInformationContributorToItem(Item item, User contributor) {
        if (contributor != null) {
            User user = userRepository.findByUsername(contributor.getUsername());
            if (item.getInformationContributors() != null) {
                if (!item.getInformationContributors().contains(user)) {
                    item.getInformationContributors().add(user);
                }
            } else {
                List<User> informationContributors = new ArrayList();
                informationContributors.add(user);
                item.setInformationContributors(informationContributors);
            }
        }
    }

    public void updateInfoDates(Item item) {
        ZonedDateTime now = ZonedDateTime.now();
        item.setLastInfoUpdate(now);
        if (item.getSource() != null) {
            item.getSource().setLastHarvestedDate(now);
            sourceRepository.save(item.getSource());
        }
    }


    public List<ItemBasicDto> getNewerVersionsOfItem(Long itemId) {
        // TODO change to recursive subordinates query in ItemRepository
        List<ItemBasicDto> versions = new ArrayList<>();
        Item nextVersion = itemRepository.findByPrevVersionId(itemId);
        while (nextVersion != null) {
            versions.add(ItemConverter.convertItem(nextVersion));
            nextVersion = itemRepository.findByPrevVersion(nextVersion);
        }
        return versions;
    }

    public List<ItemBasicDto> getOlderVersionsOfItem(Long itemId) {
        // TODO change to recursive subordinates query in ItemRepository
        List<ItemBasicDto> versions = new ArrayList<>();
        Item prevVersion = itemRepository.getOne(itemId).getPrevVersion();
        while (prevVersion != null) {
            versions.add(ItemConverter.convertItem(prevVersion));
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

    // TEMPORARY
    public void rewriteSources() {
        List<Item> items = itemRepository.findAll();
        for (Item item: items) {
            Optional<Property> propertyHolder = item.getProperties().stream().filter(p -> p.getType().getCode().equals("source-id")).findFirst();
            log.debug("Item " + item.getId() + " " + item.getLabel() + " has source-id " + propertyHolder.isPresent());
            if (propertyHolder.isPresent()) {
                item.setSourceItemId(propertyHolder.get().getValue());
                itemRepository.save(item);
            }
            if (item.getCategory().equals(ItemCategory.TOOL)) {
                log.debug("Item " + item.getId() + " " + item.getLabel() + " is tool");
                if (item.getId() > 30) {
                    item.setSource(sourceRepository.getOne(1l)); // tapor
                    itemRepository.save(item);
                }
            }
            if (item.getCategory().equals(ItemCategory.TRAINING_MATERIAL)) {
                log.debug("Item " + item.getId() + " " + item.getLabel() + " is training material");
                if (item.getId() > 30 && item.getAccessibleAt().contains("programminghistorian")) {
                    item.setSource(sourceRepository.getOne(2l)); // programminghistorian
                    itemRepository.save(item);
                }
            }

        }
    }

}
