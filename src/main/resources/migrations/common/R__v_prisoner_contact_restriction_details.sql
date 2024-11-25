--
-- Creates a view over the prisoner_contact_restriction and reference data tables to return a list of estate wide restrictions by
-- contact_id
--
CREATE OR REPLACE VIEW v_prisoner_contact_restriction_details
AS
select
    pcr.prisoner_contact_restriction_id,
    pcr.prisoner_contact_id,
    pcr.restriction_type,
    rc.description as restriction_type_description,
    pcr.start_date,
    pcr.expiry_date,
    pcr.comments,
    pcr.staff_username,
    pcr.created_by,
    pcr.created_time,
    pcr.amended_by,
    pcr.amended_time
  from prisoner_contact_restriction pcr
  left join reference_codes rc ON rc.group_code = 'RESTRICTION' and rc.code = pcr.restriction_type

-- End