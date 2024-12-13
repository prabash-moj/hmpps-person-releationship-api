--
-- After loading reference data with specific sequence values, the sequences
-- themselves need to be reset to the next number for generated ID values.
--

-- Reference data
alter sequence if exists reference_codes_reference_code_id_seq restart with 269;
alter sequence if exists nationality_reference_nationality_id_seq restart with 255;
alter sequence if exists language_reference_language_id_seq restart with 227;
alter sequence if exists country_reference_country_id_seq restart with 265;
alter sequence if exists county_reference_county_id_seq restart with 146;
alter sequence if exists city_reference_city_id_seq restart with 1730;

-- End