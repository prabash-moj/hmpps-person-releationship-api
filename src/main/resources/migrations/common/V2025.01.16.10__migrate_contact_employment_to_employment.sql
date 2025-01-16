--
-- contact_employment was initially created to ensure we didn't need to re-migrate contacts later but then we created
-- employment when creating the organisation data model. employment works with the new organisations table so we will
-- keep that and migrate the data from contact_employment into it.
--

insert into employment (
    organisation_id,
    contact_id,
    active,
    created_by,
    created_time,
    updated_by,
    updated_time
)
select
    corporate_id,
    contact_id,
    active,
    created_by,
    created_time,
    updated_by,
    updated_time
from contact_employment;

DROP TABLE contact_employment;

-- End