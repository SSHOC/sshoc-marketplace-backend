package eu.sshopencloud.marketplace.repositories.migrations;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import lombok.Getter;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class StepsTreeMigration implements CustomTaskChange {

    @Getter
    private static class StepsTreeEntry {
        private static long nextId = 1;

        private final long id;
        private final long workflowId;
        private final Long stepId;
        private final int ord;
        private final boolean root;
        private final StepsTreeEntry parent;

        public StepsTreeEntry(long workflowId) {
            this.id = nextId++;
            this.workflowId = workflowId;
            this.stepId = null;
            this.ord = 0;
            this.root = true;
            this.parent = null;
        }

        public StepsTreeEntry(ResultSet row, long workflowId, StepsTreeEntry parent) throws SQLException {
            this.id = nextId++;
            this.workflowId = workflowId;
            this.stepId = row.getLong("id");
            this.ord = row.getInt("ord");
            this.root = false;
            this.parent = parent;
        }

        public Long getParentId() {
            return (parent != null) ? parent.getId() : null;
        }
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        JdbcConnection connection = (JdbcConnection) database.getConnection();

        String stepsInsert =
                "insert into steps_trees (id, workflow_id, step_id, parent_id, ord, is_root) " +
                        "values (?, ?, ?, ?, ?, ?)";

        try (
                Statement workflowsStatement = connection.createStatement();
                PreparedStatement insertBatch = connection.prepareStatement(stepsInsert)
        ) {
            ResultSet step_ids = workflowsStatement.executeQuery("select * from steps where step_id is not null");
            if (!step_ids.next()) {
                return;
            }
            ResultSet workflowIds = workflowsStatement.executeQuery("select id from workflows");

            while (workflowIds.next()) {
                long workflowId = workflowIds.getLong("id");

                String stepsQuery =
                        "with recursive parent_step as (" +
                            "select id, step_id, ord, 1 as level from steps where workflow_id = ? " +
                            "union all " +
                            "select s.id, s.step_id, s.ord, parent_step.level + 1 as level " +
                                "from steps s " +
                                "join parent_step on s.step_id = parent_step.id " +
                        ") " +
                        "select * from parent_step order by level";
                try (PreparedStatement stepsStatement = connection.prepareStatement(stepsQuery)) {
                    stepsStatement.setLong(1, workflowId);
                    ResultSet steps = stepsStatement.executeQuery();

                    Map<Long, StepsTreeEntry> stepTrees = new HashMap<>();
                    StepsTreeEntry stepsRoot = new StepsTreeEntry(workflowId);

                    insertTreeSteps(insertBatch, stepsRoot);

                    while (steps.next()) {
                        long parentStepId = steps.getLong("step_id");
                        StepsTreeEntry parentTree = stepsRoot;

                        if (!steps.wasNull())
                            parentTree = stepTrees.get(parentStepId);

                        StepsTreeEntry treeEntry = new StepsTreeEntry(steps, workflowId, parentTree);
                        stepTrees.put(treeEntry.getStepId(), treeEntry);

                        insertTreeSteps(insertBatch, treeEntry);
                    }
                }
            }

            insertBatch.executeBatch();

            workflowsStatement.executeUpdate("update steps set step_id = null");
        }
        catch (DatabaseException | SQLException e) {
            throw new CustomChangeException(e);
        }
    }

    private void insertTreeSteps(PreparedStatement batch, StepsTreeEntry stepsTree) throws SQLException {
        batch.setLong(1, stepsTree.getId());
        batch.setLong(2, stepsTree.getWorkflowId());

        Long stepId = stepsTree.getStepId();
        if (stepId != null)
            batch.setLong(3, stepsTree.getStepId());
        else
            batch.setNull(3, Types.BIGINT);

        Long parentId = stepsTree.getParentId();
        if (parentId != null)
            batch.setLong(4, parentId);
        else
            batch.setNull(4, Types.BIGINT);

        batch.setInt(5, stepsTree.getOrd());
        batch.setBoolean(6, stepsTree.isRoot());

        batch.addBatch();
    }

    @Override
    public String getConfirmationMessage() {
        return null;
    }

    @Override
    public void setUp() throws SetupException {
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
    }

    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }
}
