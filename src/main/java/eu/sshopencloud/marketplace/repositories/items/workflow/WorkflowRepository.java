package eu.sshopencloud.marketplace.repositories.items.workflow;

import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface WorkflowRepository extends ItemVersionRepository<Workflow> {
}

