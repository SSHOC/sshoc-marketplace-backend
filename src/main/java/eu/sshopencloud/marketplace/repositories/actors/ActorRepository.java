package eu.sshopencloud.marketplace.repositories.actors;

import eu.sshopencloud.marketplace.model.actors.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Long> {

    List<Actor> getActorsByAffiliations(Actor actor);
}
