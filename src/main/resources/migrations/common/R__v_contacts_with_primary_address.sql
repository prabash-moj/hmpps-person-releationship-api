--
-- Creates a view over the contact, contact_address and prisoner_contact tables
-- to return a list of active or inactive contacts, and their primary addresses,
-- for a prisoner.
--
CREATE OR REPLACE VIEW v_contacts_with_primary_address
AS
select
    c.contact_id,
    c.created_by,
    c.created_time,
    c.date_of_birth,
    c.estimated_is_over_eighteen,
    c.first_name,
    c.last_name,
    c.middle_name,
    c.title,
    ca.contact_address_id,
    ca.address_type,
    ca.amended_by,
    ca.amended_time,
    ca.area,
    ca.city_code,
    ca.country_code,
    ca.county_code,
    ca.flat,
    ca.post_code,
    ca.primary_address,
    ca.property,
    ca.street,
    ca.verified,
    ca.verified_by,
    ca.verified_time
  from contact c
  left join contact_address ca ON ca.contact_id = c.contact_id AND ca.primary_address = true

-- End