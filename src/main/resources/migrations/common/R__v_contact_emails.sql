--
-- Creates a view over the contact_email and reference_codes tables to return a list of all phone numbers with descriptions
-- for all codes.
--
CREATE OR REPLACE VIEW v_contact_emails
AS
select
    ce.contact_email_id,
    ce.contact_id,
    ce.email_type,
    rc.description as email_type_description,
    ce.email_address,
    ce.primary_email,
    ce.created_by,
    ce.created_time,
    ce.amended_by,
    ce.amended_time
  from contact_email ce
  left join reference_codes rc ON rc.group_code = 'EMAIL_TYPE' and rc.code = ce.email_type

-- End