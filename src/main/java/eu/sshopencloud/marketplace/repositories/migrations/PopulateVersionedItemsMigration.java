package eu.sshopencloud.marketplace.repositories.migrations;

import eu.sshopencloud.marketplace.model.items.PersistentId;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;


public class PopulateVersionedItemsMigration implements CustomTaskChange {

    @Override
    public void execute(Database database) throws CustomChangeException {
        JdbcConnection connection = (JdbcConnection) database.getConnection();

        String versionedItemInsert = "insert into versioned_items (id) values (?)";
        String itemUpdate = "update items set versioned_item_id = ? where id = ?";

        try (
                Statement itemsSelect = connection.createStatement();
                PreparedStatement versionedItemsBatch = connection.prepareStatement(versionedItemInsert);
                PreparedStatement itemsBatch = connection.prepareStatement(itemUpdate)
        ) {
            ResultSet itemIds = itemsSelect.executeQuery("select id from items");
            Set<String> persistentIds = new HashSet<>();

            while (itemIds.next()) {
                long itemId = itemIds.getLong("id");
                String persistentId = PersistentId.generated();

                while (persistentIds.contains(persistentId))
                    persistentId = PersistentId.generated();

                persistentIds.add(persistentId);

                versionedItemsBatch.setString(1, persistentId);
                versionedItemsBatch.addBatch();

                itemsBatch.setString(1, persistentId);
                itemsBatch.setLong(2, itemId);
                itemsBatch.addBatch();
            }

            versionedItemsBatch.executeBatch();
            itemsBatch.executeBatch();
        }
        catch (SQLException | DatabaseException e) {
            throw new CustomChangeException(e);
        }
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
