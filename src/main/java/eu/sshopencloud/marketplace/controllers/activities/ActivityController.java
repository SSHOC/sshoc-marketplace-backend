package eu.sshopencloud.marketplace.controllers.activities;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.activities.ActivityCore;
import eu.sshopencloud.marketplace.dto.activities.ActivityDto;
import eu.sshopencloud.marketplace.services.activities.ActivityService;
import eu.sshopencloud.marketplace.services.activities.PaginatedActivities;
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

        return ResponseEntity.ok(activityService.getActivities(page, perpage));
    }

    @GetMapping(path = "/activities/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActivityDto> getActivity(@PathVariable("id") long id) {
        return ResponseEntity.ok(activityService.getActivity(id));
    }

    @PostMapping(path = "/activities", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActivityDto> createActivity(@RequestBody ActivityCore newActivity) {
        return ResponseEntity.ok(activityService.createActivity(newActivity));
    }

    @PutMapping(path = "/activities/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActivityDto> updateActivity(@PathVariable("id") long id, @RequestBody ActivityCore updatedActivity) {
        return ResponseEntity.ok(activityService.updateActivity(id, updatedActivity));
    }

    @DeleteMapping("/activities/{id}")
    public void deleteActivity(@PathVariable("id") long id) {
        activityService.deleteActivity(id);
    }

}
