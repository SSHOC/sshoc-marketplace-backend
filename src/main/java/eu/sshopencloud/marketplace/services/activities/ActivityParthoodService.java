package eu.sshopencloud.marketplace.services.activities;

import eu.sshopencloud.marketplace.model.activities.Activity;
import eu.sshopencloud.marketplace.model.activities.ActivityInline;
import eu.sshopencloud.marketplace.model.activities.ActivityParthood;
import eu.sshopencloud.marketplace.repositories.activities.ActivityParthoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ActivityParthoodService {

    private final ActivityParthoodRepository activityParthoodRepository;

    public List<Activity> getSteps(Activity activity) {
        List<ActivityParthood> activityParthoods = activityParthoodRepository.findActivityParthoodsByParentOrderByOrd(activity);
        List<Activity> steps = activityParthoods.stream().map(ActivityParthood::getChild).collect(Collectors.toList());
        for (Activity step : steps) {
            // TODO recursion
            step.setComposedOf(getSteps(step));
            step.setPartOf(getParents(step, activity));
        }
        return steps;
    }

    public List<ActivityInline> getParents(Activity activity, Activity contextParent) {
        List<ActivityParthood> activityParthoods = activityParthoodRepository.findActivityParthoodsByChildOrderByParentLabel(activity);
        List<Activity> parents = activityParthoods.stream().map(ActivityParthood::getParent).filter(parent -> contextParent != null && !parent.getId().equals(contextParent.getId())).collect(Collectors.toList());
        if (contextParent != null) {
            parents.add(0, contextParent); // for steps its parent is always first
        }
        return parents.stream().map(parent -> ActivityInline.builder().id(parent.getId()).label(parent.getLabel()).description(parent.getDescription()).build()).collect(Collectors.toList());
    }

}
