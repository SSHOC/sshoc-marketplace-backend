package eu.sshopencloud.marketplace.validators.activities;

import eu.sshopencloud.marketplace.dto.activities.ActivityCore;
import eu.sshopencloud.marketplace.model.activities.Activity;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.repositories.activities.ActivityRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.items.ItemValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ActivityValidator {

    private final ActivityRepository activityRepository;

    private final ItemValidator itemValidator;

    public Activity validate(ActivityCore activityCore, Long activityId) throws ValidationException {
        Activity activity = getOrCreateActivity(activityId);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(activityCore, "Activity");

        itemValidator.validate(activityCore, ItemCategory.ACTIVITY, activity, errors);

        List<Activity> children = new ArrayList<Activity>();
        if (activityCore.getComposedOf() != null) {
            for (int i = 0; i < activityCore.getComposedOf().size(); i++) {
                errors.pushNestedPath("composedOf" + "[" + i + "]");
                Activity child = validate( activityCore.getComposedOf().get(i), errors);
                if (child != null) {
                    children.add(child);
                }
                errors.popNestedPath();
            }
        }
        activity.setComposedOf(children);

        if (activityCore.getPrevVersionId() != null) {
            if (activityId != null && activity.getId().equals(activityCore.getPrevVersionId())) {
                errors.rejectValue("prevVersionId", "field.cycle", "Previous activity cannot be the same as the current one.");
            }
            Optional<Activity> prevVersionHolder = activityRepository.findById(activityCore.getPrevVersionId());
            if (!prevVersionHolder.isPresent()) {
                errors.rejectValue("prevVersionId", "field.notExist", "Previous activity does not exist.");
            } else {
                activity.setNewPrevVersion(prevVersionHolder.get());
            }
        }

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return activity;
        }
    }

    private Activity validate(Long activityId, Errors errors) {
        if (activityId == null) {
            errors.rejectValue("", "field.required", "Step activity is required.");
            return null;
        }
        Optional<Activity> activityHolder = activityRepository.findById(activityId);
        if (!activityHolder.isPresent()) {
            errors.rejectValue("", "field.notExist", "Step activity does not exist.");
            return null;
        } else {
            return activityHolder.get();
        }
    }

    private Activity getOrCreateActivity(Long activityId) {
        if (activityId != null) {
            return activityRepository.getOne(activityId);
        } else {
            return new Activity();
        }
    }

}
