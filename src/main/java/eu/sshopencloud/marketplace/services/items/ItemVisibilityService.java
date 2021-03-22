package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.model.items.VersionedItem;
import eu.sshopencloud.marketplace.model.items.VersionedItemStatus;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

import static eu.sshopencloud.marketplace.model.items.ItemStatus.*;


@Service
@RequiredArgsConstructor
class ItemVisibilityService {

    private final UserService userService;


    public void setupItemVersionVisibility(Item version, VersionedItem versionedItem, boolean changeStatus) {
        User currentUser = userService.loadLoggedInUser();
        if (currentUser == null || !currentUser.isContributor())
            throw new AccessDeniedException("Not authorized to create new item version");

        version.setInformationContributor(currentUser);
        assignItemVersionStatus(version, versionedItem, currentUser, changeStatus);

        boolean proposed = (version.getStatus() == ItemStatus.SUGGESTED || version.getStatus() == ItemStatus.INGESTED);
        version.setProposedVersion(proposed);
    }

    private void assignItemVersionStatus(Item version, VersionedItem versionedItem, User currentUser, boolean changeStatus) {
        if (!versionedItem.isActive()) {
            throw new IllegalArgumentException(
                    String.format("Deleted/merged item with id %s cannot be modified anymore", versionedItem.getPersistentId())
            );
        }

        if (!changeStatus && version.getPrevVersion() != null) {
            version.setStatus(version.getPrevVersion().getStatus());
            return;
        }

        // The order of these role checks does matter as, for example, a moderator is a contributor as well
        if (currentUser.isModerator()) {
            version.setStatus(ItemStatus.APPROVED);
            versionedItem.setStatus(VersionedItemStatus.REVIEWED);
        }
        else if (currentUser.isSystemContributor()) {
            version.setStatus(ItemStatus.INGESTED);

            if (versionedItem.getStatus() != VersionedItemStatus.REVIEWED)
                versionedItem.setStatus(VersionedItemStatus.INGESTED);
        }
        else if (currentUser.isContributor()) {
            version.setStatus(ItemStatus.SUGGESTED);

            if (versionedItem.getStatus() != VersionedItemStatus.REVIEWED)
                versionedItem.setStatus(VersionedItemStatus.SUGGESTED);
        }
    }


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