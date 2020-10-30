package eu.sshopencloud.marketplace.repositories.items.workflow;

import eu.sshopencloud.marketplace.model.workflows.StepsTree;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface StepsTreeRepository extends JpaRepository<StepsTree, Long> {

    StepsTree findByStepId(long stepId);
    Optional<StepsTree> findByWorkflowIdAndStepId(long workflowId, long stepId);
}
