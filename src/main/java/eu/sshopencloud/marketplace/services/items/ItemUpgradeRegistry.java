package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.Item;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Component
@RequestScope
class ItemUpgradeRegistry<I extends Item> {

    private final Map<String, Item> itemVersions;


    public ItemUpgradeRegistry() {
        this.itemVersions = new HashMap<>();
    }


    public Optional<I> resolveUpgradedVersion(String persistentId) {
        if (!itemVersions.containsKey(persistentId))
            return Optional.empty();

        return Optional.of((I) itemVersions.get(persistentId));
    }

    public void registerUpgradedVersion(Item version) {
        itemVersions.put(version.getPersistentId(), version);
    }
}
