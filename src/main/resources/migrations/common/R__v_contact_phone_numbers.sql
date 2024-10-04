--
-- Creates a view over the contact_phone and reference_codes tables to return a list of all phone numbers with descriptions
-- for all codes.
--
CREATE OR REPLACE VIEW v_contact_phone_numbers
AS
select
    cp.contact_phone_id,
    cp.contact_id,
    cp.phone_type,
    rc.description as phone_type_description,
    cp.phone_number,
    cp.ext_number,
    cp.primary_phone,
    cp.created_by,
    cp.created_time,
    cp.amended_by,
    cp.amended_time
  from contact_phone cp
  left join reference_codes rc ON rc.group_code = 'PHONE_TYPE' and rc.code = cp.phone_type

-- End