-- MULTIBENEFICIARIO FOR mygov_dovuto
ALTER TABLE public.mygov_dovuto_multibeneficiario ADD COLUMN de_rp_dati_vers_dati_sing_vers_causale_versamento character varying(1024);
ALTER TABLE public.mygov_dovuto_multibeneficiario ADD COLUMN de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying(140);
-- MULTIBENEFICIARIO FOR mygov_dovuto_elaborato
ALTER TABLE public.mygov_dovuto_multibeneficiario_elaborato ADD COLUMN de_rp_dati_vers_dati_sing_vers_causale_versamento character varying(1024);
ALTER TABLE public.mygov_dovuto_multibeneficiario_elaborato ADD COLUMN de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying(140);
