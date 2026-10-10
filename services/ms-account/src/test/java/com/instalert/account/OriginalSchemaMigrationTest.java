package com.instalert.account;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import static org.assertj.core.api.Assertions.*;

class OriginalSchemaMigrationTest {
    @Test void preservesOriginalStatusConstraintAndRowsDuringAlignment() throws Exception {
        String url="jdbc:h2:mem:original-schema;MODE=MySQL;DB_CLOSE_DELAY=-1";
        try(var connection=DriverManager.getConnection(url,"sa",""); var statement=connection.createStatement()) {
            statement.execute("CREATE TABLE account_identities(identity_id BIGINT PRIMARY KEY, account_status VARCHAR(20) NOT NULL, CONSTRAINT ck_account_status CHECK(account_status IN ('ACTIVE','DISABLED')))");
            statement.execute("CREATE TABLE account_profiles(profile_id BIGINT PRIMARY KEY, display_name VARCHAR(120))");
            statement.execute("CREATE TABLE roles(role_id SMALLINT AUTO_INCREMENT PRIMARY KEY, role_name VARCHAR(50) UNIQUE)");
            statement.execute("INSERT INTO account_identities VALUES (7,'DISABLED')");
            statement.execute("INSERT INTO account_profiles VALUES (42,'Existing profile')");
        }
        Flyway.configure().dataSource(url,"sa","").baselineOnMigrate(true).baselineVersion("1").load().migrate();
        try(var connection=DriverManager.getConnection(url,"sa",""); var statement=connection.createStatement()) {
            try(var identity=statement.executeQuery("SELECT account_status,token_version FROM account_identities WHERE identity_id=7")) {
                assertThat(identity.next()).isTrue(); assertThat(identity.getString(1)).isEqualTo("DISABLED"); assertThat(identity.getLong(2)).isZero();
            }
            try(var profile=statement.executeQuery("SELECT display_name,avatar,current_location FROM account_profiles WHERE profile_id=42")) {
                assertThat(profile.next()).isTrue(); assertThat(profile.getString(1)).isEqualTo("Existing profile");
                assertThat(profile.getString(2)).isEmpty(); assertThat(profile.getString(3)).isEmpty();
            }
            assertThatThrownBy(()->statement.execute("UPDATE account_identities SET account_status='INVALID' WHERE identity_id=7")).isInstanceOf(java.sql.SQLException.class);
        }
    }
}
