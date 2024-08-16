-- =============================================
-- Reference data
-- =============================================

insert into contact(contact_id, contact_type_code, title, last_name, first_name, middle_name, date_of_birth, place_of_birth, gender, marital_status, language_code, comments, created_by)
values (1, 'SOCIAL', 'Mr', 'Last', 'Jack', 'Middle', '2000-11-21', 'London', 'Male', 'SINGLE', 'ENG', 'Comment', 'TIM'),
       (2, 'SOCIAL', 'Miss', 'Last', 'Jacqueline', 'Middle', '2000-11-22', 'London', 'Female', 'SINGLE', 'ENG', 'Comment', 'TIM'),
       (3, 'OFFICIAL', 'Mrs', 'Last', 'Jane', 'Middle', '2000-11-23', 'London', 'Female', 'SINGLE', 'ENG', 'Comment', 'TIM');

insert into contact_nationality(contact_nationality_id, contact_id, nationality_code, created_by)
values (1, 1, 'GB', 'TIM'),
       (2, 2, 'GB', 'TIM'),
       (3, 3, 'GB', 'TIM');

insert into contact_identity(contact_identity_id, contact_id, identity_type, identity_value, created_by)
values (1, 1, 'DRIVING_LIC', 'LAST-87736799M', 'TIM'),
       (2, 2, 'PASSPORT', 'PP87878787878', 'TIM'),
       (3, 3, 'NI_NUMBER', 'NI989989AA', 'TIM');

insert into contact_address(contact_address_id, contact_id, address_type, property, street, area, city_code, county_code, post_code, country_code, created_by)
values (1, 1, 'HOME', '24','Acacia Avenue', 'Bunting', 'SHEF', 'SYORKS', 'S2 3LK', 'UK', 'TIM'),
       (2, 2, 'HOME', '24','Acacia Avenue', 'Bunting', 'SHEF', 'SYORKS', 'S2 3LK', 'UK', 'TIM'),
       (3, 3, 'HOME', '24','Acacia Avenue', 'Bunting', 'SHEF', 'SYORKS', 'S2 3LK', 'UK', 'TIM');

insert into contact_phone(contact_phone_id, contact_id, phone_type, phone_number, created_by)
values (1, 1, 'MOBILE', '07878 111111', 'TIM'),
       (2, 2, 'MOBILE', '07878 222222', 'TIM'),
       (3, 3, 'MOBILE', '07878 222222', 'TIM');

insert into contact_email(contact_email_id, contact_id, email_type, email_address, created_by)
values (1, 1, 'PERSONAL', 'mr.last@hotmail.com', 'TIM'),
       (2, 2, 'PERSONAL', 'miss.last@hotmail.com', 'TIM'),
       (3, 3, 'PERSONAL', 'mrs.last@hotmail.com', 'TIM');

insert into reference_codes(reference_code_id, group_code, code, description, created_by)
values (1, 'CONTACT_TYPE', 'SOCIAL', 'Social contact', 'TIM'),
       (2, 'CONTACT_TYPE', 'OFFICIAL', 'Official contact', 'TIM'),
       (3, 'MARITAL_STS', 'MARRIED', 'Married', 'TIM'),
       (4, 'MARITAL_STS', 'SINGLE', 'Single', 'TIM'),
       (5, 'MARITAL_STS', 'COHABIT', 'Cohabiting', 'TIM'),
       (6, 'MARITAL_STS', 'CIVIL', 'Civil partnership', 'TIM'),
       (7, 'LANGUAGE', 'FR', 'French', 'TIM'),
       (8, 'LANGUAGE', 'EN', 'English', 'TIM'),
       (9, 'LANGUAGE', 'FAR', 'Farsi', 'TIM'),
       (10, 'NATIONALITY', 'GB', 'British', 'TIM'),
       (11, 'NATIONALITY', 'FRENCH', 'French', 'TIM'),
       (12, 'NATIONALITY', 'CHIN', 'Chinese', 'TIM'),
       (13, 'NATIONALITY', 'RUS', 'Russian', 'TIM'),
       (14, 'ID_TYPE', 'DRIVING_LIC', 'Driving licence', 'TIM'),
       (15, 'ID_TYPE', 'PASSPORT', 'Passport number', 'TIM'),
       (16, 'ID_TYPE', 'NI_NUMBER', 'National insurance number', 'TIM'),
       (17, 'ADDRESS_TYPE', 'HOME', 'Home address', 'TIM'),
       (18, 'ADDRESS_TYPE', 'WORK', 'Work address', 'TIM'),
       (19, 'ADDRESS_TYPE', 'TEMP', 'Temporary address', 'TIM'),
       (20, 'ADDRESS_TYPE', 'NONE', 'No fixed address', 'TIM'),
       (21, 'CITY', 'SHEF', 'Sheffield', 'TIM'),
       (22, 'COUNTY', 'SYORKS', 'South Yorkshire', 'TIM'),
       (23, 'COUNTRY', 'UK', 'United Kingdom', 'TIM'),
       (24, 'EMAIL_TYPE', 'WORK', 'Work email', 'TIM'),
       (25, 'EMAIL_TYPE', 'PERSONAL', 'Personal email', 'TIM'),
       (26, 'PHONE_TYPE', 'WORK', 'Work phone', 'TIM'),
       (27, 'PHONE_TYPE', 'HOME', 'Home phone', 'TIM'),
       (28, 'PHONE_TYPE', 'MOBILE', 'Mobile phone', 'TIM'),
       (29, 'RESTRICTION', 'CHILDREN', 'No children', 'TIM'),
       (30, 'RESTRICTION', 'ESCORTED', 'Must be escorted', 'TIM'),
       (31, 'RELATIONSHIP', 'FATHER', 'Father', 'TIM'),
       (32, 'RELATIONSHIP', 'MOTHER', 'Mother', 'TIM'),
       (33, 'RELATIONSHIP', 'FRIEND', 'Friend', 'TIM');

-- End