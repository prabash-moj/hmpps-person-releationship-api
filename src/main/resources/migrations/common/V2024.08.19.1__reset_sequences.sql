--
-- After loading reference data with specific sequence values, the sequences
-- themselves need to be reset to the next number for generated ID values.
--
alter sequence if exists contact_contact_id_seq restart with (select max(contact_id)+1 from contact);
alter sequence if exists contact_address_contact_address_id_seq restart with (select max(contact_address_id)+1 from contact_address);
alter sequence if exists contact_email_contact_email_id_seq restart with (select max(contact_email_id)+1 from contact_email) ;
alter sequence if exists contact_identity_contact_identity_id_seq restart with (select max(contact_identity_id)+1 from contact_identity);
alter sequence if exists contact_phone_contact_phone_id_seq restart with (select max(contact_phone_id)+1 from contact_phone);
alter sequence if exists reference_codes_reference_code_id_seq restart with (select max(reference_code_id)+1 from reference_codes);
alter sequence if exists prisoner_contact_prisoner_contact_id_seq restart with (select max(prisoner_contact_id)+1 from prisoner_contact);

-- End