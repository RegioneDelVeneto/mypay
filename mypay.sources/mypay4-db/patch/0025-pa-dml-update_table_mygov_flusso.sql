-- insert default flussi for carrello ESTERNO-ANONIMO_MULTIENTE for existing ENTE not having it (normally they are created when ENTE is created)
-- the condition on IUF length is necessary given the size of the field on DB (therefore for some ENTE it's not possible to create this flow)
INSERT INTO public.mygov_flusso (mygov_flusso_id, "version", mygov_ente_id, mygov_anagrafica_stato_id, iuf, num_righe_totali, num_righe_importate_correttamente, dt_creazione, dt_ultima_modifica, flg_attivo)
select 
	nextval('mygov_flusso_mygov_flusso_id_seq'),
	0,
	me.mygov_ente_id,
	(select mygov_anagrafica_stato_id from mygov_anagrafica_stato where de_tipo_stato='flusso' and cod_stato='CARICATO'),
	'_'||me.cod_ipa_ente ||'_ESTERNO-ANONIMO_MULTIENTE',
	0,
	0,
	now(),
	now(),
	true
  from mygov_ente me 
 where LENGTH('_'||me.cod_ipa_ente ||'_ESTERNO-ANONIMO_MULTIENTE')<=50
   and not exists (
select 1
  from mygov_flusso mf
 where mf.mygov_ente_id = me.mygov_ente_id
   and mf.iuf = '_'||me.cod_ipa_ente ||'_ESTERNO-ANONIMO_MULTIENTE'
 )
 