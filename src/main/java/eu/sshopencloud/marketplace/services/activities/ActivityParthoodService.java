package eu.sshopencloud.marketplace.services.activities;

import eu.sshopencloud.marketplace.model.activities.Activity;
import eu.sshopencloud.marketplace.dto.activities.ActivityBasicDto;
import eu.sshopencloud.marketplace.model.activities.ActivityParthood;
import eu.sshopencloud.marketplace.repositories.activities.ActivityParthoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ActivityParthoodService {

    private final ActivityParthoodRepository activityParthoodRepository;


    public List<Activity> getSteps(Activity activity) {
        List<ActivityParthood> activityParthoods = activityParthoodRepository.findByParentOrderByOrd(activity);
        List<Activity> steps = activityParthoods.stream().map(ActivityParthood::getChild).collect(Collectors.toList());
        for (Activity step : steps) {
            // TODO recursion
            step.setComposedOf(getSteps(step));
            step.setPartOf(getParents(step, activity));
        }
        return steps;
    }


    public void saveSteps(Activity activity) {
        List<ActivityParthood> activityParthoods = new ArrayList();
        for (int i = 0; i < activity.getComposedOf().size(); i++) {
            Activity step = activity.getComposedOf().get(i);
            ActivityParthood activityParthood = new ActivityParthood();
            activityParthood.setParent(activity);
            activityParthood.setChild(step);
            activityParthood.setOrd(i);
            activityParthoods.add(activityParthood);
        }
        activityParthoodRepository.saveAll(activityParthoods);
    }


    public List<ActivityBasicDto> getParents(Activity activity, Activity contextParent) {
        List<ActivityParthood> activityParthoods = activityParthoodRepository.findByChildOrderByParentLabel(activity);
        List<Activity> parents = activityParthoods.stream().map(ActivityParthood::getParent).filter(parent -> contextParent != null && !parent.getId().equals(contextParent.getId())).collect(Collectors.toList());
        if (contextParent != null) {
            parents.add(0, contextParent); // for steps its parent is always first
        }
        return parents.stream().map(parent -> ActivityBasicDto.builder().id(parent.getId()).label(parent.getLabel()).description(parent.getDescription()).build()).collect(Collectors.toList());
    }




}
