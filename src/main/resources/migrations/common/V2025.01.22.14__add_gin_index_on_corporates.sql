--
-- Add GIN index for organisation search
--
CREATE INDEX idx_organisation_name_gin ON organisation USING gin (organisation_name gin_trgm_ops);

-- End