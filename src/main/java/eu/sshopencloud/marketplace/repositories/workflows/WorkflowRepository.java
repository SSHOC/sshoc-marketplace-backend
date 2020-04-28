package eu.sshopencloud.marketplace.repositories.workflows;

import eu.sshopencloud.marketplace.model.workflows.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {

}

