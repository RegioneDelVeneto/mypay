UPDATE public.mygov_export_dovuti
SET mygov_anagrafica_stato_multibeneficiario_id= 
	(select mygov_anagrafica_stato_id from mygov_anagrafica_stato where de_tipo_stato like 'export' and cod_stato like 'EXPORT_ESEGUITO')
WHERE mygov_anagrafica_stato_multibeneficiario_id is null 
	OR mygov_anagrafica_stato_multibeneficiario_id = (select mygov_anagrafica_stato_id from mygov_anagrafica_stato where de_tipo_stato like 'export' and cod_stato like 'LOAD_EXPORT');
