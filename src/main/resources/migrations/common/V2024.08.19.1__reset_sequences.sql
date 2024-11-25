--
-- After loading reference data with specific sequence values, the sequences
-- themselves need to be reset to the next number for generated ID values.
--

-- Contact ID sequence generator - generates in the range 20million+ (not to clash with NOMIS PERSON_ID)
CREATE SEQUENCE CONTACT_ID_SEQ START WITH 20000000 INCREMENT BY 1;

-- Implied sequences for JPA GenerationType.IDENTITY
-- alter sequence if exists contact_contact_id_seq restart with 22;
alter sequence if exists contact_address_contact_address_id_seq restart with 18;
alter sequence if exists contact_email_contact_email_id_seq restart with 5;
alter sequence if exists contact_identity_contact_identity_id_seq restart with 5;
alter sequence if exists contact_phone_contact_phone_id_seq restart with 13;
alter sequence if exists reference_codes_reference_code_id_seq restart with 82;
alter sequence if exists prisoner_contact_prisoner_contact_id_seq restart with 30;
alter sequence if exists contact_restriction_contact_restriction_id_seq restart with 5;
alter sequence if exists contact_address_phone_contact_address_phone_id_seq restart with 2;
alter sequence if exists prisoner_contact_restriction_prisoner_contact_restriction_id_seq restart with 6;

-- End