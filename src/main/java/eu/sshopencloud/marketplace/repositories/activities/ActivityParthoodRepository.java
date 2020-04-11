package eu.sshopencloud.marketplace.repositories.activities;

import eu.sshopencloud.marketplace.model.activities.Activity;
import eu.sshopencloud.marketplace.model.activities.ActivityParthood;
import eu.sshopencloud.marketplace.model.activities.ActivityParthoodId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityParthoodRepository extends JpaRepository<ActivityParthood, ActivityParthoodId> {

    List<ActivityParthood> findByParentOrderByOrd(Activity parent);

    List<ActivityParthood> findByChildOrderByParentLabel(Activity child);

}
