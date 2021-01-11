package eu.sshopencloud.marketplace.repositories.items.workflow;

import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.model.workflows.StepsTree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface StepsTreeRepository extends JpaRepository<StepsTree, Long> {

    Optional<StepsTree> findByWorkflowIdAndStepId(long workflowId, long stepId);

    @Query("select distinct w.versionedItem.persistentId from StepsTree st join st.workflow w where st.step = :step")
    String findWorkflowPersistentIdByStep(@Param("step") Step step);

    @Query("select st from StepsTree st where st.workflow.id = :workflowId and st.step.versionedItem.persistentId = :stepId")
    Optional<StepsTree> findByWorkflowIdAndStepPersistentId(@Param("workflowId") long workflowVersionId,
                                                            @Param("stepId") String stepId);
}
