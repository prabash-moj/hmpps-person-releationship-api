-- Test / example data
alter table contact_address alter column contact_address_id restart with (select max(contact_address_id)+1 from contact_address);
alter table contact_address_phone alter column contact_address_phone_id restart with (select max(contact_address_phone_id)+1 from contact_address_phone);
alter table contact_email alter column contact_email_id restart with (select max(contact_email_id)+1 from contact_email);
alter table contact_identity alter column contact_identity_id restart with (select max(contact_identity_id)+1 from contact_identity);
alter table contact_phone alter column contact_phone_id restart with (select max(contact_phone_id)+1 from contact_phone);
alter table contact_restriction alter column contact_restriction_id restart with (select max(contact_restriction_id)+1 from contact_restriction);
alter table prisoner_contact alter column prisoner_contact_id restart with (select max(prisoner_contact_id)+1 from prisoner_contact);

-- Reference data
alter table reference_codes alter column reference_code_id restart with (select max(reference_code_id)+1 from reference_codes);
alter table nationality_reference alter column nationality_id restart with (select max(nationality_id)+1 from nationality_reference);
alter table language_reference alter column language_id restart with (select max(language_id)+1 from language_reference);
alter table country_reference alter column country_id restart with (select max(country_id)+1 from country_reference);
alter table county_reference alter column county_id restart with (select max(county_id)+1 from county_reference);
alter table city_reference alter column city_id restart with (select max(city_id)+1 from city_reference);

-- End