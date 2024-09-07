alter table contact alter column contact_id restart with 29;

alter table contact_address alter column contact_address_id restart with 13;

alter table contact_email alter column contact_email_id restart with 4;

alter table contact_identity alter column contact_identity_id restart with 4;

alter table contact_phone alter column contact_phone_id restart with 12;

alter table reference_codes alter column reference_code_id restart with 38;

alter table prisoner_contact alter column prisoner_contact_id restart with 29;
