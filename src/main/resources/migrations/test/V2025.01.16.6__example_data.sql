-- ==========================================================
-- Example data
-- Not loaded into any real environments - DEV, PREPROD or PROD
-- Intended for integration tests and local-running only.
-- ===========================================================

insert into organisation (organisation_id, organisation_name, programme_number, vat_number, caseload_id, comments, created_by, created_time, updated_by, updated_time, active, deactivated_date)
values  (9900000, 'Name', 'P1', 'V1', 'C1', 'C2', 'Created by', '2025-01-16 17:28:11.261566', 'U1', '2025-01-16 18:08:11.261569', false, '2025-01-16');
