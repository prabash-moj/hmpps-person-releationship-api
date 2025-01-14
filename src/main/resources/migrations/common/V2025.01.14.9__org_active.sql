--
-- Active flag on an organisation and a deactivated date which is set when the org is set to active = false
--
ALTER TABLE organisation ADD COLUMN active boolean NOT NULL;
ALTER TABLE organisation ADD COLUMN deactivated_date date;

-- End