--
-- CONTACT DB TABLES
--

---------------------------------------------------------------------------------------
-- This is the core table for contacts - and holds the details of people who may
-- visit, or are related to, people in prison.
----------------------------------------------------------------------------------------

CREATE TABLE contact
(
    contact_id bigserial NOT NULL CONSTRAINT contact_id_pk PRIMARY KEY,
    title varchar(12),
    last_name  varchar(35) NOT NULL,
    first_name varchar(35) NOT NULL,
    middle_names varchar(35),
    date_of_birth date,
    estimated_is_over_eighteen varchar(11),
    place_of_birth varchar(25),
    active boolean NOT NULL default true,
    suspended boolean NOT NULL DEFAULT false,
    staff_flag boolean NOT NULL DEFAULT false,
    remitter_flag boolean NOT NULL default false,
    deceased_flag boolean NOT NULL DEFAULT false,
    deceased_date date,
    coroner_number varchar(32),
    gender varchar(12),
    domestic_status varchar(12), -- Reference codes - DOMESTIC_STS - nullable
    language_code varchar(12), -- Reference codes - LANGUAGE - nullable
    nationality_code varchar(12), -- Reference data - NATIONALITY - nullable
    interpreter_required boolean NOT NULL DEFAULT false,
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    amended_by varchar(100),
    amended_time timestamp
);

CREATE INDEX idx_contact_last_name ON contact(last_name);
CREATE INDEX idx_contact_first_name ON contact(first_name);
CREATE INDEX idx_contact_date_of_birth ON contact(date_of_birth);

---------------------------------------------------------------------------------------
-- Contacts need to provide one or more forms of ID.
-- This table holds the details of the ID provided.
-- e.g. Passport number, driving licence, national insurance number.
-- There may be no proof of ID provided.
----------------------------------------------------------------------------------------

CREATE TABLE contact_identity
(
    contact_identity_id bigserial NOT NULL CONSTRAINT contact_identity_id_pk PRIMARY KEY,
    contact_id bigint NOT NULL REFERENCES contact(contact_id),
    identity_type varchar(12), -- Reference codes - ID_TYPE
    identity_value varchar(100), -- driving licence number, NI number, passport number
    issuing_authority varchar(40), -- e.g. UK passport agency, DVLA
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    amended_by varchar(100),
    amended_time timestamp
);

CREATE INDEX idx_contact_identity_contact_id ON contact_identity(contact_id);
CREATE INDEX idx_contact_identity_identity_value ON contact_identity(identity_value);

---------------------------------------------------------------------------------------
-- Contacts may have one or more addresses.
-- This table holds the details of addresses provided for each contact.
-- There may be no addresses provided.
----------------------------------------------------------------------------------------

CREATE TABLE contact_address
(
    contact_address_id bigserial NOT NULL CONSTRAINT contact_address_id_pk PRIMARY KEY,
    contact_id bigint NOT NULL REFERENCES contact(contact_id),
    address_type varchar(12), -- Reference code - ADDRESS_TYPE e.g. HOME, WORK, TEMPORARY, UNKNOWN - nullable
    primary_address boolean NOT NULL DEFAULT false,
    flat varchar(30), -- flat number (nullable)
    property varchar(50), -- house name or number - nullable
    street varchar(160), -- road or street - nullable
    area varchar(70), -- locality (nullable)
    city_code varchar(12), -- Reference codes - CITY - nullable
    county_code varchar(12), -- Reference codes - COUNTY - nullable
    post_code varchar(12),
    country_code varchar(12), -- Reference codes - COUNTRY - nullable
    verified boolean NOT NULL default false, -- has the address been PAF-checked?
    verified_by varchar(100),
    verified_time timestamp,
    mail_flag boolean NOT NULL DEFAULT false,
    start_date date,
    end_date date,
    no_fixed_address boolean NOT NULL DEFAULT false,
    comments varchar(240),
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    amended_by varchar(100),
    amended_time timestamp
);

