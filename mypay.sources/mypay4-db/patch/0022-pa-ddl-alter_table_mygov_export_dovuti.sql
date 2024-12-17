-- 21 -> cod_stato = 'LOAD_EXPORT' and de_tipo_stato = 'export'
ALTER TABLE public.mygov_export_dovuti ADD mygov_anagrafica_stato_multibeneficiario_id int8 NULL DEFAULT 21;

ALTER TABLE public.mygov_export_dovuti ADD CONSTRAINT mygov_export_dovuti_mygov_anagrafica_stato_multibeneficiario_fkey
FOREIGN KEY (mygov_anagrafica_stato_multibeneficiario_id) REFERENCES public.mygov_anagrafica_stato(mygov_anagrafica_stato_id);
