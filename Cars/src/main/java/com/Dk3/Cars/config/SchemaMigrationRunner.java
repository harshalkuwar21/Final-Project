package com.Dk3.Cars.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaMigrationRunner implements ApplicationRunner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SchemaMigrationRunner.class);
    private final JdbcTemplate jdbcTemplate;

    public SchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!tableExists("car")) {
            return;
        }

        // Newly introduced car-detail columns used by dashboard and user car-details page.
        ensureColumn("car", "mileage", "varchar(255) null");
        ensureColumn("car", "engine_cc", "varchar(255) null");
        ensureColumn("car", "safety_rating", "varchar(255) null");
        ensureColumn("car", "seating_capacity", "varchar(255) null");
        ensureColumn("car", "fuel_options", "varchar(255) null");
        ensureColumn("car", "transmission_options", "varchar(255) null");
        ensureColumn("car", "mileage_details", "text null");
        ensureColumn("car", "variant_details", "text null");
        ensureColumn("car", "color_options", "text null");
        ensureColumn("car", "review_score", "double null");
        ensureColumn("car", "review_exterior", "double null");
        ensureColumn("car", "review_performance", "double null");
        ensureColumn("car", "review_value", "double null");
        ensureColumn("car", "review_fuel_economy", "double null");
        ensureColumn("car", "review_comfort", "double null");
        ensureColumn("car", "faq_details", "text null");
    }

    private boolean tableExists(String table) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                table
        );
        return count != null && count > 0;
    }

    private void ensureColumn(String table, String column, String definition) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                    Integer.class,
                    table,
                    column
            );
            if (count == null || count == 0) {
                jdbcTemplate.execute("ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` " + definition);
                log.info("Added missing column {}.{}.", table, column);
            }
        } catch (Exception ex) {
            log.error("Failed to ensure column {}.{} - {}", table, column, ex.getMessage());
        }
    }
}
