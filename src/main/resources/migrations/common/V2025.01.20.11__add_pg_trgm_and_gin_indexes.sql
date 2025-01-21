--
-- enable pg_trgm and add GIN indexes for contact search
--
CREATE EXTENSION pg_trgm;
CREATE INDEX idx_contact_first_name_gin ON contact USING gin (first_name gin_trgm_ops);
CREATE INDEX idx_contact_last_name_gin ON contact USING gin (last_name gin_trgm_ops);
CREATE INDEX idx_contact_middle_names_gin ON contact USING gin (middle_names gin_trgm_ops);

-- End