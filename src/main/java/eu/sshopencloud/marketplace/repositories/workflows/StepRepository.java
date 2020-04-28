package eu.sshopencloud.marketplace.repositories.workflows;

import eu.sshopencloud.marketplace.model.workflows.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StepRepository extends JpaRepository<Step, Long> {

}