CREATE INDEX idx_contact_address_contact_id ON contact_address(contact_id);
CREATE INDEX idx_contact_address_post_code ON contact_address(post_code);
CREATE INDEX idx_contact_address_street ON contact_address(street);
CREATE INDEX idx_contact_address_property ON contact_address(property);

---------------------------------------------------------------------------------------
-- Contacts may have multiple email addresses.
-- This table stores the email addresses related to a contact.
----------------------------------------------------------------------------------------

CREATE TABLE contact_email
(
    contact_email_id bigserial NOT NULL CONSTRAINT contact_email_id_pk PRIMARY KEY,
    contact_id bigint NOT NULL REFERENCES contact(contact_id),
    email_address varchar(240) NOT NULL,
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    amended_by varchar(100),
    amended_time timestamp
);

CREATE INDEX idx_contact_email_contact_id ON contact_email(contact_id);
CREATE INDEX idx_contact_email_address ON contact_email(email_address);

---------------------------------------------------------------------------------------
-- Contacts may have multiple telephone numbers.
-- This table stores the telephone numbers related to a contact.
----------------------------------------------------------------------------------------

CREATE TABLE contact_phone
(
    contact_phone_id bigserial NOT NULL CONSTRAINT contact_phone_id_pk PRIMARY KEY,
    contact_id bigint NOT NULL REFERENCES contact(contact_id),
    phone_type varchar(12) NOT NULL, -- Reference codes - PHONE_TYPE e.g. HOME, WORK or MOBILE
    phone_number varchar(40) NOT NULL,
    ext_number varchar(7),
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    amended_by varchar(100),
    amended_time timestamp
);

CREATE INDEX idx_contact_phone_contact_id ON contact_phone(contact_id);
CREATE INDEX idx_contact_phone_number ON contact_phone(phone_number);

---------------------------------------------------------------------------------------
-- Address-specific phone numbers.
-- Currently modelled as a join-table between contact_phone and contact_address.
-- (may revisit to store as a separate set of address-specific phone numbers as in NOMIS)
----------------------------------------------------------------------------------------

CREATE TABLE contact_address_phone
(
    contact_address_phone_id bigserial NOT NULL CONSTRAINT contact_address_phone_id_pk PRIMARY KEY,
    contact_id bigint NOT NULL REFERENCES contact(contact_id),
    contact_address_id bigint NOT NULL REFERENCES contact_address(contact_address_id),
    contact_phone_id bigint NOT NULL REFERENCES contact_phone(contact_phone_id),
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    amended_by varchar(100),
    amended_time timestamp
);

CREATE INDEX idx_contact_address_phone_contact_id ON contact_address_phone(contact_id);
CREATE INDEX idx_contact_address_phone_contact_address_id ON contact_address_phone(contact_address_id);
CREATE INDEX idx_contact_address_phone_contact_phone_id ON contact_address_phone(contact_phone_id);

---------------------------------------------------------------------------------------
-- Restrictions can exist against contacts (unrelated to any prisoners)
-- This table holds the details of restrictions against contacts.
-- In NOMIS this would be VISITOR_RESTRICTIONS.
----------------------------------------------------------------------------------------

CREATE TABLE contact_restriction
(
    contact_restriction_id bigserial NOT NULL CONSTRAINT contact_restriction_id_pk PRIMARY KEY,
    contact_id        bigint NOT NULL REFERENCES contact(contact_id),
    restriction_type  varchar(12) NOT NULL, -- Reference codes - RESTRICTION
    start_date        date,
    expiry_date       date,
    comments          varchar(240),
    staff_username    varchar(100),
    created_by        varchar(100) NOT NULL,
    created_time      timestamp NOT NULL DEFAULT current_timestamp,
    amended_by        varchar(100),
    amended_time      timestamp
);

CREATE INDEX idx_contact_restriction_contact_id ON contact_restriction(contact_id);
CREATE INDEX idx_contact_restriction_start_date ON contact_restriction(start_date);
CREATE INDEX idx_contact_restriction_expiry_date ON contact_restriction(expiry_date);

---------------------------------------------------------------------------------------
-- Prisoners can have a relationship with their contacts eg. MOTHER, SON, SISTER etc.
-- They may also be professional relationships.
-- This table holds the details of the relationships between people in prison and their contacts.
----------------------------------------------------------------------------------------

