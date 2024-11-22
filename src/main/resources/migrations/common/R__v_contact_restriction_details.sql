--
-- Creates a view over the contact_restriction and reference data tables to return a list of estate wide restrictions by
-- contact_id
--
CREATE OR REPLACE VIEW v_contact_restriction_details
AS
select
    cr.contact_restriction_id,
    cr.contact_id,
    cr.restriction_type,
    rc.description as restriction_type_description,
    cr.start_date,
    cr.expiry_date,
    cr.comments,
    cr.staff_username,
    cr.created_by,
    cr.created_time,
    cr.amended_by,
    cr.amended_time
  from contact_restriction cr
  left join reference_codes rc ON rc.group_code = 'RESTRICTION' and rc.code = cr.restriction_type

-- End