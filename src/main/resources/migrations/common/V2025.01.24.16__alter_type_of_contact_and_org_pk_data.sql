--
-- The two were incorrectly set as integers despite being modelled as longs in Kotlin allowing integer overflow.
--
ALTER TABLE contact ALTER COLUMN contact_id TYPE BIGINT;
ALTER TABLE organisation ALTER COLUMN organisation_id TYPE BIGINT;

-- End