-- SET SCRIPT NAME
-- use pattern ####-[pa|fesp]-[dml|ddl]-script_name.sql' (max 100 chars)
-- where #### is a sequence of integer numbers (without holes and duplicates) determining script execution order
set myvars.script_name to '0038-pa-ddl-alter_table_mygov_receipt.sql';
-- SET SCRIPT DESCRIPTION
-- free text, max 500 chars
set myvars.script_description to 'add column receipt_bytes to table mygov_receipt';

-- start transaction
BEGIN;

-- check that script has not already been executed
--  and store script execution info into table mygov_script
insert into mygov_script values ( current_setting('myvars.script_name'),current_setting('myvars.script_description') );

------------------- business logic -------------------

ALTER TABLE public.mygov_receipt ADD receipt_bytes bytea NULL;

------------------------------------------------------

-- final commit
COMMIT;
