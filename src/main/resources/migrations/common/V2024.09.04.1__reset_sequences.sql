---------------------------------------------------------------------------------------
-- Support the estimated date of birth question. Is the contact over 18? yes, no, don't know
----------------------------------------------------------------------------------------

    alter table contact add column is_over_eighteen boolean;

-- End