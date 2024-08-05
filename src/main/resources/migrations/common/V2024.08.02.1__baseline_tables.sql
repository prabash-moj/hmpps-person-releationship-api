--
-- TABLES
--

---------------------------------------------------------------------------------------
-- Contains coded reference values, used to constrain the values of lists/validation.
-- e.g. Booking types, meeting types, hearing types, status etc..
----------------------------------------------------------------------------------------


CREATE TABLE contact
(
    contact_id bigserial NOT NULL CONSTRAINT contact_id_pk PRIMARY KEY,
    prison_id         bigint,
    name              varchar(100) NOT NULL,
    email             varchar(100),
    telephone         varchar(20),
    position          varchar(100),
    enabled boolean   NOT NULL,
    notes             varchar(200),
    primary_contact   boolean NOT NULL,
    created_by        varchar(100) NOT NULL,
    created_time      timestamp NOT NULL,
    amended_by        varchar(100),
    amended_time      timestamp
);

CREATE INDEX idx_prison_prison_id ON contact(prison_id);
CREATE INDEX idx_contact_name ON contact(name);
CREATE INDEX idx_contact_email ON contact(email);

-- END --
