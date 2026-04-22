package com.smart_campus_hub.smart_campus_api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingSchemaMigration implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingSchemaMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public BookingSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        if (!tableExists("bookings") || !tableExists("booking_resources")) {
            return;
        }

        String referencedTable = querySingleString("""
                SELECT kcu.REFERENCED_TABLE_NAME
                FROM information_schema.KEY_COLUMN_USAGE kcu
                WHERE kcu.TABLE_SCHEMA = DATABASE()
                  AND kcu.TABLE_NAME = 'bookings'
                  AND kcu.COLUMN_NAME = 'resource_id'
                  AND kcu.REFERENCED_TABLE_NAME IS NOT NULL
                LIMIT 1
                """);

        if ("booking_resources".equalsIgnoreCase(referencedTable)) {
            return;
        }

        String fkName = querySingleString("""
                SELECT kcu.CONSTRAINT_NAME
                FROM information_schema.KEY_COLUMN_USAGE kcu
                WHERE kcu.TABLE_SCHEMA = DATABASE()
                  AND kcu.TABLE_NAME = 'bookings'
                  AND kcu.COLUMN_NAME = 'resource_id'
                  AND kcu.REFERENCED_TABLE_NAME IS NOT NULL
                LIMIT 1
                """);

        try {
            if (fkName != null && !fkName.isBlank()) {
                jdbcTemplate.execute("ALTER TABLE bookings DROP FOREIGN KEY `" + fkName + "`");
            }

            jdbcTemplate.execute("ALTER TABLE bookings MODIFY COLUMN resource_id BIGINT NOT NULL");
            jdbcTemplate.execute("""
                    ALTER TABLE bookings
                    ADD CONSTRAINT fk_bookings_resource
                    FOREIGN KEY (resource_id) REFERENCES booking_resources(resource_id)
                    """);

            LOGGER.info("Applied bookings.resource_id foreign key migration to booking_resources.");
        } catch (Exception ex) {
            LOGGER.warn("Could not auto-migrate bookings.resource_id foreign key: {}", ex.getMessage());
        }
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName);
        return count != null && count > 0;
    }

    private String querySingleString(String sql) {
        List<String> values = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1));
        if (values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }
}
