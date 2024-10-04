--
-- Creates a view over the contact_address and reference data tables to return a list of all addresses with descriptions
-- for all codes.
--
CREATE OR REPLACE VIEW v_contact_addresses
AS
select
    ca.contact_address_id,
    ca.contact_id,
    ca.address_type,
    rc.description as address_type_description,
    ca.primary_address,
    ca.flat,
    ca.property,
    ca.street,
    ca.area,
    ca.city_code,
    city_ref.nomis_description as city_description,
    ca.county_code,
    county_ref.nomis_description as county_description,
    ca.post_code,
    ca.country_code,
    country_ref.nomis_description as country_description,
    ca.verified,
    ca.verified_by,
    ca.verified_time,
    ca.mail_flag,
    ca.start_date,
    ca.end_date,
    ca.no_fixed_address,
    ca.created_by,
    ca.created_time,
    ca.amended_by,
    ca.amended_time
  from contact_address ca
  left join city_reference city_ref on city_ref.nomis_code = ca.city_code
  left join county_reference county_ref on county_ref.nomis_code = ca.county_code
  left join country_reference country_ref on country_ref.nomis_code = ca.country_code
  left join reference_codes rc ON rc.group_code = 'ADDRESS_TYPE' and rc.code = ca.address_type

-- End