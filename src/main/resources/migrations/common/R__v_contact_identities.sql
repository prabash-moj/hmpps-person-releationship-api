--
-- Creates a view over the contact_identity and reference_codes tables to return a list of all identities with descriptions
-- for all codes.
--
CREATE OR REPLACE VIEW v_contact_identities
AS
select
    ci.contact_identity_id,
    ci.contact_id,
    ci.identity_type,
    rc.description as identity_type_description,
    ci.identity_value,
    ci.issuing_authority,
    ci.verified,
    ci.verified_by,
    ci.verified_time,
    ci.created_by,
    ci.created_time,
    ci.amended_by,
    ci.amended_time
  from contact_identity ci
  left join reference_codes rc ON rc.group_code = 'ID_TYPE' and rc.code = ci.identity_type

-- End