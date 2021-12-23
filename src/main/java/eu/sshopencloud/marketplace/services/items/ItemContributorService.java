package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import eu.sshopencloud.marketplace.repositories.items.ItemContributorCriteriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemContributorService {

    private final ItemContributorCriteriaRepository itemContributorRepository;

    public List<ItemContributor> getItemContributors(Long itemId) {
        return itemContributorRepository.findByItemId(itemId);
    }

    public List<ItemContributor> getItemContributorsByActor(Long actorId, String roleCode ){

        return itemContributorRepository.findByActorIdAndActorRole(actorId, roleCode);
    }

}
