--
-- Creates a view over the contact, contact_address and prisoner_contact tables
-- to return a list of active or inactive contacts, and their primary addresses,
-- for a prisoner.
-- Note: the view is only dropped if the checksum of this migration changes
--
DROP VIEW IF EXISTS v_contacts_with_primary_address;
CREATE VIEW v_contacts_with_primary_address
AS
select
    c.contact_id,
    c.created_by,
    c.created_time,
    c.date_of_birth,
    c.estimated_is_over_eighteen,
    c.first_name,
    c.last_name,
    c.middle_names,
    c.title,
    ca.contact_address_id,
    ca.address_type,
    ca.updated_by,
    ca.updated_time,
    ca.area,
    ca.city_code,
    city_ref.nomis_description as city_description,
    ca.county_code,
    county_ref.nomis_description as county_description,
    ca.country_code,
    country_ref.nomis_description as country_description,
    ca.flat,
    ca.post_code,
    ca.primary_address,
    ca.property,
    ca.street,
    ca.verified,
    ca.verified_by,
    ca.verified_time,
    ca.mail_flag,
    ca.start_date,
    ca.end_date,
    ca.no_fixed_address,
    ca.comments
  from contact c
  left join contact_address ca ON ca.contact_address_id = (
      select contact_address_id from contact_address ca1
      where ca1.contact_id = c.contact_id
      and end_date is null
      order by ca1.primary_address desc, ca1.mail_flag desc, ca1.start_date desc nulls last, ca1.created_time desc
      limit 1
  )
  left join city_reference city_ref on city_ref.nomis_code = ca.city_code
  left join county_reference county_ref on county_ref.nomis_code = ca.county_code
  left join country_reference country_ref on country_ref.nomis_code = ca.country_code;

-- End