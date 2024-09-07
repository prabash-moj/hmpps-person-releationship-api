-- =============================================
-- Reference data
-- =============================================

insert into contact(contact_id, contact_type_code, title, last_name, first_name, middle_name, date_of_birth, place_of_birth, gender, marital_status, language_code, comments, created_by, active)
values (1, 'SOCIAL',   'Mr',   'Last',   'Jack',       'Middle', '2000-11-21', 'London', 'Male',   'SINGLE', 'ENG', 'Comment', 'TIM', true),
       (2, 'SOCIAL',   'Miss', 'Last',   'Jacqueline', 'Middle', '2000-11-22', 'London', 'Female', 'SINGLE', 'ENG', 'Comment', 'TIM', true),
       (3, 'OFFICIAL', 'Mrs', 'Last',    'Jane',       'Middle', '2000-11-23', 'London', 'Male',   'SINGLE', 'ENG', 'Comment', 'TIM', true),
       (4, 'SOCIAL',   'Mr',   'Four',   'John',       'Middle', '2000-05-18', 'London', 'Male',   'SINGLE', 'ENG', 'Comment', 'TIM', true),
       (5, 'SOCIAL',   'Mr',   'Five',   'Jon',        'Middle', '2000-09-21', 'London', 'Male',   'SINGLE', 'ENG', 'Comment', 'TIM', true),
       (6, 'SOCIAL',   'Mr',   'Six',    'Johnny',     'Middle', '2000-10-23', 'London', 'Male',   'SINGLE', 'ENG', 'Comment', 'TIM', true),
       (7, 'SOCIAL',   'Mr',   'Seven',  'Pete',       'Middle', '2015-08-29', 'London', 'Male',   'SINGLE', 'ENG', 'Comment', 'TIM', true),
       (8, 'SOCIAL',   'Mr',   'Eight',  'Harry',      'Middle', '2015-12-23', 'London', 'Male',   'SINGLE', 'ENG', 'Comment', 'TIM', true),
       (9, 'SOCIAL',   'Mr',   'Nine',   'Donald',     'Middle', '2000-11-23', 'London', 'Male',   'SINGLE', 'ENG', 'Comment', 'TIM', true),
       (10, 'SOCIAL',  'Ms',   'Ten',    'Freya',      'Middle', '2000-11-24', 'London', 'Female', 'SINGLE', 'ENG', 'Comment', 'TIM', true),
       (11, 'SOCIAL',  'Ms',   'Eleven', 'Suki',       'Middle', '2000-11-25', 'London', 'Female', 'SINGLE', 'ENG', 'Comment', 'TIM', false),
       (12, 'SOCIAL',  'Mrs',  'Twelve', 'Jane',       'Middle', '2000-11-26', 'London', 'Female', 'SINGLE', 'ENG', 'Comment', 'TIM', false);

insert into contact_identity(contact_identity_id, contact_id, identity_type, identity_value, created_by)
values (1, 1, 'DRIVING_LIC', 'LAST-87736799M', 'TIM'),
       (2, 2, 'PASSPORT', 'PP87878787878', 'TIM'),
       (3, 3, 'NI_NUMBER', 'NI989989AA', 'TIM');

insert into contact_address(contact_address_id, contact_id, address_type, primary_address, flat, property, street, area, city_code, county_code, post_code, country_code, created_by)
values (1,  1,  'HOME', true, null, '24','Acacia Avenue', 'Bunting', 'SHEF', 'SYORKS', 'S2 3LK', 'UK', 'TIM'),
       (2,  2,  'HOME', true, null, '24','Acacia Avenue', 'Bunting', 'SHEF', 'SYORKS', 'S2 3LK', 'UK', 'TIM'),
       (3,  3,  'HOME', true, null, '24','Acacia Avenue', 'Bunting', 'SHEF', 'SYORKS', 'S2 3LK', 'UK', 'TIM'),
       (4,  4,  'HOME', true, null, '26','Acacia Avenue', 'Bunting', 'SHEF', 'SYORKS', 'S2 3LK', 'UK', 'TIM'),
       (5,  5,  'HOME', true, null, '28','Acacia Avenue', 'Bunting', 'SHEF', 'SYORKS', 'S2 3LK', 'UK', 'TIM'),
       (6,  6,  'HOME', true, null, '30','Acacia Avenue', 'Bunting', 'SHEF', 'SYORKS', 'S2 3LK', 'UK', 'TIM'),
       (7,  7,  'HOME', true, null, '32','Acacia Avenue', 'Bunting', 'SHEF', 'SYORKS', 'S2 3LK', 'UK', 'TIM'),
       (8,  8,  'HOME', true, null, '34','Acacia Avenue', 'Bunting', 'SHEF', 'SYORKS', 'S2 3LK', 'UK', 'TIM'),
       (9,  9,  'HOME', true, null, '36','Acacia Avenue', 'Bunting', 'SHEF', 'SYORKS', 'S2 3LK', 'UK', 'TIM'),
       (10, 10, 'HOME', true, null, '38','Acacia Avenue', 'Bunting', 'SHEF', 'SYORKS', 'S2 3LK', 'UK', 'TIM'),
       (11, 11, 'HOME', true, null, '40','Acacia Avenue', 'Bunting', 'SHEF', 'SYORKS', 'S2 3LK', 'UK', 'TIM'),
       (12, 12, 'HOME', true, 'Flat 3b', '42','Acacia Avenue', 'Bunting', 'SHEF', 'SYORKS', 'S2 3LK', 'UK', 'TIM');


