package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.controllers.actors.ActorConverter;
import eu.sshopencloud.marketplace.controllers.actors.ActorRoleConverter;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ItemContributorConverter {

    public ItemContributor convert(ItemContributorId itemContributor, Item item) {
        ItemContributor result = new ItemContributor();
        result.setItem(item);
        result.setActor(ActorConverter.convert(itemContributor.getActor()));
        result.setRole(ActorRoleConverter.convert(itemContributor.getRole()));
        return result;
    }

    public List<ItemContributor> convert(List<ItemContributorId> itemContributors, Item item) {
        List<ItemContributor> result = new ArrayList<ItemContributor>();
        if (itemContributors != null) {
            for (ItemContributorId itemContributor : itemContributors) {
                result.add(convert(itemContributor, item));
            }
        }
        return result;
    }

}
