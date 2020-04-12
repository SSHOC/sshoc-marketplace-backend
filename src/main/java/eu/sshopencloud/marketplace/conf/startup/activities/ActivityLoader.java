package eu.sshopencloud.marketplace.conf.startup.activities;

import eu.sshopencloud.marketplace.conf.startup.items.ItemLoader;
import eu.sshopencloud.marketplace.model.activities.Activity;
import eu.sshopencloud.marketplace.model.activities.ActivityParthood;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.repositories.activities.ActivityParthoodRepository;
import eu.sshopencloud.marketplace.repositories.activities.ActivityRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.services.activities.ActivityParthoodService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLoader {

    private final ItemLoader itemLoader;

    private final ActivityRepository activityRepository;

    private final ActivityParthoodRepository activityParthoodRepository;

    private final ActivityParthoodService activityParthoodService;

    private  final IndexService indexService;

    public void createActivities(String profile, List<Activity> newActivities, List<ActivityParthood> activityParthoods) {
        List<Activity> activities = new ArrayList<Activity>();
        for (Activity newActivity: newActivities) {
            itemLoader.completeProperties(newActivity);
            itemLoader.completeContributors(newActivity);
            activities.add(activityRepository.save(newActivity));
        }
        activityParthoodRepository.saveAll(activityParthoods);
        for (Activity activity: activities) {
            activity.setComposedOf(activityParthoodService.getSteps(activity));
            if (!activity.getComposedOf().isEmpty()) {
                // index only complex activities
                if (!profile.equals("prod")) {
                    indexService.indexItem(activity);
                }
            }
        }
    }

}
