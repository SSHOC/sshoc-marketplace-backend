package eu.sshopencloud.marketplace.repositories.items.workflow;

import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StepRepository extends ItemVersionRepository<Step> {
}
