alter table contact_address alter column contact_address_id restart with (select max(contact_address_id)+1 from contact_address);

alter table contact_address_phone alter column contact_address_phone_id restart with (select max(contact_address_phone_id)+1 from contact_address_phone);

alter table contact_email alter column contact_email_id restart with (select max(contact_email_id)+1 from contact_email);

alter table contact_identity alter column contact_identity_id restart with (select max(contact_identity_id)+1 from contact_identity);

alter table contact_phone alter column contact_phone_id restart with (select max(contact_phone_id)+1 from contact_phone);

alter table contact_restriction alter column contact_restriction_id restart with (select max(contact_restriction_id)+1 from contact_restriction);

alter table reference_codes alter column reference_code_id restart with (select max(reference_code_id)+1 from reference_codes);

alter table prisoner_contact alter column prisoner_contact_id restart with (select max(prisoner_contact_id)+1 from prisoner_contact);
