package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import java.sql.Connection;
import java.sql.SQLException;

/** Additive alignment for the original design SQL, explicitly baselined locally at V1. */
public class V2__align_original_account_schema extends BaseJavaMigration {
    @Override public void migrate(Context context) throws Exception {
        var connection=context.getConnection();
        if(column(connection,"account_identities","status") && !column(connection,"account_identities","account_status"))
            execute(connection,"ALTER TABLE account_identities RENAME COLUMN status TO account_status");
        if(!column(connection,"account_identities","token_version"))
            execute(connection,"ALTER TABLE account_identities ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0");
        if(!column(connection,"account_profiles","avatar"))
            execute(connection,"ALTER TABLE account_profiles ADD COLUMN avatar VARCHAR(2048) NOT NULL DEFAULT ''");
        if(!column(connection,"account_profiles","current_location"))
            execute(connection,"ALTER TABLE account_profiles ADD COLUMN current_location VARCHAR(255) NOT NULL DEFAULT ''");
        for(var role:new String[]{"ROLE_USER","ROLE_ADMIN"}) {
            try(var insert=connection.prepareStatement("INSERT INTO roles(role_name) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name=?)")) {
                insert.setString(1,role); insert.setString(2,role); insert.executeUpdate();
            }
        }
    }
    private static boolean column(Connection connection,String table,String name) throws SQLException {
        try(var result=connection.getMetaData().getColumns(connection.getCatalog(),null,"%","%")) {
            while(result.next()) if(table.equalsIgnoreCase(result.getString("TABLE_NAME")) && name.equalsIgnoreCase(result.getString("COLUMN_NAME"))) return true;
        }
        return false;
    }
    private static void execute(Connection connection,String sql) throws SQLException {
        try(var statement=connection.createStatement()) { statement.execute(sql); }
    }
}
