--
-- Removing estimated DOB as this is not required or something that exists in NOMIS.
-- Dropping dependent views as their migrations will be re-run after versioned migrations.
--

DROP VIEW IF EXISTS v_prisoner_contacts;
DROP VIEW IF EXISTS v_contacts_with_primary_address;

ALTER TABLE contact DROP COLUMN estimated_is_over_eighteen;

-- End