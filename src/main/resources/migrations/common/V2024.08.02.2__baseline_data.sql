-- =============================================
-- Base data
-- =============================================

insert into contact (contact_id, prison_id, name, email, telephone, position, enabled, notes, primary_contact, created_by, created_time)
values (1, 1, 'Prab Chan', 'p@t.com', '0117 211311', 'Contacts Admin', true, '', true, 'PRAB', current_timestamp),
       (2, 1, 'Matt Chan', 'm@m.com', '0117 211311', 'Contacts Admin', true, '', false, 'PRAB', current_timestamp),
       (3, 1, 'Steve Chan', 's@s.com', '0117 211311', 'Contacts Admin', true, '', false, 'PRAB', current_timestamp),
       (4, 33, 'Jane Chan', 'j@j.com', '0117 211311', 'Contacts Admin', true, '', true, 'PRAB', current_timestamp),
       (5, 16, 'Robbie Chan', 'r@r.com', '0117 211311', 'Contacts Admin', true, '', true, 'PRAB', current_timestamp);
