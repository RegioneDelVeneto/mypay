-- SET SCRIPT NAME
-- use pattern ####-[pa|fesp]-[dml|ddl]-script_name.sql' (max 100 chars)
-- where #### is a sequence of integer numbers (without holes and duplicates) determining script execution order
set myvars.script_name to '0037-pa-dml-init_table_mygov_script.sql';
-- SET SCRIPT DESCRIPTION
-- free text, max 500 chars
set myvars.script_description to 'aggiornamento dati tabella mygov_script per precedenti script già eseguiti';

-- start transaction
BEGIN;

-- check that script has not already been executed
--  and store script execution info into table mygov_script
insert into mygov_script values ( current_setting('myvars.script_name'),current_setting('myvars.script_description') );

------------------- business logic -------------------

insert into mygov_script values (
'0001-pa-ddl-create_function_modify_mygov_dovuto_noinout.sql'
,'adeguamento function modify_mygov_dovuto per jdbi (gestione parametri in-out)'
);
insert into mygov_script values (
'0002-pa-ddl-create_function_insert_mygov_dovuto_noinout.sql'
,'adeguamento function insert_mygov_dovuto per jdbi (gestione parametri in-out)'
);
insert into mygov_script values (
'0003-pa-ddl-create_table_sequence_mygov_registro_operazione.sql'
,'creazione tabella mygov_registro_operazione'
);
insert into mygov_script values (
'0004-pa-ddl-alter_table_mygov_ente_add_column_dt_avvio.sql'
,'aggiunta colonna dt_avvio su tabella mygov_ente'
);
insert into mygov_script values (
'0005-pa-ddl-alter_table_mygov_ente_add_column_cod_tipo_ente.sql'
,'aggiunta colonna cod_tipo_ente su tabella mygov_ente'
);
insert into mygov_script values (
'0006-pa-ddl-alter_table_mygov_ente_tipo_dovuto_add_columns_tassonomia.sql'
,'aggiunta colonne tassonomia su tabella mygov_ente_tipo_dovuto'
);
insert into mygov_script values (
'0007-pa-ddl-alter_table_mygov_operatore_add_column_de_email_address.sql'
,'aggiunta colonna de_email_address su tabella mygov_operatore'
);
insert into mygov_script values (
'0008-pa-ddl-create_function_insert_mygov_dovuto_elaborato_noinout.sql'
,'adeguamento function insert_mygov_dovuto_elaborato per jdbi (gestione parametri in-out)'
);
insert into mygov_script values (
'0009-pa-ddl-alter_table_mygov_utente_add_column_dt_set_address.sql'
,'aggiunta colonna dt_set_address su tabella mygov_utente'
);
insert into mygov_script values (
'0010-pa-ddl-create_table_mygov_validazione_email.sql'
,'creazione tabella mygov_validazione_email'
);
insert into mygov_script values (
'0011-pa-ddl-alter_table_mygov_utente_add_columns_validazione_email.sql'
,'aggiunta colonne validazione_email su tabella mygov_utente'
);
insert into mygov_script values (
'0012-pa-ddl-alter_table_mygov_ente_alter_columns_drop_not_null.sql'
,'rimozione constraint not null su tabella mygov_ente'
);
insert into mygov_script values (
'0013-pa-ddl-create_table_mygov_standard_tipo_dovuto.sql'
,'creazione tabella mygov_standard_tipo_dovuto'
);
insert into mygov_script values (
'0014-pa-dml-insert_table_mygov_standard_tipo_dovuto.sql'
,'inserimento dati tabella mygov_standard_tipo_dovuto'
);
insert into mygov_script values (
'0015-pa-dml-update_table_mygov_operatore_email.sql'
,'aggiornamento dati tabella mygov_operatore'
);
insert into mygov_script values (
'0016-pa-ddl-create_table_mygov_receipt.sql'
,'creazione tabella mygov_receipt'
);
insert into mygov_script values (
'0017-pa-ddl-alter_table_mygov_receipt.sql'
,'rimozione constraint not null su tabella mgov_receipt'
);
insert into mygov_script values (
'0018-pa-ddl-create_table_mygov_dovuto_multibeneficiario.sql'
,'creazione tabella mygov_dovuto_multibeneficiario'
);
insert into mygov_script values (
'0019-pa-ddl-create_table_mygov_dovuto_multibeneficiario_elaborato.sql'
,'creazione tabella mygov_dovuto_multibeneficiario_elaborato'
);
insert into mygov_script values (
'0020-pa-ddl-create_function_sposta_dovuto_in_dovuto_elaborato.sql'
,'aggiornamento funzione sposta_dovuto_in_dovuto_elaborato'
);
insert into mygov_script values (
'0021-pa-ddl-alter_table_mygov_receipt.sql'
,'aggiunta colonna su tabella mygov_receipt'
);
insert into mygov_script values (
'0022-pa-ddl-alter_table_mygov_export_dovuti.sql'
,'aggiunta colonna + foreign key su tabella mygov_export_dovuti'
);
insert into mygov_script values (
'0023-pa-ddl-alter_table_mygov_avviso_anonimo.sql'
,'rimozione foreign key su tabella mygov_avviso_anonimo'
);
insert into mygov_script values (
'0024-pa-ddl-alter_table_mygov_spontaneo_anonimo.sql'
,'rimozione foreign key su tabella mygov_spontaneo_anonimo'
);
insert into mygov_script values (
'0025-pa-dml-update_table_mygov_flusso.sql'
,'inizializzazione dati tabella mygov_flusso'
);
insert into mygov_script values (
'0026-pa-ddl-alter_table_mygov_dovuto_multibeneficiario.sql'
,'aggiunta colonne su tabella mygov_dovuto_multibeneficiario'
);
insert into mygov_script values (
'0027-pa-ddl-alter_table_mygov_receipt.sql'
,'aggiornamento colonna su tabella mygov_receipt'
);
insert into mygov_script values (
'0028-pa-ddl-alter_table_mygov_avviso.sql'
,'rimozione not null su tabella mygov_avviso'
);
insert into mygov_script values (
'0029-pa-ddl-alter_table_mygov_export_dovuti.sql'
,'aggiornamento colonna su tabella mygov_export_dovuti'
);
insert into mygov_script values (
'0030-pa-dml-update_table_mygov_export_dovuti.sql'
,'aggiornamento dati su tabella mygov_export_dovuti'
);
insert into mygov_script values (
'0031-pa-ddl-alter_table_mygov_export_dovuti.sql'
,'aggiunta colonna su mygov_export_dovuti'
);
insert into mygov_script values (
'0032-pa-ddl-alter_table_mygov_dovuto_multibeneficiario.sql'
,'rimozione colonna su mygov_dovuto_multibeneficiario_elaborato'
);
insert into mygov_script values (
'0033-pa-ddl-create_function_sposta_dovuto_in_dovuto_elaborato.sql'
,'aggiornamento funzione sposta_dovuto_in_dovuto_elaborato'
);
insert into mygov_script values (
'0034-pa-ddl-alter_table_mygov_receipt.sql'
,'aggiunta colonne + aggiornamento dati su tabelle mygov_receipt'
);
insert into mygov_script values (
'0035-pa-ddl-alter_table_mygov_receipt.sql'
,'rimozione colonna su mygov_receipt'
);
insert into mygov_script values (
'0036-pa-ddl-create_table_mygov_script.sql'
,'creazione tabella mygov_script'
);

------------------------------------------------------

-- final commit
COMMIT;
