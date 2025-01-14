--
-- Removing contact_person_name as this is actually on the address rather than organisation. All values are null in NOMIS.
--
ALTER TABLE organisation DROP COLUMN contact_person_name;

--
-- Removing referential integrity on the employment table. This is to allow for the initial migration of contacts
-- and organisations to happen in any order.
--
ALTER TABLE employment DROP CONSTRAINT IF EXISTS employment_organisation_id_fkey;
ALTER TABLE employment DROP CONSTRAINT IF EXISTS employment_contact_id_fkey;

--
-- Rename primary key column of organisation_web_address
--
ALTER TABLE organisation_web_address DROP CONSTRAINT organisation_web_id_pk;
ALTER TABLE organisation_web_address RENAME COLUMN organisation_web_id TO organisation_web_address_id;
ALTER TABLE organisation_web_address ADD CONSTRAINT organisation_web_address_id_pk PRIMARY KEY (organisation_web_address_id) ;

-- End