package eu.sshopencloud.marketplace.controllers.activities;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.activities.ActivityCore;
import eu.sshopencloud.marketplace.model.activities.Activity;
import eu.sshopencloud.marketplace.services.DataViolationException;
import eu.sshopencloud.marketplace.services.activities.ActivityService;
import eu.sshopencloud.marketplace.services.activities.PaginatedActivities;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptDisallowedException;
import eu.sshopencloud.marketplace.services.vocabularies.DisallowedObjectTypeException;
import eu.sshopencloud.marketplace.services.vocabularies.TooManyObjectTypesException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ActivityController {

    @Value("${marketplace.pagination.default-perpage}")
    private Integer defualtPerpage;

    @Value("${marketplace.pagination.maximal-perpage}")
    private Integer maximalPerpage;

    private final ActivityService activityService;

    @GetMapping(path = "/activities", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedActivities> getActivities(@RequestParam(value = "page", required = false) Integer page,
                                                             @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        perpage = perpage == null ? defualtPerpage : perpage;
        if (perpage > maximalPerpage) {
            throw new PageTooLargeException(maximalPerpage);
        }
        page = page == null ? 1 : page;

        PaginatedActivities activities = activityService.getActivities(page, perpage);
        return ResponseEntity.ok(activities);
    }

    @GetMapping(path = "/activities/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Activity> getActivity(@PathVariable("id") long id) {
        Activity activity = activityService.getActivity(id);
        return ResponseEntity.ok(activity);
    }

    @PostMapping(path = "/activities", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Activity> createActivity(@RequestBody ActivityCore newActivity)
            throws DataViolationException, ConceptDisallowedException, DisallowedObjectTypeException, TooManyObjectTypesException {
        Activity activity = activityService.createActivity(newActivity);
        return ResponseEntity.ok(activity);
    }

    @PutMapping(path = "/activities/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Activity> updateActivity(@PathVariable("id") long id, @RequestBody ActivityCore newActivity)
            throws DataViolationException, ConceptDisallowedException, DisallowedObjectTypeException, TooManyObjectTypesException {
        Activity activity = activityService.updateActivity(id, newActivity);
        return ResponseEntity.ok(activity);
    }

    @DeleteMapping("/activities/{id}")
    public void deleteActivity(@PathVariable("id") long id) {
        activityService.deleteActivity(id);
    }

}
