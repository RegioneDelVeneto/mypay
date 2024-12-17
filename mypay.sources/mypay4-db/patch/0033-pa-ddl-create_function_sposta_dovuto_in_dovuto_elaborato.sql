-- FUNCTION: public.sposta_dovuto_in_dovuto_elaborato(bigint, bigint, numeric, bigint, bigint, character varying, character varying, timestamp without time zone, character varying, character, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, date, character varying, numeric, numeric, character varying, character varying, character varying, character varying)

-- DROP FUNCTION public.sposta_dovuto_in_dovuto_elaborato(bigint, bigint, numeric, bigint, bigint, character varying, character varying, timestamp without time zone, character varying, character, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, date, character varying, numeric, numeric, character varying, character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION public.sposta_dovuto_in_dovuto_elaborato(
	id_dovuto_cancellare bigint,
	n_mygov_flusso_id bigint,
	n_num_riga_flusso numeric,
	n_mygov_anagrafica_stato_id bigint,
	n_mygov_carrello_id bigint,
	n_cod_iud character varying,
	n_cod_iuv character varying,
	n_dt_creazione timestamp without time zone,
	n_de_rp_versione_oggetto character varying,
	n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character,
	n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying,
	n_de_rp_sogg_pag_anagrafica_pagatore character varying,
	n_de_rp_sogg_pag_indirizzo_pagatore character varying,
	n_de_rp_sogg_pag_civico_pagatore character varying,
	n_cod_rp_sogg_pag_cap_pagatore character varying,
	n_de_rp_sogg_pag_localita_pagatore character varying,
	n_de_rp_sogg_pag_provincia_pagatore character varying,
	n_cod_rp_sogg_pag_nazione_pagatore character varying,
	n_de_rp_sogg_pag_email_pagatore character varying,
	n_dt_rp_dati_vers_data_esecuzione_pagamento date,
	n_cod_rp_dati_vers_tipo_versamento character varying,
	n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric,
	n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric,
	n_cod_tipo_dovuto character varying,
	n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying,
	n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying,
	n_bilancio character varying)
    RETURNS character varying
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
DECLARE
		found_mygov_dovuto_multibeneficiario	mygov_dovuto_multibeneficiario%ROWTYPE;
BEGIN   
		
		SELECT   mdm.*
		INTO     found_mygov_dovuto_multibeneficiario
		FROM     mygov_dovuto_multibeneficiario mdm
		WHERE	 mdm.mygov_dovuto_id = id_dovuto_cancellare
		LIMIT 1;	 
		
		IF found_mygov_dovuto_multibeneficiario.mygov_dovuto_id IS NOT NULL THEN
			DELETE FROM mygov_dovuto_multibeneficiario
			WHERE mygov_dovuto_id = id_dovuto_cancellare;
		/*ELSE			*/
			/*RETURN 'PAA_IMPORT_DOVUTO_NON_PRESENTE';*/
		END IF;
		
	    DELETE FROM mygov_dovuto
	    WHERE mygov_dovuto_id = id_dovuto_cancellare;  

	    IF NOT FOUND THEN 
		RETURN 'PAA_IMPORT_DOVUTO_NON_PRESENTE';
	    END IF;
		    
	    INSERT INTO mygov_dovuto_elaborato(
	    mygov_dovuto_elaborato_id, "version", flg_dovuto_attuale, mygov_flusso_id, 
	    num_riga_flusso, mygov_anagrafica_stato_id, mygov_carrello_id, 
	    cod_iud, cod_iuv, dt_creazione, de_rp_versione_oggetto, cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
	    cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, de_rp_sogg_pag_anagrafica_pagatore, 
	    de_rp_sogg_pag_indirizzo_pagatore, de_rp_sogg_pag_civico_pagatore, 
	    cod_rp_sogg_pag_cap_pagatore, de_rp_sogg_pag_localita_pagatore, 
	    de_rp_sogg_pag_provincia_pagatore, cod_rp_sogg_pag_nazione_pagatore, 
	    de_rp_sogg_pag_email_pagatore, dt_rp_dati_vers_data_esecuzione_pagamento, 
	    cod_rp_dati_vers_tipo_versamento, num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
	    num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, 
	    cod_tipo_dovuto,
	    de_rp_dati_vers_dati_sing_vers_causale_versamento, de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, dt_ultimo_cambio_stato, bilancio)

	    VALUES (nextval('mygov_dovuto_elaborato_mygov_dovuto_elaborato_id_seq'), 0, true, n_mygov_flusso_id, 
	    n_num_riga_flusso, n_mygov_anagrafica_stato_id, n_mygov_carrello_id, 
	    n_cod_iud, n_cod_iuv, n_dt_creazione, n_de_rp_versione_oggetto, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
	    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, n_de_rp_sogg_pag_anagrafica_pagatore, 
	    n_de_rp_sogg_pag_indirizzo_pagatore, n_de_rp_sogg_pag_civico_pagatore, 
	    n_cod_rp_sogg_pag_cap_pagatore, n_de_rp_sogg_pag_localita_pagatore, 
	    n_de_rp_sogg_pag_provincia_pagatore, n_cod_rp_sogg_pag_nazione_pagatore, 
	    n_de_rp_sogg_pag_email_pagatore, n_dt_rp_dati_vers_data_esecuzione_pagamento, 
	    null, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
	    n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa,
	    n_cod_tipo_dovuto,
	    n_de_rp_dati_vers_dati_sing_vers_causale_versamento, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_dt_creazione, n_bilancio);	

	    RETURN null;    
    END;
$BODY$;
