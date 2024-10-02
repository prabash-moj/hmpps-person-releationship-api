--
-- After loading reference data with specific sequence values, the sequences
-- themselves need to be reset to the next number for generated ID values.
--
alter sequence if exists contact_contact_id_seq restart with 20;
alter sequence if exists contact_address_contact_address_id_seq restart with 17;
alter sequence if exists contact_email_contact_email_id_seq restart with 4;
alter sequence if exists contact_identity_contact_identity_id_seq restart with 4;
alter sequence if exists contact_phone_contact_phone_id_seq restart with 12;
alter sequence if exists reference_codes_reference_code_id_seq restart with 82;
alter sequence if exists prisoner_contact_prisoner_contact_id_seq restart with 30;
alter sequence if exists contact_restriction_contact_restriction_id_seq restart with 4;

-- End