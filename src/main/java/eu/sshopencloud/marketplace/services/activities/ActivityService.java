package eu.sshopencloud.marketplace.services.activities;

import eu.sshopencloud.marketplace.dto.activities.ActivityCore;
import eu.sshopencloud.marketplace.model.activities.Activity;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.repositories.activities.ActivityRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.validators.activities.ActivityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.ZonedDateTime;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;

    private final ActivityValidator activityValidator;

    private final ActivityParthoodService activityParthoodService;

    private final ItemService itemService;

    private final ItemRelatedItemService itemRelatedItemService;

    private final IndexService indexService;


    public PaginatedActivities getActivities(Integer page, Integer perpage) {
        Page<Activity> activities = activityRepository.findAll(PageRequest.of(page - 1, perpage, Sort.by(Sort.Order.asc("label"))));
        for (Activity activity: activities) {
            complete(activity);
        }

        return PaginatedActivities.builder().activities(activities.getContent())
                .count(activities.getContent().size()).hits(activities.getTotalElements()).page(page).perpage(perpage).pages(activities.getTotalPages())
                .build();
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

    public Activity createActivity(ActivityCore activityCore) {
        Activity activity = activityValidator.validate(activityCore, null);
        activity.setLastInfoUpdate(ZonedDateTime.now());

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        itemService.addInformationContributorToItem(activity, LoggedInUserHolder.getLoggedInUser());

        Item nextVersion = itemService.clearVersionForCreate(activity);
        activity = activityRepository.save(activity);
        activityParthoodService.saveSteps(activity);
        itemService.switchVersion(activity, nextVersion);

        if (!activity.getComposedOf().isEmpty()) {
            // index only complex activities
            indexService.indexItem(activity);
        }
        return complete(activity);
    }

    public Activity updateActivity(Long id, ActivityCore activityCore) {
        // TODO
        return null;
    }

    public void deleteActivity(Long id) {
        // TODO
    }

}
