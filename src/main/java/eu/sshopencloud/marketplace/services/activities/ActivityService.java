package eu.sshopencloud.marketplace.services.activities;

import eu.sshopencloud.marketplace.dto.activities.ActivityCore;
import eu.sshopencloud.marketplace.model.activities.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ActivityService {


    public PaginatedActivities getActivities(Integer page, Integer perpage) {
        // TODO
        return null;
    }


    public Activity getActivity(Long id) {
        // TODO
        return null;
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