CREATE TABLE prisoner_contact
(
    prisoner_contact_id bigserial NOT NULL CONSTRAINT prisoner_contact_id_pk PRIMARY KEY,
    contact_id bigint NOT NULL REFERENCES contact(contact_id),
    prisoner_number varchar(7) NOT NULL, -- The prison number (NOMS id) e.g. A1234AA
    active boolean NOT NULL DEFAULT true,
    contact_type varchar(12), -- Reference codes - CONTACT_TYPE (S) social or (O) official
    relationship_type varchar(12) NOT NULL, -- Reference codes - RELATIONSHIP
    current_term boolean NOT NULL DEFAULT true, -- True if it applies to latest booking sequence 1
    approved_visitor boolean NOT NULL DEFAULT false,
    next_of_kin boolean NOT NULL DEFAULT false,
    emergency_contact boolean NOT NULL DEFAULT false,
    comments varchar(240),
    approved_by varchar(100),
    approved_time timestamp,
    expiry_date date,
    created_at_prison varchar(5), -- prison code where this contact was created (for info)
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    amended_by varchar(100),
    amended_time timestamp
);

CREATE INDEX idx_prisoner_contact_contact_id ON prisoner_contact(contact_id);
CREATE INDEX idx_prisoner_contact_prisoner_number ON prisoner_contact(prisoner_number);
CREATE INDEX idx_prisoner_contact_relationship_type on prisoner_contact(relationship_type);

---------------------------------------------------------------------------------------
-- Prisoners and their contacts can have restrictions placed on the relationship.
-- This table holds the details of these restrictions.
----------------------------------------------------------------------------------------

CREATE TABLE prisoner_contact_restriction
(
    prisoner_contact_restriction_id bigserial NOT NULL CONSTRAINT prisoner_contact_restriction_id_pk PRIMARY KEY,
    prisoner_contact_id bigint NOT NULL REFERENCES prisoner_contact(prisoner_contact_id),
    restriction_type varchar(12) NOT NULL, -- Reference code: RESTRICTION
    start_date date,
    expiry_date date,
    comments varchar(255),
    staff_username varchar(100),
    authorised_by varchar(100),
    authorised_time timestamp,
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    amended_by varchar(100),
    amended_time timestamp
);

CREATE INDEX idx_prisoner_contact_restriction_contact_id ON prisoner_contact_restriction(prisoner_contact_id);
CREATE INDEX idx_prisoner_contact_restriction_type ON prisoner_contact_restriction(restriction_type);
CREATE INDEX idx_prisoner_contact_start_date ON prisoner_contact_restriction(start_date);
CREATE INDEX idx_prisoner_contact_expiry_date ON prisoner_contact_restriction(expiry_date);

---------------------------------------------------------------------------------------
-- Contact employments - for official contacts only
-- This table holds the details of the employer(s) of official contacts
-- At this stage the corporate organisations are not held locally so this is external.
----------------------------------------------------------------------------------------

CREATE TABLE contact_employment
(
    contact_employment_id bigserial NOT NULL CONSTRAINT contact_employment_id_pk PRIMARY KEY,
    contact_id bigint NOT NULL REFERENCES contact(contact_id),
    corporate_id bigint NOT NULL,
    corporate_name varchar(100),
    active boolean NOT NULL DEFAULT true,
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    amended_by varchar(100),
    amended_time timestamp
);

CREATE INDEX idx_contact_employment_contact_id ON contact_employment(contact_id);
CREATE INDEX idx_contact_employment_corporate_id ON contact_employment(corporate_id);

---------------------------------------------------------------------------------------
-- Contains coded reference values used to constrain the values of lists/validation.
-- e.g. address types, phone types, county codes, country codes, relationship types etc..
-- Still questions over reference data - who owns this? Other uses in NOMIS?
-- One-way sync from DPS to NOMIS?
-- Local maintenance of reference data? Forms/menu options? Roles to maintain?
----------------------------------------------------------------------------------------