insert into contact_phone(contact_phone_id, contact_id, phone_type, phone_number, primary_phone, created_by)
values (1, 1, 'MOBILE', '07878 111111', true, 'TIM'),
       (2, 2, 'MOBILE', '07878 222222', true, 'TIM'),
       (3, 3, 'MOBILE', '07878 222222', false, 'TIM'),
       (4, 4, 'MOBILE', '07878 222222', true, 'TIM'),
       (5, 5, 'MOBILE', '07878 222222', true, 'TIM'),
       (6, 6, 'HOME', '07878 222222', true, 'TIM'),
       (7, 7, 'HOME', '07878 222222', true, 'TIM'),
       (8, 8, 'MOBILE', '07878 222222', true, 'TIM'),
       (9, 9, 'WORK', '07878 222222', true, 'TIM'),
       (10, 10, 'MOBILE', '07878 222222', true, 'TIM'),
       (11, 11, 'MOBILE', '07878 222222', true, 'TIM');

insert into contact_email(contact_email_id, contact_id, email_type, email_address, primary_email, created_by)
values (1, 1, 'PERSONAL', 'mr.last@hotmail.com', true, 'TIM'),
       (2, 2, 'PERSONAL', 'miss.last@hotmail.com', true,  'TIM'),
       (3, 3, 'PERSONAL', 'mrs.last@hotmail.com', false, 'TIM');

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
       (33, 'RELATIONSHIP', 'FRIEND', 'Friend', 'TIM'),
       (34, 'RELATIONSHIP', 'SISTER', 'Sister', 'TIM'),
       (35, 'RELATIONSHIP', 'UNCLE', 'Brother', 'TIM'),
       (36, 'RELATIONSHIP', 'BROTHER', 'Brother', 'TIM'),
       (37, 'RELATIONSHIP', 'GIRLFRIEND', 'Girlfriend', 'TIM');

insert into prisoner_contact (prisoner_contact_id, contact_id, prisoner_number, active, relationship_type, comments, created_at_prison, created_by, created_time)
values (1, 1, 'A1234BB', true, 'FATHER', 'Comment', 'MDI', 'TIM', current_timestamp),
       -- Jason Mitchell
       (2,  1,  'A8185DY', true, 'FATHER', 'Comment', 'MDI', 'TIM', current_timestamp),
       (3,  6,  'A8185DY', true, 'FRIEND', 'Comment', 'MDI', 'TIM', current_timestamp),
       (4,  7,  'A8185DY', true, 'FRIEND', 'Comment', 'MDI', 'TIM', current_timestamp),
       (5,  8,  'A8185DY', true, 'FRIEND', 'Comment', 'MDI', 'TIM', current_timestamp),
       (6,  10, 'A8185DY', true, 'FRIEND', 'Comment', 'MDI', 'TIM', current_timestamp),
       (7,  11, 'A8185DY', true, 'FRIEND', 'Comment', 'MDI', 'TIM', current_timestamp),
       -- Tim Harrison
       (8,  1,  'G4793VF', true,  'FATHER', 'Comment', 'MDI', 'TIM', current_timestamp),
       (9,  2,  'G4793VF', true,  'MOTHER', 'Comment', 'MDI', 'TIM', current_timestamp),
       (10, 3,  'G4793VF', true,  'SISTER', 'Comment', 'MDI', 'TIM', current_timestamp),
       (11, 4,  'G4793VF', true,  'BROTHER', 'Comment', 'MDI', 'TIM', current_timestamp),
       (12, 5,  'G4793VF', true,  'FRIEND', 'Comment', 'MDI', 'TIM', current_timestamp),
       (13, 6,  'G4793VF', true,  'FRIEND', 'Comment', 'MDI', 'TIM', current_timestamp),
       (14, 7,  'G4793VF', true,  'FRIEND', 'Comment', 'MDI', 'TIM', current_timestamp),
       (15, 8,  'G4793VF', true,  'UNCLE', 'Comment', 'MDI', 'TIM', current_timestamp),
       (16, 9,  'G4793VF', true,  'UNCLE', 'Comment', 'MDI', 'TIM', current_timestamp),
       (17, 10, 'G4793VF', true, 'GIRLFRIEND', 'Comment', 'MDI', 'TIM', current_timestamp),
       (18, 11, 'G4793VF', false, 'GIRLFRIEND', 'Comment', 'MDI', 'TIM', current_timestamp),
       (19, 12, 'G4793VF', false, 'GIRLFRIEND', 'Comment', 'MDI', 'TIM', current_timestamp),
       -- Tim Cooks
       (20, 1, 'A4162DZ', true, 'FATHER', 'Comment', 'MDI', 'TIM', current_timestamp),
       (21, 2, 'A4162DZ', true, 'FRIEND', 'Comment', 'MDI', 'TIM', current_timestamp),
       (22, 3, 'A4162DZ', true, 'FRIEND', 'Comment', 'MDI', 'TIM', current_timestamp),
       (23, 4, 'A4162DZ', true, 'FRIEND', 'Comment', 'MDI', 'TIM', current_timestamp),
       (24, 5, 'A4162DZ', true, 'FRIEND', 'Comment', 'MDI', 'TIM', current_timestamp),
       (25, 10, 'A4162DZ', true, 'MOTHER', 'Comment', 'MDI', 'TIM', current_timestamp),
       -- Justin Timberlake
       (26, 1, 'A5166DY', true, 'FATHER', 'Comment', 'MDI', 'TIM', current_timestamp),
       -- Mike Toby
       (27, 1, 'A4385DZ', true, 'FATHER', 'Comment', 'MDI', 'TIM', current_timestamp),
       (28, 10, 'A4385DZ', true, 'MOTHER', 'Comment', 'MDI', 'TIM', current_timestamp);
-- End