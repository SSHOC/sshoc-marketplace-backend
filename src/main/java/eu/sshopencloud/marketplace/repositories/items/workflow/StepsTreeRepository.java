package eu.sshopencloud.marketplace.repositories.items.workflow;

import eu.sshopencloud.marketplace.model.workflows.StepsTree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface StepsTreeRepository extends JpaRepository<StepsTree, Long> {

    StepsTree findByStepId(long stepId);
    Optional<StepsTree> findByWorkflowIdAndStepId(long workflowId, long stepId);

    @Query("select st from StepsTree st where st.workflow.id = :workflowId and st.step.versionedItem.persistentId = :stepId")
    Optional<StepsTree> findByWorkflowIdAndStepPersistentId(@Param("workflowId") long workflowVersionId,
                                                            @Param("stepId") String stepId);
}
