package com.hareeshvar.attendance.flyway;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FreshDatabaseFlywayTest {

    @Test
    @DisplayName("Scenario B: Verify Flyway creates complete schema on a fresh database")
    void testFreshDatabaseMigration() {
        String freshDbUrl = "jdbc:mysql://localhost:3306/attendance_system_fresh_test?createDatabaseIfNotExist=true";

        DataSource dataSource = DataSourceBuilder.create()
                .url(freshDbUrl)
                .username("root")
                .password("hareesh33@mi")
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(false)
                .cleanDisabled(false)
                .load();

        flyway.clean();
        var result = flyway.migrate();

        System.out.println("FRESH DB MIGRATION SUCCESSFUL: Applied " + result.migrationsExecuted + " migrations.");
        assertTrue(result.migrationsExecuted > 0, "Flyway should execute V1 baseline script on a fresh database");
    }
}
