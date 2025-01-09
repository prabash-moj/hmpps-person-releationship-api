--
-- Removing estimated DOB as this is not required or something that exists in NOMIS
--

ALTER TABLE contact DROP COLUMN estimated_is_over_eighteen;

-- End