CREATE TABLE reference_codes
(
    reference_code_id   bigserial NOT NULL CONSTRAINT reference_code_pk PRIMARY KEY,
    group_code          varchar(40) NOT NULL,
    code                varchar(40) NOT NULL,
    description         varchar(100) NOT NULL,
    display_order          integer NOT NULL,
    is_active          boolean NOT NULL,
    created_by          varchar(100) NOT NULL,
    created_time        timestamp NOT NULL DEFAULT current_timestamp,
    amended_by          varchar(100),
    amended_time        timestamp
);

CREATE UNIQUE INDEX idx_reference_code_group ON reference_codes(group_code, code);

---------------------------------------------------------------------------------------
-- Nationality reference data exported from NOMIS enriched with ISO codes.
-- This table holds the details from PROFILE_CODES where PROFILE_TYPE= 'NAT';.
----------------------------------------------------------------------------------------

CREATE TABLE nationality_reference
(
    nationality_id          bigserial NOT NULL CONSTRAINT nationality_pk PRIMARY KEY,
    nomis_code        VARCHAR(12),
    nomis_description VARCHAR(100),
    iso_numeric             integer UNIQUE,
    iso_alpha2              char(2) UNIQUE,
    iso_alpha3              char(3) UNIQUE,
    iso_nationality_desc    VARCHAR(100),
    display_sequence        INTEGER
);

---------------------------------------------------------------------------------------
-- Language reference data exported from NOMIS enriched with ISO codes.
-- This table holds the details from OMS_OWNER.REFERENCE_CODES where domain like '%LANG%';
----------------------------------------------------------------------------------------

CREATE TABLE language_reference
(
    language_id         bigserial NOT NULL CONSTRAINT language_pk PRIMARY KEY,
    nomis_code        VARCHAR(12) NOT NULL UNIQUE,
    nomis_description VARCHAR(100) NOT NULL,
    iso_alpha2           char(2) UNIQUE,
    iso_alpha3           char(3) UNIQUE,
    iso_language_desc    VARCHAR(100) NOT NULL,
    display_sequence     INTEGER NOT NULL
);

---------------------------------------------------------------------------------------
-- Country reference data exported from NOMIS enriched with ISO codes.
-- This table holds the details from OMS_OWNER.REFERENCE_CODES where domain like '%COUNTRY%' ;
----------------------------------------------------------------------------------------

CREATE TABLE country_reference
(
    country_id        bigserial NOT NULL CONSTRAINT country_pk PRIMARY KEY,
    nomis_code        VARCHAR(12)  NOT NULL UNIQUE,
    nomis_description VARCHAR(100) NOT NULL,
    iso_numeric       integer NOT NULL UNIQUE,
    iso_alpha2        char(2) NOT NULL UNIQUE,
    iso_alpha3        char(3) NOT NULL UNIQUE,
    iso_country_desc  VARCHAR(100) NOT NULL,
    display_sequence  INTEGER NOT NULL
);

---------------------------------------------------------------------------------------
-- County reference data exported from NOMIS.
-- This table holds the details from OMS_OWNER.REFERENCE_CODES where domain like '%COUNTY%' ;
----------------------------------------------------------------------------------------

CREATE TABLE county_reference
(
    county_id         bigserial NOT NULL CONSTRAINT county_pk PRIMARY KEY,
    nomis_code        VARCHAR(12) UNIQUE NOT NULL,
    nomis_description VARCHAR(100) NOT NULL,
    display_sequence  INTEGER NOT NULL
);


---------------------------------------------------------------------------------------
-- City reference data exported from NOMIS.
-- This table holds the details from OMS_OWNER.REFERENCE_CODES where domain like '%CITY' ;
----------------------------------------------------------------------------------------

CREATE TABLE city_reference
(
    city_id           bigserial NOT NULL CONSTRAINT city_pk PRIMARY KEY,
    nomis_code        VARCHAR(12) UNIQUE NOT NULL,
    nomis_description VARCHAR(100) NOT NULL,
    display_sequence  INTEGER NOT NULL
);

---
-- END
---