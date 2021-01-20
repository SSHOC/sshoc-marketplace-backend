package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static eu.sshopencloud.marketplace.model.items.ItemStatus.*;


@Service
@RequiredArgsConstructor
class ItemVisibilityService {

    public boolean shouldCurrentUserSeeItem(Item item) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();
        return shouldSeeItem(item, currentUser);
    }

    public boolean shouldSeeItem(Item item, User user) {
        if (item.getStatus().equals(ItemStatus.DEPRECATED))
            return false;

        return hasAccessToVersion(item, user);
    }

    public boolean hasAccessToVersion(Item version, User user) {
        ItemStatus itemStatus = version.getStatus();

        if (itemStatus.equals(ItemStatus.APPROVED))
            return true;

        if (user == null)
            return false;

        if (user.isModerator())
            return true;

        return List.of(SUGGESTED, INGESTED, DISAPPROVED).contains(itemStatus)
                && user.isContributor()
                && user.equals(version.getInformationContributor());
    }
}
