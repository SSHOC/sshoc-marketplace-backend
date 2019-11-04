package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import eu.sshopencloud.marketplace.services.DataViolationException;
import eu.sshopencloud.marketplace.services.actors.ActorRoleService;
import eu.sshopencloud.marketplace.services.actors.ActorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemContributorService {

    private final ActorService actorService;

    private final ActorRoleService actorRoleService;

    public List<ItemContributor> validate(String prefix, List<ItemContributorId> itemContributors, Item item) throws DataViolationException {
        List<ItemContributor> result = new ArrayList<ItemContributor>();
        if (itemContributors != null) {
            for (int i = 0; i < itemContributors.size(); i++) {
                ItemContributorId itemContributor = itemContributors.get(i);
                result.add(validate(prefix + "[" + i + "].", itemContributor, item));
            }
        }
        return result;
    }

    public ItemContributor validate(String prefix, ItemContributorId itemContributor, Item item) throws DataViolationException {
        ItemContributor result = new ItemContributor();
        result.setItem(item);
        if (itemContributor.getActor() == null) {
            throw new DataViolationException(prefix + "actor", "null");
        }
        result.setActor(actorService.validate(prefix + "actor.", itemContributor.getActor()));
        if (itemContributor.getRole() == null) {
            throw new DataViolationException(prefix + "role", "null");
        }
        result.setRole(actorRoleService.validate(prefix + "role.", itemContributor.getRole()));
        return result;
    }


}
