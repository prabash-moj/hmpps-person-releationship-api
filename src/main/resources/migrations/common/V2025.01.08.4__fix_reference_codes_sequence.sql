-- =============================================
-- There were 2048 items loaded with the initial reference data and this sequence was reset to 269 incorrectly.
-- Reset the sequence to match current values.
-- =============================================
alter sequence if exists reference_codes_reference_code_id_seq restart with 2409;

-- End