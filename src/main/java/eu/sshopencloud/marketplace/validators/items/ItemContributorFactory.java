package eu.sshopencloud.marketplace.validators.items;

import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import eu.sshopencloud.marketplace.repositories.items.ItemContributorCriteriaRepository;
import eu.sshopencloud.marketplace.validators.actors.ActorRoleFactory;
import eu.sshopencloud.marketplace.validators.actors.ActorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemContributorFactory {

    private final ActorFactory actorFactory;
    private final ActorRoleFactory actorRoleFactory;
    private final ItemContributorCriteriaRepository itemContributorRepository;


    public List<ItemContributor> create(List<ItemContributorId> itemContributorIds, Item item, Errors errors, String nestedPath) {
        List<ItemContributor> itemContributors = new ArrayList<>();
        if (itemContributorIds != null) {
            for (int i = 0; i < itemContributorIds.size(); i++) {
                errors.pushNestedPath(nestedPath + "[" + i + "]");
                ItemContributor itemContributor = create(itemContributorIds.get(i), item, itemContributors, errors);
                if (itemContributor != null) {
                    itemContributors.add(itemContributor);
                }
                errors.popNestedPath();
            }
        }

        return itemContributors;
    }

    private ItemContributor create(ItemContributorId itemContributorId, Item item, List<ItemContributor> processedContributors, Errors errors) {
        Actor actor = null;
        if (itemContributorId.getActor() == null) {
            errors.rejectValue("actor", "field.required", "Actor is required.");
        } else {
            errors.pushNestedPath("actor");
            actor = actorFactory.create(itemContributorId.getActor(), errors);
            if (actor != null) {
                for (ItemContributor processedContributor: processedContributors) {
                    if (actor.getId().equals(processedContributor.getActor().getId())) {
                        errors.rejectValue("id", "field.repeated", "The actor has already occurred.");
                        break;
                    }
                }
            }
            errors.popNestedPath();
        }

        ActorRole role = null;
        if (itemContributorId.getRole() == null) {
            errors.rejectValue("role", "field.required", "Actor role is required.");
        } else {
            errors.pushNestedPath("role");
            role = actorRoleFactory.create(itemContributorId.getRole(), errors);
            errors.popNestedPath();
        }
        if (actor != null && role != null) {
            ItemContributor itemContributor = null;
            if (item.getId() != null) {
                itemContributor = itemContributorRepository.findByItemIdAndActorId(item.getId(), actor.getId());
            }
            if (itemContributor == null) {
                itemContributor = new ItemContributor();
                itemContributor.setItem(item);
                itemContributor.setActor(actor);
            }
            itemContributor.setRole(role);
            return itemContributor;
        }

        return null;
    }
}
