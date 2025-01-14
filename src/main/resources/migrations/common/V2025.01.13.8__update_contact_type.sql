--
-- Updating off_relation reference data to official_relationship
--
ALTER TABLE prisoner_contact RENAME COLUMN relationship_type TO relationship_to_prisoner;
ALTER TABLE prisoner_contact RENAME COLUMN contact_type TO relationship_type;

update reference_codes set group_code = 'OFFICIAL_RELATIONSHIP' where group_code = 'OFF_RELATION';
update reference_codes set group_code = 'SOCIAL_RELATIONSHIP' where group_code = 'RELATIONSHIP';
update reference_codes set group_code = 'RELATIONSHIP_TYPE' where group_code = 'CONTACT_TYPE';

-- End