package eu.sshopencloud.marketplace.conf.startup.activities;

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

    private final ActivityRepository activityRepository;

    private final ActivityParthoodRepository activityParthoodRepository;

    private final ActivityParthoodService activityParthoodService;

    private final ConceptRepository conceptRepository;

    private  final IndexService indexService;

    public void createActivities(List<Activity> newActivities, List<ActivityParthood> activityParthoods) {
        List<Activity> activities = new ArrayList<Activity>();
        for (Activity newActivity: newActivities) {
            for (Property property: newActivity.getProperties()) {
                if (property.getConcept() != null) {
                    property.setConcept(conceptRepository.findById(ConceptId.builder().code(property.getConcept().getCode()).vocabulary(property.getConcept().getVocabulary().getCode()).build()).get());
                }
            }
            activities.add(activityRepository.save(newActivity));
        }
        activityParthoodRepository.saveAll(activityParthoods);
        for (Activity activity: activities) {
            activity.setComposedOf(activityParthoodService.getSteps(activity));
            if (!activity.getComposedOf().isEmpty()) {
                // index only complex activities
                indexService.indexItem(activity);
            }
        }
    }

}
