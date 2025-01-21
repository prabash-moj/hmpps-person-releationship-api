--
-- Some small updates to reference data descriptions
--

-- Change from Social/Family to Social
UPDATE reference_codes SET description = 'Social' WHERE group_code = 'RELATIONSHIP_TYPE' AND code = 'S';

-- Change mixed case entries to start upper case only
UPDATE reference_codes SET description = 'Access requirements' WHERE group_code = 'RESTRICTION' and code = 'ACC';
UPDATE reference_codes SET description = 'Child visitors to be vetted' WHERE group_code = 'RESTRICTION' and code = 'CHILD';
UPDATE reference_codes SET description = 'Disability health concerns' WHERE group_code = 'RESTRICTION' and code = 'DIHCON';
UPDATE reference_codes SET description = 'Non-contact visit' WHERE group_code = 'RESTRICTION' and code = 'NONCON';
UPDATE reference_codes SET description = 'Previous info' WHERE group_code = 'RESTRICTION' and code = 'PREINF';

-- End