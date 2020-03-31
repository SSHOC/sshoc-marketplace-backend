package eu.sshopencloud.marketplace.services.activities;

import eu.sshopencloud.marketplace.dto.activities.ActivityCore;
import eu.sshopencloud.marketplace.model.activities.Activity;
import eu.sshopencloud.marketplace.repositories.activities.ActivityRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;

    private final ActivityParthoodService activityParthoodService;

    private final ItemService itemService;

    private final ItemRelatedItemService itemRelatedItemService;

    public PaginatedActivities getActivities(Integer page, Integer perpage) {
        // TODO
        return null;
    }


    public Activity getActivity(Long id) {
        Activity activity = activityRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Activity.class.getName() + " with id " + id));
        return complete(activity);
    }

    private Activity complete(Activity activity) {
        activity.setComposedOf(activityParthoodService.getSteps(activity));
        activity.setPartOf(activityParthoodService.getParents(activity, null));
        activity.setRelatedItems(itemRelatedItemService.getItemRelatedItems(activity.getId()));
        activity.setOlderVersions(itemService.getOlderVersionsOfItem(activity));
        activity.setNewerVersions(itemService.getNewerVersionsOfItem(activity));
        itemService.fillAllowedVocabulariesForPropertyTypes(activity);
        return activity;
    }

    public Activity createActivity(ActivityCore newActivity) {
        // TODO
        return null;
    }

    public Activity updateActivity(Long id, ActivityCore newActivity) {
        // TODO
        return null;
    }

    public void deleteActivity(Long id) {
        // TODO
    }

}
