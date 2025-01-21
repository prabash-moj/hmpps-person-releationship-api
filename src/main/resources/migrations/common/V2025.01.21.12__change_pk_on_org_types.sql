--
-- Changing primary key to just be organisation_id and organisation_type
--
ALTER TABLE organisation_type DROP CONSTRAINT organisation_type_pkey;
ALTER TABLE organisation_type DROP COLUMN organisation_type_id;
ALTER TABLE organisation_type ADD CONSTRAINT organisation_type_pkey PRIMARY KEY (organisation_id, organisation_type);

-- End