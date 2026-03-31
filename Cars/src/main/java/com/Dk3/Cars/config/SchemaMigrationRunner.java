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

        if (tableExists("showroom")) {
            ensureColumn("showroom", "address", "varchar(255) null");
            ensureColumn("showroom", "contact_number", "varchar(255) null");
            ensureColumn("showroom", "showroom_type", "varchar(255) null");
            ensureColumn("showroom", "map_url", "varchar(1000) null");
            ensureColumn("showroom", "working_hours", "varchar(255) null");
            ensureColumn("showroom", "manager_name", "varchar(255) null");
            ensureColumn("showroom", "image_url", "varchar(1000) null");
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

        if (tableExists("booking")) {
            ensureColumn("booking", "road_tax_amount", "double null");
            ensureColumn("booking", "fastag_charges", "double null");
            ensureColumn("booking", "handling_charges", "double null");
            ensureColumn("booking", "extended_warranty_amount", "double null");
            ensureColumn("booking", "tcs_amount", "double null");
            ensureColumn("booking", "down_payment_verified", "bit null");
            ensureColumn("booking", "pre_verification_status", "varchar(255) null");
            ensureColumn("booking", "customer_name_matched", "bit null");
            ensureColumn("booking", "pre_verification_remarks", "text null");
            ensureColumn("booking", "pre_verified_by", "varchar(255) null");
            ensureColumn("booking", "pre_verified_at", "datetime(6) null");
            ensureColumn("booking", "insurance_company_name", "varchar(255) null");
            ensureColumn("booking", "insurance_policy_number", "varchar(255) null");
            ensureColumn("booking", "insurance_document_url", "varchar(1000) null");
            ensureColumn("booking", "insurance_generated_at", "datetime(6) null");
            ensureColumn("booking", "form20_submitted", "bit null");
            ensureColumn("booking", "form21_submitted", "bit null");
            ensureColumn("booking", "form22_submitted", "bit null");
            ensureColumn("booking", "invoice_submitted_to_rto", "bit null");
            ensureColumn("booking", "insurance_submitted_to_rto", "bit null");
            ensureColumn("booking", "rto_authority", "varchar(255) null");
            ensureColumn("booking", "rto_application_status", "varchar(255) null");
            ensureColumn("booking", "temporary_registration_number", "varchar(255) null");
            ensureColumn("booking", "temporary_registration_url", "varchar(1000) null");
            ensureColumn("booking", "rto_applied_at", "datetime(6) null");
            ensureColumn("booking", "original_documents_verified", "bit null");
            ensureColumn("booking", "physical_verification_done", "bit null");
            ensureColumn("booking", "delivery_note_signed", "bit null");
            ensureColumn("booking", "delivery_completed_at", "datetime(6) null");
            ensureColumn("booking", "final_invoice_url", "varchar(1000) null");
            ensureColumn("booking", "warranty_document_url", "varchar(1000) null");
            ensureColumn("booking", "loan_document_url", "varchar(1000) null");
        }
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
