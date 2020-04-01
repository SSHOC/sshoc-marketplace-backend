package eu.sshopencloud.marketplace.repositories.activities;

import eu.sshopencloud.marketplace.model.activities.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

}
