package eu.sshopencloud.marketplace.repositories.actors;

import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActorHistoryRepository extends JpaRepository <ActorHistory, Long>{

    List<ActorHistory> findActorHistoryByActor(Actor actor);

}
