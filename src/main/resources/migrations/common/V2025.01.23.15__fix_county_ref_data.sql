--
-- Some whitespace was introduced around some counties when we first imported it from NOMIS
--
UPDATE reference_codes SET description = TRIM(description) WHERE group_code = 'COUNTY';

-- End