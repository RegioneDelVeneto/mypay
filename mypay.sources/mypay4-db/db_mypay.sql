--
-- PostgreSQL database dump
--

-- Dumped from database version 10.14
-- Dumped by pg_dump version 14.4

-- Started on 2022-09-22 16:13:41

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2813 (class 1262 OID 20840)
-- Name: mypay; Type: DATABASE; Schema: -; Owner: mypay4
--

CREATE DATABASE mypay WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE = 'en_US.UTF-8';


ALTER DATABASE mypay OWNER TO mypay4;

\connect mypay

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 614 (class 1247 OID 20844)
-- Name: dblink_pkey_results; Type: TYPE; Schema: public; Owner: mypay4
--

CREATE TYPE public.dblink_pkey_results AS (
	"position" integer,
	colname text
);


ALTER TYPE public.dblink_pkey_results OWNER TO mypay4;

--
-- TOC entry 287 (class 1255 OID 20845)
-- Name: annulla_dovuto_iuv_ente(bigint, bigint, numeric, bigint, character varying, timestamp without time zone, character varying, date, numeric, bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.annulla_dovuto_iuv_ente(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_de_rp_versione_oggetto character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_mygov_utente_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
    DECLARE
    
	id_lock					bigint;
	dovuto_cancellare			mygov_dovuto;
	num_cod_tipo				bigint;
        val_cod_ipa_ente			character varying;
	val_application_code			character varying(2);	  
	
    BEGIN  

	/* ************  LOCK SU mygov_ente_id TABELLA mygov_ente  ********** */

	SELECT   mygov_ente_id 
	       , cod_ipa_ente
	       , application_code
	FROM     mygov_ente 
	WHERE    mygov_ente_id = n_mygov_ente_id 
	INTO     id_lock
	       , val_cod_ipa_ente 
	       , val_application_code
	FOR UPDATE;

	/* *****************   TROVO DOVUTO DA CANCELLARE   *********** */
	SELECT	dovuto.*
	INTO	dovuto_cancellare
	FROM	mygov_dovuto dovuto
	      , mygov_flusso flusso
	      , mygov_anagrafica_stato stato	    
	WHERE	flusso.mygov_flusso_id = dovuto.mygov_flusso_id
	AND	stato.mygov_anagrafica_stato_id = dovuto.mygov_anagrafica_stato_id
	AND	stato.cod_stato = 'INSERIMENTO_DOVUTO'
	AND	stato.de_tipo_stato = 'dovuto'
	AND	dovuto.cod_iuv = n_cod_iuv
	AND	dovuto.flg_dovuto_attuale = true
	AND	flusso.flg_attivo = true
	AND	flusso.mygov_ente_id = n_mygov_ente_id;  


	IF dovuto_cancellare.mygov_dovuto_id IS NULL THEN 
		PERFORM insert_mygov_dovuto_rifiutato(n_mygov_flusso_id
						    ,n_num_riga_flusso 
						    ,null
						    ,n_cod_iuv
						    ,(select mygov_anagrafica_stato_id from mygov_anagrafica_stato where cod_stato = 'ERROR_DOVUTO')
						    ,'Dovuto non presente o in pagamento nel database, impossibile modificarlo'
						    ,n_dt_creazione
						    ,n_dt_creazione);
		RETURN; 
	END IF; 			  

	/* ************  VERIFICA permessi dell''operatore su tipo dovuto del dovuto esistente ********** */

	SELECT INTO num_cod_tipo sum(
	     (SELECT count(1) 	
	FROM mygov_ente_tipo_dovuto metd
	   , mygov_flusso mf
	   , mygov_operatore_ente_tipo_dovuto tdo
	   , mygov_operatore o
	   , mygov_utente u
	WHERE metd.cod_tipo = dovuto_cancellare.cod_tipo_dovuto 
		AND metd.mygov_ente_id = mf.mygov_ente_id 
		AND mf.mygov_flusso_id = dovuto_cancellare.mygov_flusso_id
		AND tdo.mygov_ente_tipo_dovuto_id = metd.mygov_ente_tipo_dovuto_id
		AND tdo.flg_attivo = true
		AND tdo.mygov_operatore_id = o.mygov_operatore_id
		AND o.cod_fed_user_id = u.cod_fed_user_id
		AND u.mygov_utente_id = n_mygov_utente_id) 
		+
	    (SELECT count(1)
	       FROM mygov_utente u
	      WHERE split_part(u.cod_fed_user_id, '-', 2) = 'WS_USER' 
		AND u.mygov_utente_id = n_mygov_utente_id));
	    
	IF ((num_cod_tipo = 0) OR (num_cod_tipo > 1)) THEN
		PERFORM insert_mygov_dovuto_rifiutato(n_mygov_flusso_id
						    ,n_num_riga_flusso 
						    ,null
						    ,n_cod_iuv
						    ,(select mygov_anagrafica_stato_id from mygov_anagrafica_stato where cod_stato = 'ERROR_DOVUTO')
						    ,'L'' operatore non ha i permessi sul tipo dovuto per annullare il dovuto esistente; per questo ente'
						    ,n_dt_creazione
						    ,n_dt_creazione);

			
		RETURN;
	END IF;	 		  

	PERFORM sposta_dovuto_in_dovuto_elaborato(dovuto_cancellare.mygov_dovuto_id, n_mygov_flusso_id, n_num_riga_flusso, 
	    n_mygov_anagrafica_stato_id, null, dovuto_cancellare.cod_iud, n_cod_iuv, n_dt_creazione, 
	    n_de_rp_versione_oggetto, dovuto_cancellare.cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, dovuto_cancellare.cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, 
	    dovuto_cancellare.de_rp_sogg_pag_anagrafica_pagatore, dovuto_cancellare.de_rp_sogg_pag_indirizzo_pagatore, dovuto_cancellare.de_rp_sogg_pag_civico_pagatore, 
	    dovuto_cancellare.cod_rp_sogg_pag_cap_pagatore, dovuto_cancellare.de_rp_sogg_pag_localita_pagatore, dovuto_cancellare.de_rp_sogg_pag_provincia_pagatore, 
	    dovuto_cancellare.cod_rp_sogg_pag_nazione_pagatore, dovuto_cancellare.de_rp_sogg_pag_email_pagatore, n_dt_rp_dati_vers_data_esecuzione_pagamento, 
	    dovuto_cancellare.cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
	    dovuto_cancellare.num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, dovuto_cancellare.cod_tipo_dovuto, dovuto_cancellare.de_rp_dati_vers_dati_sing_vers_causale_versamento, 
	    dovuto_cancellare.de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, dovuto_cancellare.bilancio);

		/* Quando annullo un avviso digitale, bisogna creare una richiesta di cancellazione avviso digitale esistente (tipo_operazione = C) */
		
	IF (n_cod_iuv IS NOT NULL AND
		(SELECT  flg_attivo
		    FROM    mygov_ente_funzionalita 
		    WHERE   cod_ipa_ente = val_cod_ipa_ente
		    AND     cod_funzionalita = 'AVVISO_DIGITALE')) THEN
		
		/* Se non è stata ancora sottomessa la richiesta di creazione di un nuovo avviso digitale (tipo_operazione = C), è sufficiente settare lo stato ad ANNULLATO*/
		UPDATE mygov_avviso_digitale 
		SET    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'ANNULLATO'
						    AND    de_tipo_stato = 'ad'),
			  tipo_operazione = 'D',
			  dt_ultima_modifica = NOW()
		WHERE  (cod_ad_cod_avviso = '0' || val_application_code || n_cod_iuv OR cod_ad_cod_avviso = '3' || n_cod_iuv)
		  AND  mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'NUOVO'
						    AND    de_tipo_stato = 'ad')
		  AND tipo_operazione = 'C';
		 
		/* Creo una nuova richiesta di cancellazione di avviso digitale (tipo_operazione = C) se 
			- non è stato già annullato
			- non è stata già creata richiesta di cancellazione */
		
		UPDATE mygov_avviso_digitale 
		SET    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'NUOVO'
						    AND    de_tipo_stato = 'ad'),
			  tipo_operazione = 'D',
			  dt_ultima_modifica = NOW()
		WHERE  (cod_ad_cod_avviso = '0' || val_application_code || n_cod_iuv OR cod_ad_cod_avviso = '3' || n_cod_iuv)
		  AND  mygov_anagrafica_stato_id != (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'ANNULLATO'
						    AND    de_tipo_stato = 'ad')
		 AND tipo_operazione != 'D';
		 
	END IF;
	
	
	IF (n_cod_iuv IS NOT NULL AND
		(SELECT  flg_attivo
		    FROM    mygov_ente_funzionalita 
		    WHERE   cod_ipa_ente = val_cod_ipa_ente
		    AND     cod_funzionalita = 'NOTIFICA_AVVISI_IO')) THEN
		UPDATE mygov_avviso_digitale 
		SET    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'ANNULLATO'
						    AND    de_tipo_stato = 'naIO'),
			tipo_operazione = 'D',
			dt_ultima_modifica = NOW()
		WHERE  (cod_ad_cod_avviso = '0_' || val_application_code || '_' || n_cod_iuv OR cod_ad_cod_avviso = '3_' || n_cod_iuv)
		  AND  mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'NUOVO'
						    AND    de_tipo_stato = 'naIO');
	END IF;
	
    END;
$$;


ALTER FUNCTION public.annulla_dovuto_iuv_ente(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_de_rp_versione_oggetto character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_mygov_utente_id bigint) OWNER TO postgres;

--
-- TOC entry 288 (class 1255 OID 20848)
-- Name: annulla_dovuto_iuv_ente(bigint, bigint, numeric, bigint, character varying, timestamp without time zone, character varying, date, numeric, character varying, bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.annulla_dovuto_iuv_ente(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, INOUT n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_de_rp_versione_oggetto character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_cod_tipo_dovuto character varying, n_mygov_utente_id bigint, OUT result character varying, OUT result_desc character varying) RETURNS record
    LANGUAGE plpgsql
    AS $$
    DECLARE
    
	id_lock					bigint;
	dovuto_cancellare			mygov_dovuto;
	num_cod_tipo				bigint;
        val_cod_ipa_ente			character varying;
	val_application_code			character varying(2);	  
	
    BEGIN  

	/* ************  LOCK SU mygov_ente_id TABELLA mygov_ente  ********** */

	SELECT   mygov_ente_id 
	       , cod_ipa_ente
	       , application_code
	FROM     mygov_ente 
	WHERE    mygov_ente_id = n_mygov_ente_id 
	INTO     id_lock
	       , val_cod_ipa_ente 
	       , val_application_code
	FOR UPDATE;

	/* *****************   TROVO DOVUTO DA CANCELLARE   *********** */
	SELECT	dovuto.*
	INTO	dovuto_cancellare
	FROM	mygov_dovuto dovuto
	      , mygov_flusso flusso
	      , mygov_anagrafica_stato stato	    
	WHERE	flusso.mygov_flusso_id = dovuto.mygov_flusso_id
	AND	stato.mygov_anagrafica_stato_id = dovuto.mygov_anagrafica_stato_id
	AND	stato.cod_stato = 'INSERIMENTO_DOVUTO'
	AND	stato.de_tipo_stato = 'dovuto'
	AND	dovuto.cod_iuv = n_cod_iuv
	AND	dovuto.flg_dovuto_attuale = true
	AND	flusso.flg_attivo = true
	AND	flusso.mygov_ente_id = n_mygov_ente_id;  


	IF dovuto_cancellare.mygov_dovuto_id IS NULL THEN 
		result := 'PAA_IMPORT_DOVUTO_NON_PRESENTE'; 
		result_desc := 'Dovuto non presente o in pagamento nel database, impossibile modificarlo'; 
		RETURN;
	END IF; 			  

	/* ************  VERIFICA permessi dell''operatore su tipo dovuto del dovuto esistente ********** */

	SELECT INTO num_cod_tipo sum(
	     (SELECT count(1)
		FROM mygov_ente_tipo_dovuto metd
		   , mygov_operatore_ente_tipo_dovuto tdo
		   , mygov_operatore o
		   , mygov_utente u
		   , mygov_ente e
		WHERE  e.mygov_ente_id = metd.mygov_ente_id 
		   AND tdo.mygov_ente_tipo_dovuto_id = metd.mygov_ente_tipo_dovuto_id
		   AND tdo.mygov_operatore_id = o.mygov_operatore_id
		   AND o.cod_fed_user_id = u.cod_fed_user_id
		   AND o.cod_ipa_ente = e.cod_ipa_ente 
		   AND e.mygov_ente_id = n_mygov_ente_id
		   AND tdo.flg_attivo = true
		   AND metd.cod_tipo = n_cod_tipo_dovuto
		   AND u.mygov_utente_id = n_mygov_utente_id)
		+
	    (SELECT count(1)
	       FROM mygov_utente u
	      WHERE split_part(u.cod_fed_user_id, '-', 2) = 'WS_USER' 
		AND u.mygov_utente_id = n_mygov_utente_id));
	    
	IF ((num_cod_tipo = 0) OR (num_cod_tipo > 1)) THEN
		result := 'PAA_IMPORT_OPERATORE_NO_PERMESSI';
		result_desc := 'L''operatore non ha i permessi per questo tipo dovuto e ente';
		RETURN;
	END IF;	 		  

	PERFORM sposta_dovuto_in_dovuto_elaborato(dovuto_cancellare.mygov_dovuto_id, n_mygov_flusso_id, n_num_riga_flusso, 
	    n_mygov_anagrafica_stato_id, null, dovuto_cancellare.cod_iud, n_cod_iuv, n_dt_creazione, 
	    n_de_rp_versione_oggetto, dovuto_cancellare.cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, dovuto_cancellare.cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, 
	    dovuto_cancellare.de_rp_sogg_pag_anagrafica_pagatore, dovuto_cancellare.de_rp_sogg_pag_indirizzo_pagatore, dovuto_cancellare.de_rp_sogg_pag_civico_pagatore, 
	    dovuto_cancellare.cod_rp_sogg_pag_cap_pagatore, dovuto_cancellare.de_rp_sogg_pag_localita_pagatore, dovuto_cancellare.de_rp_sogg_pag_provincia_pagatore, 
	    dovuto_cancellare.cod_rp_sogg_pag_nazione_pagatore, dovuto_cancellare.de_rp_sogg_pag_email_pagatore, n_dt_rp_dati_vers_data_esecuzione_pagamento, 
	    dovuto_cancellare.cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
	    dovuto_cancellare.num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, dovuto_cancellare.cod_tipo_dovuto, dovuto_cancellare.de_rp_dati_vers_dati_sing_vers_causale_versamento, 
	    dovuto_cancellare.de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, dovuto_cancellare.bilancio);

	IF (n_cod_iuv IS NOT NULL AND
		(SELECT  flg_attivo
		    FROM    mygov_ente_funzionalita 
		    WHERE   cod_ipa_ente = val_cod_ipa_ente
		    AND     cod_funzionalita = 'AVVISO_DIGITALE')) THEN
		UPDATE mygov_avviso_digitale 
		SET    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'ANNULLATO'
						    AND    de_tipo_stato = 'ad')
		WHERE  cod_ad_cod_avviso = '0' || val_application_code || n_cod_iuv
		  AND  mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'NUOVO'
						    AND    de_tipo_stato = 'ad');
	END IF;
	
	
	IF (n_cod_iuv IS NOT NULL AND
		(SELECT  flg_attivo
		    FROM    mygov_ente_funzionalita 
		    WHERE   cod_ipa_ente = val_cod_ipa_ente
		    AND     cod_funzionalita = 'NOTIFICA_AVVISI_IO')) THEN
		UPDATE mygov_avviso_digitale 
		SET    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'ANNULLATO'
						    AND    de_tipo_stato = 'naIO'),
			dt_ultima_modifica = NOW()
		WHERE  (cod_ad_cod_avviso = '0_' || val_application_code || '_' || n_cod_iuv OR cod_ad_cod_avviso = '3_' || n_cod_iuv)
		  AND  mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'NUOVO'
						    AND    de_tipo_stato = 'naIO');
	END IF;
	
	
	
	
    END;
$$;


ALTER FUNCTION public.annulla_dovuto_iuv_ente(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, INOUT n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_de_rp_versione_oggetto character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_cod_tipo_dovuto character varying, n_mygov_utente_id bigint, OUT result character varying, OUT result_desc character varying) OWNER TO postgres;

--
-- TOC entry 301 (class 1255 OID 20851)
-- Name: get_ente_tipo_progressivo(character varying, character varying, date); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.get_ente_tipo_progressivo(n_cod_ipa_ente character varying, n_tipo_generatore character varying, n_data_riferimento date) RETURNS bigint
    LANGUAGE plpgsql
    AS $$
	DECLARE
		old_num_progressivo 		bigint;
		old_data_riferimento 		date;
		insert_num_progressivo 		bigint;
		insert_data_riferimento		date;
		return_value			bigint;

	BEGIN
		IF (n_tipo_generatore = 'prog_flusso_avviso_digitale' OR n_tipo_generatore = 'cod_avviso_digitale') THEN
		    INSERT INTO mygov_ente_tipo_progressivo(cod_ipa_ente, tipo_generatore, data_riferimento, num_progressivo)
		    VALUES (n_cod_ipa_ente, n_tipo_generatore, n_data_riferimento, 1);
		    RETURN 1;
		ELSE
			RAISE EXCEPTION 'TipoGeneratoreNonRiconosciutoException';   					
		END IF;	    
		EXCEPTION
		    WHEN unique_violation THEN
			SELECT data_riferimento, num_progressivo  
			FROM   mygov_ente_tipo_progressivo 
			WHERE  cod_ipa_ente = n_cod_ipa_ente 
			AND    tipo_generatore = n_tipo_generatore 
			INTO   old_data_riferimento, old_num_progressivo FOR UPDATE;
			
			IF (n_data_riferimento > old_data_riferimento) THEN
				insert_num_progressivo:=1;
				insert_data_riferimento:=n_data_riferimento;
				return_value:=1;
			ELSEIF (n_data_riferimento = old_data_riferimento) THEN
				insert_num_progressivo:=old_num_progressivo + 1;
				insert_data_riferimento:=old_data_riferimento;
				return_value:=old_num_progressivo + 1;
			ELSE
				RAISE EXCEPTION 'DataPrecedenteException';
			END IF;			
			IF (n_tipo_generatore = 'prog_flusso_avviso_digitale') THEN
				IF (insert_num_progressivo > 99) THEN
					RAISE EXCEPTION 'SuperatoNumeroMassimoException';
				END IF;
			ELSEIF (n_tipo_generatore = 'cod_avviso_digitale') THEN
				IF (insert_num_progressivo > 99999999999) THEN
					RAISE EXCEPTION 'SuperatoNumeroMassimoException';
				END IF;
			ELSE
				RAISE EXCEPTION 'TipoGeneratoreNonRiconosciutoException';   					
			END IF;
			
			UPDATE mygov_ente_tipo_progressivo 
			SET    num_progressivo = insert_num_progressivo
			      ,data_riferimento = insert_data_riferimento
			WHERE  cod_ipa_ente = n_cod_ipa_ente 
			AND    tipo_generatore = n_tipo_generatore ;
			return return_value; 
	END;
$$;


ALTER FUNCTION public.get_ente_tipo_progressivo(n_cod_ipa_ente character varying, n_tipo_generatore character varying, n_data_riferimento date) OWNER TO postgres;

--
-- TOC entry 302 (class 1255 OID 20852)
-- Name: insert_mygov_avviso_digitale(bigint, timestamp without time zone, character varying, character varying, character, character varying, date, numeric, character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.insert_mygov_avviso_digitale(n_mygov_ente_id bigint, n_dt_creazione timestamp without time zone, n_cod_iuv character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_sogg_pag_email_pagatore character varying) RETURNS void
    LANGUAGE plpgsql
    AS $$	

    DECLARE
	val_mygov_ente_id 		bigint;
	val_cod_ipa_ente		character varying(80);
	val_codice_fiscale_ente		character varying(35);
	val_de_nome_ente		character varying(35);
	val_application_code		character varying(2);	
	val_dati_sing_vers_iban_accredito	character varying(35); 
	val_dati_sing_vers_iban_appoggio	character varying(35);
	

    BEGIN


	/* ************  LOCK SU mygov_ente_id TABELLA mygov_ente  ********** */

	SELECT   mygov_ente_id
	       , cod_ipa_ente
	       , codice_fiscale_ente
	       , substr(de_nome_ente, 0, 35) 
	       , application_code
		   , cod_rp_dati_vers_dati_sing_vers_iban_accredito
		   , cod_rp_dati_vers_dati_sing_vers_iban_appoggio
	FROM     mygov_ente 
	WHERE    mygov_ente_id = n_mygov_ente_id 
	INTO     val_mygov_ente_id
	       , val_cod_ipa_ente
	       , val_codice_fiscale_ente
	       , val_de_nome_ente
	       , val_application_code 
		   , val_dati_sing_vers_iban_accredito 
		   , val_dati_sing_vers_iban_appoggio
   FOR UPDATE;   
   
	    IF length(n_cod_iuv) = 15 THEN

			INSERT INTO mygov_avviso_digitale(
				    mygov_avviso_digitale_id,
					version,
					cod_ad_id_dominio,
					de_ad_anag_beneficiario, 
				    cod_ad_id_messaggio_richiesta,
					cod_ad_cod_avviso,
					de_ad_sog_pag_anag_pagatore, 
				    cod_ad_sog_pag_id_univ_pag_tipo_id_univ,
					cod_ad_sog_pag_id_univ_pag_cod_id_univ, 
				    dt_ad_data_scadenza_pagamento,
					dt_ad_data_scadenza_avviso,
					num_ad_importo_avviso,
					de_ad_email_soggetto,  /*deRpSoggPagEmailPagatore di mygov_dovuto se presente*/
					de_ad_desc_pagamento,
					mygov_anagrafica_stato_id, 
				    num_ad_tentativi_invio, 
					dt_creazione, 
					dt_ultima_modifica,
					dati_sing_vers_iban_accredito,
					dati_sing_vers_iban_appoggio,
					tipo_pagamento,
					tipo_operazione)
			    VALUES (nextval('mygov_avviso_digitale_id_seq'),
					0, 
					val_codice_fiscale_ente, 
					val_de_nome_ente, 
				    to_char(n_dt_creazione, 'YYYYMMDD') || '_' || (select get_ente_tipo_progressivo(val_cod_ipa_ente, 'cod_avviso_digitale'::character varying, n_dt_creazione::date))::text, 
					'0' || val_application_code || n_cod_iuv, 
					n_de_rp_sogg_pag_anagrafica_pagatore, 
				    n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
					n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, 
				    n_dt_rp_dati_vers_data_esecuzione_pagamento, 
					n_dt_rp_dati_vers_data_esecuzione_pagamento, 
					n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
					n_de_rp_sogg_pag_email_pagatore,					
				    substring(n_de_rp_dati_vers_dati_sing_vers_causale_versamento from 1 for 10000),
				    (SELECT mygov_anagrafica_stato_id FROM mygov_anagrafica_stato WHERE cod_stato = 'NUOVO' AND de_tipo_stato = 'ad'), 
				    0, 
					n_dt_creazione, 
					n_dt_creazione,
					val_dati_sing_vers_iban_accredito, 
					val_dati_sing_vers_iban_appoggio,
					1,
					'C');	  

		ELSIF length(n_cod_iuv) = 17 THEN

			INSERT INTO mygov_avviso_digitale(
				    mygov_avviso_digitale_id, 
					version,
					cod_ad_id_dominio,
					de_ad_anag_beneficiario, 
				    cod_ad_id_messaggio_richiesta,
					cod_ad_cod_avviso,
					de_ad_sog_pag_anag_pagatore, 
				    cod_ad_sog_pag_id_univ_pag_tipo_id_univ,
					cod_ad_sog_pag_id_univ_pag_cod_id_univ, 
				    dt_ad_data_scadenza_pagamento,
					dt_ad_data_scadenza_avviso,
					num_ad_importo_avviso,
					de_ad_email_soggetto, /*deRpSoggPagEmailPagatore di mygov_dovuto se presente */
				    de_ad_desc_pagamento, 
				    mygov_anagrafica_stato_id, 
				    num_ad_tentativi_invio,
					dt_creazione,
					dt_ultima_modifica,
					dati_sing_vers_iban_accredito,
					dati_sing_vers_iban_appoggio,
					tipo_pagamento,
					tipo_operazione)
			    VALUES (nextval('mygov_avviso_digitale_id_seq'),
					0, 
					val_codice_fiscale_ente,
					val_de_nome_ente, 
				    to_char(n_dt_creazione, 'YYYYMMDD') || '_' || (select get_ente_tipo_progressivo(val_cod_ipa_ente, 'cod_avviso_digitale'::character varying, n_dt_creazione::date))::text,
					'3' || n_cod_iuv, 
					n_de_rp_sogg_pag_anagrafica_pagatore, 
				    n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
					n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, 
				    n_dt_rp_dati_vers_data_esecuzione_pagamento, 
					n_dt_rp_dati_vers_data_esecuzione_pagamento, 
					n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
					n_de_rp_sogg_pag_email_pagatore,
				    substring(n_de_rp_dati_vers_dati_sing_vers_causale_versamento from 1 for 10000), 
				    (SELECT mygov_anagrafica_stato_id FROM mygov_anagrafica_stato WHERE cod_stato = 'NUOVO' AND de_tipo_stato = 'ad'), 
				    0, 
					n_dt_creazione, 
					n_dt_creazione,
					val_dati_sing_vers_iban_accredito, 
					val_dati_sing_vers_iban_appoggio,
					1,
					'C');

		END IF;

    END;
$$;


ALTER FUNCTION public.insert_mygov_avviso_digitale(n_mygov_ente_id bigint, n_dt_creazione timestamp without time zone, n_cod_iuv character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_sogg_pag_email_pagatore character varying) OWNER TO postgres;

--
-- TOC entry 303 (class 1255 OID 20853)
-- Name: insert_mygov_avviso_digitale_notifica_io(bigint, timestamp without time zone, character varying, character varying, character, character varying, date, numeric, character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.insert_mygov_avviso_digitale_notifica_io(n_mygov_ente_id bigint, n_dt_creazione timestamp without time zone, n_cod_iuv character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_sogg_pag_email_pagatore character varying) RETURNS void
    LANGUAGE plpgsql
    AS $$	

    DECLARE
	val_mygov_ente_id 		bigint;
	val_cod_ipa_ente		character varying(80);
	val_codice_fiscale_ente		character varying(35);
	val_de_nome_ente		character varying(35);
	val_application_code		character varying(2);
	val_dati_sing_vers_iban_accredito	character varying(35); 
	val_dati_sing_vers_iban_appoggio	character varying(35);
	
	found_mygov_flusso_avviso_digitale	mygov_flusso_avviso_digitale%ROWTYPE;

    BEGIN


	/* ************  LOCK SU mygov_ente_id TABELLA mygov_ente  ********** */

	SELECT   mygov_ente_id
	       , cod_ipa_ente
	       , codice_fiscale_ente
	       , substr(de_nome_ente, 0, 35) 
	       , application_code
		   , cod_rp_dati_vers_dati_sing_vers_iban_accredito
		   , cod_rp_dati_vers_dati_sing_vers_iban_appoggio
	FROM     mygov_ente 
	WHERE    mygov_ente_id = n_mygov_ente_id 
	INTO     val_mygov_ente_id
	       , val_cod_ipa_ente
	       , val_codice_fiscale_ente
	       , val_de_nome_ente
	       , val_application_code 
		   , val_dati_sing_vers_iban_accredito
		   , val_dati_sing_vers_iban_appoggio
   FOR UPDATE;   
   
    /* Recupero, se esiste, flusso fittizio di NOTIFICA_IO per l''ente */
   
	SELECT   flusso.*
	INTO     found_mygov_flusso_avviso_digitale
	FROM     mygov_flusso_avviso_digitale flusso	    
	WHERE    flusso.cod_fad_id_flusso = '_' || val_cod_ipa_ente || '_NOTIFICA-AVVISO-IO';
   
	/* Se il flusso non esiste lo inserisco*/
	
	IF found_mygov_flusso_avviso_digitale.mygov_flusso_avviso_digitale_id IS NULL THEN
	
		INSERT INTO mygov_flusso_avviso_digitale(
			mygov_flusso_avviso_digitale_id, 
			version, 
			cod_fad_id_dominio, 
            cod_fad_id_flusso, 
			mygov_anagrafica_stato_id, 
			cod_fad_tipo_flusso, 
            de_fad_file_path, 
            de_fad_filename,
			dt_creazione,
			dt_ultima_modifica
			)
		VALUES
			(nextval('mygov_flusso_avviso_digitale_id_seq'),
			0,
			 val_codice_fiscale_ente,
			'_' || val_cod_ipa_ente || '_NOTIFICA-AVVISO-IO',
			(SELECT mygov_anagrafica_stato_id FROM mygov_anagrafica_stato WHERE cod_stato = 'NOTIFICA_AVVISO_IO' AND de_tipo_stato = 'fnaIO'),
			'NA_IO',
			'n.d.',
			'n.d.',
			n_dt_creazione, 
			n_dt_creazione
			);
	
	END IF;
		

	    IF length(n_cod_iuv) = 15 THEN

			INSERT INTO mygov_avviso_digitale(
				    mygov_avviso_digitale_id, 
					version, 
					cod_ad_id_dominio, 
					de_ad_anag_beneficiario,  
					cod_ad_cod_avviso, 
					de_ad_sog_pag_anag_pagatore, 
				    cod_ad_sog_pag_id_univ_pag_tipo_id_univ, 
					cod_ad_sog_pag_id_univ_pag_cod_id_univ, 
				    dt_ad_data_scadenza_pagamento, 
					dt_ad_data_scadenza_avviso, 
					num_ad_importo_avviso, 
				    de_ad_email_soggetto,  /*deRpSoggPagEmailPagatore di mygov_dovuto se presente*/
					de_ad_desc_pagamento, 
				    cod_id_flusso_av,   /*Valorizzato con una stringa nel formato : _<codIpaEnte>_NOTIFICA-AVVISO-IO*/
					mygov_anagrafica_stato_id, 
				    num_ad_tentativi_invio, 
					dt_creazione, 
					dt_ultima_modifica,
					dati_sing_vers_iban_accredito,
					dati_sing_vers_iban_appoggio,
					tipo_pagamento,
					tipo_operazione)
			    VALUES 
					(nextval('mygov_avviso_digitale_id_seq'), 
					0, 
					val_codice_fiscale_ente, 
					val_de_nome_ente, 
					'0_' || val_application_code || '_' || n_cod_iuv, 
					n_de_rp_sogg_pag_anagrafica_pagatore, 
				    n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
					n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, 
				    n_dt_rp_dati_vers_data_esecuzione_pagamento, 
					n_dt_rp_dati_vers_data_esecuzione_pagamento, 
					n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
					n_de_rp_sogg_pag_email_pagatore,
				    substring(n_de_rp_dati_vers_dati_sing_vers_causale_versamento from 1 for 10000),
					'_' || val_cod_ipa_ente || '_NOTIFICA-AVVISO-IO',					
				    (SELECT mygov_anagrafica_stato_id FROM mygov_anagrafica_stato WHERE cod_stato = 'NUOVO' AND de_tipo_stato = 'naIO'), 
				    1, 
					n_dt_creazione, 
					n_dt_creazione,
					val_dati_sing_vers_iban_accredito, 
					val_dati_sing_vers_iban_appoggio,
					1,
					'C');	  

		ELSIF length(n_cod_iuv) = 17 THEN

			INSERT INTO mygov_avviso_digitale(
				    mygov_avviso_digitale_id, 
					version, 
					cod_ad_id_dominio, 
					de_ad_anag_beneficiario, 
					cod_ad_cod_avviso, 
					de_ad_sog_pag_anag_pagatore, 
				    cod_ad_sog_pag_id_univ_pag_tipo_id_univ, 
					cod_ad_sog_pag_id_univ_pag_cod_id_univ, 
				    dt_ad_data_scadenza_pagamento, 
					dt_ad_data_scadenza_avviso, 
					num_ad_importo_avviso, 
				    de_ad_email_soggetto, /*deRpSoggPagEmailPagatore di mygov_dovuto se presente */
					de_ad_desc_pagamento, 
				    cod_id_flusso_av,   /*Valorizzato con una stringa nel formato : _<codIpaEnte>_NOTIFICA-AVVISO-IO*/
					mygov_anagrafica_stato_id, 
				    num_ad_tentativi_invio, 
					dt_creazione, 
					dt_ultima_modifica,
					dati_sing_vers_iban_accredito,
					dati_sing_vers_iban_appoggio,
					tipo_pagamento,
					tipo_operazione)
			    VALUES 
					(nextval('mygov_avviso_digitale_id_seq'), 
					0, 
					val_codice_fiscale_ente, val_de_nome_ente, 
					'3_' || n_cod_iuv, 
					n_de_rp_sogg_pag_anagrafica_pagatore, 
				    n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
					n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, 
				    n_dt_rp_dati_vers_data_esecuzione_pagamento, 
					n_dt_rp_dati_vers_data_esecuzione_pagamento, 
					n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
					n_de_rp_sogg_pag_email_pagatore,
				    substring(n_de_rp_dati_vers_dati_sing_vers_causale_versamento from 1 for 10000), 
					'_' || val_cod_ipa_ente || '_NOTIFICA-AVVISO-IO',
				    (SELECT mygov_anagrafica_stato_id FROM mygov_anagrafica_stato WHERE cod_stato = 'NUOVO' AND de_tipo_stato = 'naIO'), 
				    1, 
					n_dt_creazione, 
					n_dt_creazione,
					val_dati_sing_vers_iban_accredito, 
					val_dati_sing_vers_iban_appoggio,
					1,
					'C');

		END IF;

    END;
$$;


ALTER FUNCTION public.insert_mygov_avviso_digitale_notifica_io(n_mygov_ente_id bigint, n_dt_creazione timestamp without time zone, n_cod_iuv character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_sogg_pag_email_pagatore character varying) OWNER TO postgres;

--
-- TOC entry 304 (class 1255 OID 20859)
-- Name: insert_mygov_dovuto(bigint, bigint, numeric, bigint, bigint, character varying, character varying, timestamp without time zone, timestamp without time zone, character, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, date, character varying, numeric, numeric, character varying, character varying, character varying, bigint, character varying, boolean, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.insert_mygov_dovuto(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, INOUT n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_dt_ultima_modifica timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean DEFAULT true, n_flg_genera_iuv boolean DEFAULT NULL::boolean, OUT result character varying, OUT result_desc character varying) RETURNS record
    LANGUAGE plpgsql
    AS $$
    DECLARE
	num_iud 		bigint := 0;
	num_iuv 		bigint := 0;
	num_cod_tipo 		bigint;
	obbligatorio 		boolean;
	stampa_data 		boolean;
	notifica_io		boolean;
	id_lock 		bigint;
        val_cod_ipa_ente	character varying;    
	
        iud_cursor		CURSOR FOR 
				SELECT * 
				FROM mygov_identificativo_univoco 
				WHERE identificativo = n_cod_iud 
				  AND cod_tipo_identificativo = 'IUD'
				  AND mygov_ente_id = n_mygov_ente_id;
				  
        iud_record		mygov_identificativo_univoco%ROWTYPE;
        iud_insertable		boolean := false;

        iuv_cursor		CURSOR FOR 
				SELECT * 
				FROM mygov_identificativo_univoco 
				WHERE identificativo = n_cod_iuv 
				  AND cod_tipo_identificativo = 'IUV'
				  AND mygov_ente_id = n_mygov_ente_id;
				  
        iuv_record		mygov_identificativo_univoco%ROWTYPE; 
        iuv_insertable		boolean := false;  

        n_dt_creazione_cod_iuv  timestamp without time zone; 
        	
    BEGIN  
    	

	/* ************  LOCK SU mygov_ente_id TABELLA mygov_ente  ********** */

	SELECT   mygov_ente_id 
	       , cod_ipa_ente
	FROM     mygov_ente 
	WHERE    mygov_ente_id = n_mygov_ente_id 
	INTO     id_lock
	       , val_cod_ipa_ente 
	FOR UPDATE;

	/* ************  VERIFICA esistenza tipo dovuto per ente ********** */ 

	select INTO num_cod_tipo sum(
	     (SELECT count(1)
		FROM mygov_ente_tipo_dovuto metd
		   , mygov_ente e
		WHERE  e.mygov_ente_id = metd.mygov_ente_id 
		   AND e.mygov_ente_id = n_mygov_ente_id
		   AND metd.cod_tipo = n_cod_tipo_dovuto) ); 
	    
	IF (num_cod_tipo = 0) THEN
		result := 'PAA_IMPORT_COD_TIPO_DOVUTO_INESISTENTE';
		result_desc := 'Il tipo dovuto non esiste per questo ente';
		RETURN;
	END IF;	
	
	/* ************  VERIFICA permessi dell''operatore su tipo dovuto del dovuto nuovo ********** */ 
		
	select INTO num_cod_tipo sum(
	     (SELECT count(1)
		FROM mygov_ente_tipo_dovuto metd
		   , mygov_operatore_ente_tipo_dovuto tdo
		   , mygov_operatore o
		   , mygov_utente u
		   , mygov_ente e
		WHERE  e.mygov_ente_id = metd.mygov_ente_id 
		   AND tdo.mygov_ente_tipo_dovuto_id = metd.mygov_ente_tipo_dovuto_id
		   AND tdo.mygov_operatore_id = o.mygov_operatore_id
		   AND o.cod_fed_user_id = u.cod_fed_user_id
		   AND o.cod_ipa_ente = e.cod_ipa_ente 
		   AND e.mygov_ente_id = n_mygov_ente_id
		   AND tdo.flg_attivo = true
		   AND metd.cod_tipo = n_cod_tipo_dovuto
		   AND u.mygov_utente_id = n_mygov_utente_id) 
		+
	    (SELECT count(1)
	       FROM mygov_utente u
	      WHERE split_part(u.cod_fed_user_id, '-', 2) = 'WS_USER' 
		AND u.mygov_utente_id = n_mygov_utente_id)); 
	    
	IF ((num_cod_tipo = 0) OR (num_cod_tipo > 1)) THEN
		result := 'PAA_IMPORT_OPERATORE_NO_PERMESSI';
		result_desc := 'L''operatore non ha i permessi per questo tipo dovuto e ente';
		RETURN;
	END IF;	

	/* ************  VERIFICA obbligatorieta data per tipo dovuto  ********** */ 
		
	SELECT flg_scadenza_obbligatoria, flg_stampa_data_scadenza
	INTO   obbligatorio, stampa_data 
	FROM   mygov_ente_tipo_dovuto
	WHERE  mygov_ente_id = n_mygov_ente_id
	AND    cod_tipo = n_cod_tipo_dovuto; 
	    
	IF ((obbligatorio OR stampa_data) AND n_dt_rp_dati_vers_data_esecuzione_pagamento IS NULL) THEN
		result := 'PAA_IMPORT_DATA_SCADENZA_PAGAMENTO_OBBLIGATORIA';
		result_desc := 'dataScadenzaPagamento deve essere valorizzata per questo tipo dovuto';
		RETURN;
	END IF;	

	IF (obbligatorio AND n_dt_rp_dati_vers_data_esecuzione_pagamento < n_dt_creazione::date) THEN
		result := 'PAA_IMPORT_DATA_SCADENZA_PAGAMENTO_OBBLIGATORIA';
		result_desc := 'dataScadenzaPagamento deve essere maggiore uguale alla data attuale per questo tipo dovuto';
		RETURN;
	END IF;		
               

	/* ************  VERIFICA ESISTENZA cod_iud SU TABELLA mygov_identificativo_univoco  ********** */

	OPEN iud_cursor;

	LOOP
		FETCH iud_cursor INTO iud_record;
		EXIT WHEN NOT FOUND;
		num_iud = num_iud + 1;
		iud_insertable := is_dovuto_annullato(iud_record.identificativo, 'IUD', iud_record.mygov_flusso_id);
		IF iud_insertable = false THEN			
			EXIT;  -- exit loop
		END IF;
	END LOOP;

	CLOSE iud_cursor;

	IF (num_iud > 0 AND iud_insertable = false) THEN
		result := 'PAA_IMPORT_IUD_DUPLICATO';	
		result_desc := 'IUD attivo presente nel database, non si accettano dovuti duplicati';
		RETURN;
	END IF;	
	

	IF n_cod_iuv IS NOT NULL THEN

	        /* ************     Valorizzo dt_creazione_cod_iuv solo se presente iuv in input    ********** */

		n_dt_creazione_cod_iuv := n_dt_creazione;
		
		/* ************  VERIFICA ESISTENZA cod_iuv SU TABELLA mygov_identificativo_univoco  ********** */
	    
		OPEN iuv_cursor;

		LOOP
			FETCH iuv_cursor INTO iuv_record;
			EXIT WHEN NOT FOUND;
			num_iuv = num_iuv + 1;
			iuv_insertable := is_dovuto_annullato(iuv_record.identificativo, 'IUV', iuv_record.mygov_flusso_id);
			IF iuv_insertable = false THEN			
				EXIT;  -- exit loop
			END IF;
		END LOOP;

		CLOSE iuv_cursor;

		IF (num_iuv > 0 AND iuv_insertable = false) THEN
			result := 'PAA_IMPORT_IUV_DUPLICATO';	
			result_desc := 'IUV attivo presente nel database, non si accettano dovuti duplicati';
			RETURN;	
		END IF;
							
		/* ************  Inserisce su mygov_avviso_digitale se ha iuv valido e flg su ente a true ed cod_funzionalita = 'AVVISO_DIGITALE' ********** */
		          							
		IF (SELECT  flg_attivo
		    FROM    mygov_ente_funzionalita 
		    WHERE   cod_ipa_ente = val_cod_ipa_ente
		    AND     cod_funzionalita = 'AVVISO_DIGITALE') AND (length(n_cod_iuv) = 15 OR length(n_cod_iuv) = 17) AND n_dt_rp_dati_vers_data_esecuzione_pagamento IS NOT NULL AND insert_avv_dig THEN
			PERFORM insert_mygov_avviso_digitale(
							    n_mygov_ente_id,
							    n_dt_creazione,
							    n_cod_iuv,
							    n_de_rp_sogg_pag_anagrafica_pagatore,
							    n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco,
							    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
							    n_dt_rp_dati_vers_data_esecuzione_pagamento,
							    n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
							    n_de_rp_dati_vers_dati_sing_vers_causale_versamento
							);
		END IF;	
		
		/* ************  Recupero flg_notifica_io dalla tabella mygov_ente_tipo_dovuto data la tipologia del dovuto da inserire (n_cod_tipo_dovuto character varying) ed l''id ente (n_mygov_ente_id bigint)********** */
		
		SELECT flg_notifica_io 
			INTO notifica_io
			FROM mygov_ente_tipo_dovuto
			WHERE mygov_ente_id = n_mygov_ente_id AND cod_tipo = n_cod_tipo_dovuto;
		
		/* ************  Inserisce su mygov_avviso_digitale se ha iuv valido, flg su ente a true, cod_funzionalita = 'NOTIFICA_AVVISI_IO' e per il dovuto ï¿œ prevista la notifica IO********** */
		
		IF (SELECT  flg_attivo
		    FROM    mygov_ente_funzionalita 
		    WHERE   cod_ipa_ente = val_cod_ipa_ente
		    AND     cod_funzionalita = 'NOTIFICA_AVVISI_IO') AND (length(n_cod_iuv) = 15 OR length(n_cod_iuv) = 17) AND notifica_io THEN
			PERFORM insert_mygov_avviso_digitale_notifica_io(
							    n_mygov_ente_id,
							    n_dt_creazione,
							    n_cod_iuv,
							    n_de_rp_sogg_pag_anagrafica_pagatore,
							    n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco,
							    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
							    n_dt_rp_dati_vers_data_esecuzione_pagamento,
							    n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
							    n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
								n_de_rp_sogg_pag_email_pagatore
							);
		END IF;	
		
		
		
	 END IF;
   
	IF (num_iud > 0 AND iud_insertable = true) THEN
		UPDATE mygov_identificativo_univoco SET mygov_flusso_id = n_mygov_flusso_id WHERE mygov_ente_id = n_mygov_ente_id AND cod_tipo_identificativo = 'IUD' AND identificativo = n_cod_iud;
	ELSEIF (num_iud = 0) THEN
		INSERT INTO mygov_identificativo_univoco(mygov_identificativo_univoco_id
							 ,"version"
							 , mygov_ente_id
							 , mygov_flusso_id
							 , cod_tipo_identificativo
							 , identificativo
							 , dt_inserimento)
						   VALUES (nextval('mygov_identificativo_univoco_id_seq')
							 , 0
							 , n_mygov_ente_id
							 , n_mygov_flusso_id
							 , 'IUD'
							 , n_cod_iud
							 , n_dt_creazione);							 
	END IF; 

	IF (n_cod_iuv IS NOT NULL AND num_iuv > 0 AND iuv_insertable = true) THEN
		UPDATE mygov_identificativo_univoco SET mygov_flusso_id = n_mygov_flusso_id WHERE mygov_ente_id = n_mygov_ente_id AND cod_tipo_identificativo = 'IUV' AND identificativo = n_cod_iuv;	
	ELSIF (n_cod_iuv IS NOT NULL AND num_iuv = 0) THEN
		INSERT INTO mygov_identificativo_univoco(mygov_identificativo_univoco_id
							,"version"
							, mygov_ente_id
							, mygov_flusso_id
							, cod_tipo_identificativo
							, identificativo
							, dt_inserimento)
						   VALUES (nextval('mygov_identificativo_univoco_id_seq')
							, 0
							, n_mygov_ente_id
							, n_mygov_flusso_id
							, 'IUV'
							, n_cod_iuv
							, n_dt_creazione);	
	END IF;	   
	  	    
	  INSERT INTO mygov_dovuto(
	    mygov_dovuto_id, "version", flg_dovuto_attuale, mygov_flusso_id, 
	    num_riga_flusso, mygov_anagrafica_stato_id, mygov_carrello_id, 
	    cod_iud, cod_iuv, dt_creazione, dt_ultima_modifica, cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
	    cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, de_rp_sogg_pag_anagrafica_pagatore, 
	    de_rp_sogg_pag_indirizzo_pagatore, de_rp_sogg_pag_civico_pagatore, 
	    cod_rp_sogg_pag_cap_pagatore, de_rp_sogg_pag_localita_pagatore, 
	    de_rp_sogg_pag_provincia_pagatore, cod_rp_sogg_pag_nazione_pagatore, 
	    de_rp_sogg_pag_email_pagatore, dt_rp_dati_vers_data_esecuzione_pagamento, 
	    cod_rp_dati_vers_tipo_versamento, num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
	    num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, cod_tipo_dovuto, 
	    de_rp_dati_vers_dati_sing_vers_causale_versamento, de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, dt_creazione_cod_iuv,
		bilancio,flg_genera_iuv)

	    VALUES (nextval('mygov_dovuto_mygov_dovuto_id_seq'), 0, true, n_mygov_flusso_id, 
	    n_num_riga_flusso, n_mygov_anagrafica_stato_id, n_mygov_carrello_id, 
	    n_cod_iud, n_cod_iuv, n_dt_creazione, n_dt_ultima_modifica, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
	    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, n_de_rp_sogg_pag_anagrafica_pagatore, 
	    n_de_rp_sogg_pag_indirizzo_pagatore, n_de_rp_sogg_pag_civico_pagatore, 
	    n_cod_rp_sogg_pag_cap_pagatore, n_de_rp_sogg_pag_localita_pagatore, 
	    n_de_rp_sogg_pag_provincia_pagatore, n_cod_rp_sogg_pag_nazione_pagatore, 
	    n_de_rp_sogg_pag_email_pagatore, n_dt_rp_dati_vers_data_esecuzione_pagamento, 
	    n_cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
	    n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, n_cod_tipo_dovuto,  
	    n_de_rp_dati_vers_dati_sing_vers_causale_versamento, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_dt_creazione_cod_iuv,
		n_bilancio,n_flg_genera_iuv); 
    END;
$$;


ALTER FUNCTION public.insert_mygov_dovuto(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, INOUT n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_dt_ultima_modifica timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean, n_flg_genera_iuv boolean, OUT result character varying, OUT result_desc character varying) OWNER TO postgres;

--
-- TOC entry 316 (class 1255 OID 364025)
-- Name: insert_mygov_dovuto_elaborato(bigint, bigint, numeric, bigint, bigint, character varying, character varying, timestamp without time zone, character varying, character, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, date, character varying, numeric, numeric, character varying, character varying, character varying, bigint, boolean); Type: FUNCTION; Schema: public; Owner: mypay4
--

CREATE FUNCTION public.insert_mygov_dovuto_elaborato(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, INOUT n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_de_rp_versione_oggetto character varying, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, insert_avv_dig boolean DEFAULT true, OUT result character varying, OUT result_desc character varying) RETURNS record
    LANGUAGE plpgsql
    AS $$
    DECLARE
	id_lock					bigint;
	num_cod_tipo				bigint;
	dovuto_cancellare			mygov_dovuto;
        val_cod_ipa_ente			character varying;
	val_application_code			character varying(2);	
	notifica_io			boolean;
    BEGIN  

	/* ************  LOCK SU mygov_ente_id TABELLA mygov_ente  ********** */

	SELECT   mygov_ente_id 
	       , cod_ipa_ente
	       , application_code
	FROM     mygov_ente 
	WHERE    mygov_ente_id = n_mygov_ente_id 
	INTO     id_lock
	       , val_cod_ipa_ente 
	       , val_application_code
	FOR UPDATE;



	/* ************  VERIFICA esistenza tipo dovuto per ente ********** */ 

	select INTO num_cod_tipo sum(
	     (SELECT count(1)
		FROM mygov_ente_tipo_dovuto metd
		   , mygov_ente e
		WHERE  e.mygov_ente_id = metd.mygov_ente_id 
		   AND e.mygov_ente_id = n_mygov_ente_id
		   AND metd.cod_tipo = n_cod_tipo_dovuto) ); 
	    
	IF (num_cod_tipo = 0) THEN
		result := 'PAA_IMPORT_COD_TIPO_DOVUTO_INESISTENTE';
		result_desc := 'Il tipo dovuto non esiste per questo ente';
		RETURN;
	END IF;		

	/* ************  VERIFICA permessi dell''operatore su tipo dovuto del dovuto nuovo ********** */ 

	SELECT INTO num_cod_tipo sum(
	     (SELECT count(1)
		FROM mygov_ente_tipo_dovuto metd
		   , mygov_operatore_ente_tipo_dovuto tdo
		   , mygov_operatore o
		   , mygov_utente u
		   , mygov_ente e
		WHERE  e.mygov_ente_id = metd.mygov_ente_id 
		   AND tdo.mygov_ente_tipo_dovuto_id = metd.mygov_ente_tipo_dovuto_id
		   AND tdo.mygov_operatore_id = o.mygov_operatore_id
		   AND o.cod_fed_user_id = u.cod_fed_user_id
		   AND o.cod_ipa_ente = e.cod_ipa_ente 
		   AND e.mygov_ente_id = n_mygov_ente_id
		   AND tdo.flg_attivo = true
		   AND metd.cod_tipo = n_cod_tipo_dovuto
		   AND u.mygov_utente_id = n_mygov_utente_id) 
		+
	    (SELECT count(1)
	       FROM mygov_utente u
	      WHERE split_part(u.cod_fed_user_id, '-', 2) = 'WS_USER' 
		AND u.mygov_utente_id = n_mygov_utente_id));
	    
	IF ((num_cod_tipo = 0) OR (num_cod_tipo > 1)) THEN
	result := 'PAA_IMPORT_OPERATORE_NO_PERMESSI';
		result_desc := 'L''operatore non ha i permessi per questo tipo dovuto e ente';			
		RETURN;
	END IF;	     

	/* ***************** TROVO ID DA CANCELLARE E TIPO DOVUTO *********** */
	SELECT   
	    dovuto.*
	INTO
	    dovuto_cancellare
	FROM     mygov_dovuto dovuto
	   , mygov_flusso flusso
	   , mygov_anagrafica_stato stato	    
	WHERE    flusso.mygov_flusso_id = dovuto.mygov_flusso_id
	AND      stato.mygov_anagrafica_stato_id = dovuto.mygov_anagrafica_stato_id
	AND      stato.cod_stato = 'INSERIMENTO_DOVUTO'
	AND      stato.de_tipo_stato = 'dovuto'
	AND      dovuto.cod_iud = n_cod_iud
	AND      dovuto.flg_dovuto_attuale = true
	AND      flusso.flg_attivo = true
	AND      flusso.mygov_ente_id = n_mygov_ente_id;  


	IF dovuto_cancellare.mygov_dovuto_id IS NULL THEN 
		result := 'PAA_IMPORT_DOVUTO_NON_PRESENTE'; 
		result_desc := 'Dovuto non presente o in pagamento nel database, impossibile modificarlo';
		RETURN; 
	END IF; 			  

	/* ************  VERIFICA permessi dell''operatore su tipo dovuto del dovuto esistente ********** */

	SELECT INTO num_cod_tipo sum(
	     (SELECT count(1) 	
	FROM mygov_ente_tipo_dovuto metd
	   , mygov_flusso mf
	   , mygov_operatore_ente_tipo_dovuto tdo
	   , mygov_operatore o
	   , mygov_utente u
	WHERE metd.cod_tipo = dovuto_cancellare.cod_tipo_dovuto 
		AND metd.mygov_ente_id = mf.mygov_ente_id 
		AND mf.mygov_flusso_id = dovuto_cancellare.mygov_flusso_id
		AND tdo.mygov_ente_tipo_dovuto_id = metd.mygov_ente_tipo_dovuto_id
		AND tdo.flg_attivo = true
		AND tdo.mygov_operatore_id = o.mygov_operatore_id
		AND o.cod_fed_user_id = u.cod_fed_user_id
		AND u.mygov_utente_id = n_mygov_utente_id) 
		+
	    (SELECT count(1)
	       FROM mygov_utente u
	      WHERE split_part(u.cod_fed_user_id, '-', 2) = 'WS_USER' 
		AND u.mygov_utente_id = n_mygov_utente_id));
	    
	IF ((num_cod_tipo = 0) OR (num_cod_tipo > 1)) THEN
			result := 'PAA_IMPORT_OPERATORE_NO_PERMESSI';
		result_desc := 'L''operatore non ha i permessi per questo tipo dovuto e ente';
		RETURN;
	END IF;	 		  

	PERFORM sposta_dovuto_in_dovuto_elaborato(dovuto_cancellare.mygov_dovuto_id, n_mygov_flusso_id, n_num_riga_flusso, 
	    n_mygov_anagrafica_stato_id, dovuto_cancellare.mygov_carrello_id, dovuto_cancellare.cod_iud, dovuto_cancellare.cod_iuv, n_dt_creazione, 
	    n_de_rp_versione_oggetto, dovuto_cancellare.cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, dovuto_cancellare.cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, 
	    dovuto_cancellare.de_rp_sogg_pag_anagrafica_pagatore, dovuto_cancellare.de_rp_sogg_pag_indirizzo_pagatore, dovuto_cancellare.de_rp_sogg_pag_civico_pagatore, 
	    dovuto_cancellare.cod_rp_sogg_pag_cap_pagatore, dovuto_cancellare.de_rp_sogg_pag_localita_pagatore, dovuto_cancellare.de_rp_sogg_pag_provincia_pagatore, 
	    dovuto_cancellare.cod_rp_sogg_pag_nazione_pagatore, dovuto_cancellare.de_rp_sogg_pag_email_pagatore, dovuto_cancellare.dt_rp_dati_vers_data_esecuzione_pagamento, 
	    dovuto_cancellare.cod_rp_dati_vers_tipo_versamento, dovuto_cancellare.num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
	    dovuto_cancellare.num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, dovuto_cancellare.cod_tipo_dovuto, dovuto_cancellare.de_rp_dati_vers_dati_sing_vers_causale_versamento, 
	    dovuto_cancellare.de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, dovuto_cancellare.bilancio);

		/* Logica per comunicazione di annullamento di un avviso digitale esistente*/
	IF (dovuto_cancellare.cod_iuv IS NOT NULL AND
		(SELECT  flg_attivo
		    FROM    mygov_ente_funzionalita 
		    WHERE   cod_ipa_ente = val_cod_ipa_ente
		    AND     cod_funzionalita = 'AVVISO_DIGITALE')) AND insert_avv_dig THEN
			
		/* gestire logica annullamento come nella classe java */	
			
		/* Se non ï¿½ stata ancora sottomessa la richiesta di creazione di un nuovo avviso digitale (tipo_operazione = C), ï¿½ sufficiente settare lo stato ad ANNULLATO*/
		UPDATE mygov_avviso_digitale 
		SET    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'ANNULLATO'
						    AND    de_tipo_stato = 'ad'),
			  tipo_operazione = 'D',
			  dt_ultima_modifica = NOW()
		WHERE  (cod_ad_cod_avviso = '0' || val_application_code || n_cod_iuv OR cod_ad_cod_avviso = '3' || n_cod_iuv)
		  AND  mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'NUOVO'
						    AND    de_tipo_stato = 'ad')
		  AND tipo_operazione = 'C';
		 
		/* Creo una nuova richiesta di cancellazione di avviso digitale (tipo_operazione = C) se 
			- non e'stato gia' annullato
			- non e'stata gia' creata richiesta di cancellazione */
		
		UPDATE mygov_avviso_digitale 
		SET    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'NUOVO'
						    AND    de_tipo_stato = 'ad'),
			  tipo_operazione = 'D',
			  dt_ultima_modifica = NOW()
		WHERE  (cod_ad_cod_avviso = '0' || val_application_code || n_cod_iuv OR cod_ad_cod_avviso = '3' || n_cod_iuv)
		  AND  mygov_anagrafica_stato_id != (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'ANNULLATO'
						    AND    de_tipo_stato = 'ad')
		 AND tipo_operazione != 'D';
	END IF;
	
	/* ************  Recupero flg_notifica_io dalla tabella mygov_ente_tipo_dovuto data la tipologia del dovuto da inserire (n_cod_tipo_dovuto character varying) ed l''id ente (n_mygov_ente_id bigint)********** */
	
	SELECT flg_notifica_io 
		INTO notifica_io
		FROM mygov_ente_tipo_dovuto
		WHERE mygov_ente_id = n_mygov_ente_id AND cod_tipo = n_cod_tipo_dovuto;
	
	IF (dovuto_cancellare.cod_iuv IS NOT NULL AND notifica_io AND
		(SELECT  flg_attivo
		    FROM    mygov_ente_funzionalita 
		    WHERE   cod_ipa_ente = val_cod_ipa_ente
		    AND     cod_funzionalita = 'NOTIFICA_AVVISI_IO')) THEN
		UPDATE mygov_avviso_digitale 
		SET    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'ANNULLATO'
						    AND    de_tipo_stato = 'naIO'),
			tipo_operazione = 'D',
			dt_ultima_modifica = NOW()
		WHERE  (cod_ad_cod_avviso = '0_' || val_application_code || '_' || dovuto_cancellare.cod_iuv OR cod_ad_cod_avviso = '3_' || dovuto_cancellare.cod_iuv)
		  AND  mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
						    FROM   mygov_anagrafica_stato
						    WHERE  cod_stato = 'NUOVO'
						    AND    de_tipo_stato = 'naIO');
	END IF;
	
	
    END;
$$;


ALTER FUNCTION public.insert_mygov_dovuto_elaborato(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, INOUT n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_de_rp_versione_oggetto character varying, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, insert_avv_dig boolean, OUT result character varying, OUT result_desc character varying) OWNER TO mypay4;

--
-- TOC entry 315 (class 1255 OID 364027)
-- Name: insert_mygov_dovuto_elaborato_noinout(bigint, bigint, numeric, bigint, bigint, character varying, character varying, timestamp without time zone, character varying, character, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, date, character varying, numeric, numeric, character varying, character varying, character varying, bigint, boolean); Type: FUNCTION; Schema: public; Owner: mypay4
--

CREATE FUNCTION public.insert_mygov_dovuto_elaborato_noinout(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_de_rp_versione_oggetto character varying, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, insert_avv_dig boolean DEFAULT true, OUT result character varying, OUT result_desc character varying) RETURNS record
    LANGUAGE plpgsql
    AS $$  
  declare
    n_num_riga_flusso_out numeric;
  BEGIN        
    select * from public.insert_mygov_dovuto_elaborato(n_mygov_ente_id, n_mygov_flusso_id, n_num_riga_flusso, n_mygov_anagrafica_stato_id, n_mygov_carrello_id, n_cod_iud, n_cod_iuv, n_dt_creazione, n_de_rp_versione_oggetto, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, n_de_rp_sogg_pag_anagrafica_pagatore, n_de_rp_sogg_pag_indirizzo_pagatore, n_de_rp_sogg_pag_civico_pagatore, n_cod_rp_sogg_pag_cap_pagatore, n_de_rp_sogg_pag_localita_pagatore, n_de_rp_sogg_pag_provincia_pagatore, n_cod_rp_sogg_pag_nazione_pagatore, n_de_rp_sogg_pag_email_pagatore, n_dt_rp_dati_vers_data_esecuzione_pagamento, n_cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, n_cod_tipo_dovuto, n_de_rp_dati_vers_dati_sing_vers_causale_versamento, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_mygov_utente_id, insert_avv_dig)
    into n_num_riga_flusso_out, result, result_desc;
    END;
$$;


ALTER FUNCTION public.insert_mygov_dovuto_elaborato_noinout(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_de_rp_versione_oggetto character varying, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, insert_avv_dig boolean, OUT result character varying, OUT result_desc character varying) OWNER TO mypay4;

--
-- TOC entry 317 (class 1255 OID 389969)
-- Name: insert_mygov_dovuto_noinout(bigint, bigint, numeric, bigint, bigint, character varying, character varying, timestamp without time zone, timestamp without time zone, character, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, date, character varying, numeric, numeric, character varying, character varying, character varying, bigint, character varying, boolean); Type: FUNCTION; Schema: public; Owner: mypay4
--

CREATE FUNCTION public.insert_mygov_dovuto_noinout(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_dt_ultima_modifica timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean DEFAULT true) RETURNS void
    LANGUAGE plpgsql
    AS $$

    DECLARE
	num_iud 		bigint := 0;
	num_iuv 		bigint := 0;
	num_cod_tipo 		bigint;
	obbligatorio 		boolean;
	notifica_io		boolean;
	id_lock 		bigint;
        val_cod_ipa_ente	character varying;    
	
        iud_cursor		CURSOR FOR 
				SELECT * 
				FROM mygov_identificativo_univoco 
				WHERE identificativo = n_cod_iud 
				  AND cod_tipo_identificativo = 'IUD'
				  AND mygov_ente_id = n_mygov_ente_id;
				  
        iud_record		mygov_identificativo_univoco%ROWTYPE;
        iud_insertable		boolean := false;

        iuv_cursor		CURSOR FOR 
				SELECT * 
				FROM mygov_identificativo_univoco 
				WHERE identificativo = n_cod_iuv 
				  AND cod_tipo_identificativo = 'IUV'
				  AND mygov_ente_id = n_mygov_ente_id;
				  
        iuv_record		mygov_identificativo_univoco%ROWTYPE; 
        iuv_insertable		boolean := false;  

        n_dt_creazione_cod_iuv  timestamp without time zone; 
        	
    BEGIN  
    	

	/* ************  LOCK SU mygov_ente_id TABELLA mygov_ente  ********** */

	SELECT   mygov_ente_id 
	       , cod_ipa_ente
	FROM     mygov_ente 
	WHERE    mygov_ente_id = n_mygov_ente_id 
	INTO     id_lock
	       , val_cod_ipa_ente 
	FOR UPDATE;


	/* ************  VERIFICA permessi dell''operatore su tipo dovuto del dovuto nuovo ********** */ 
		
	select INTO num_cod_tipo sum(
	     (SELECT count(1)
		FROM mygov_ente_tipo_dovuto metd
		   , mygov_operatore_ente_tipo_dovuto tdo
		   , mygov_operatore o
		   , mygov_utente u
		   , mygov_ente e
		WHERE  e.mygov_ente_id = metd.mygov_ente_id 
		   AND tdo.mygov_ente_tipo_dovuto_id = metd.mygov_ente_tipo_dovuto_id
		   AND tdo.mygov_operatore_id = o.mygov_operatore_id
		   AND o.cod_fed_user_id = u.cod_fed_user_id
		   AND o.cod_ipa_ente = e.cod_ipa_ente 
		   AND e.mygov_ente_id = n_mygov_ente_id
		   AND tdo.flg_attivo = true
		   AND metd.cod_tipo = n_cod_tipo_dovuto
		   AND u.mygov_utente_id = n_mygov_utente_id) 
		+
	    (SELECT count(1)
	       FROM mygov_utente u
	      WHERE split_part(u.cod_fed_user_id, '-', 2) = 'WS_USER' 
		AND u.mygov_utente_id = n_mygov_utente_id)); 
	    
	IF ((num_cod_tipo = 0) OR (num_cod_tipo > 1)) THEN
		PERFORM insert_mygov_dovuto_rifiutato(n_mygov_flusso_id
						    ,n_num_riga_flusso 
						    ,n_cod_iud
						    ,n_cod_iuv
						    ,(select mygov_anagrafica_stato_id from mygov_anagrafica_stato where cod_stato = 'ERROR_DOVUTO')
						    ,'L'' operatore non ha i permessi sul tipo dovuto per inserire il dovuto; per questo ente'
						    ,n_dt_creazione
						    ,n_dt_creazione);

			
		RETURN;
	END IF;	

	/* ************  VERIFICA obbligatorieta data per tipo dovuto  ********** */ 
		
	SELECT flg_scadenza_obbligatoria OR flg_stampa_data_scadenza
	INTO   obbligatorio 
	FROM   mygov_ente_tipo_dovuto
	WHERE  mygov_ente_id = n_mygov_ente_id
	AND    cod_tipo = n_cod_tipo_dovuto; 
	    
	IF (obbligatorio AND n_dt_rp_dati_vers_data_esecuzione_pagamento IS NULL) THEN
		PERFORM insert_mygov_dovuto_rifiutato(n_mygov_flusso_id
						    ,n_num_riga_flusso 
						    ,n_cod_iud
						    ,n_cod_iuv
						    ,(select mygov_anagrafica_stato_id from mygov_anagrafica_stato where cod_stato = 'ERROR_DOVUTO')
						    ,'dataScadenzaPagamento deve essere valorizzata per questo tipo dovuto'
						    ,n_dt_creazione
						    ,n_dt_creazione);			
		RETURN;
	END IF;		
               

	/* ************  VERIFICA ESISTENZA cod_iud SU TABELLA mygov_identificativo_univoco  ********** */

	OPEN iud_cursor;

	LOOP
		FETCH iud_cursor INTO iud_record;
		EXIT WHEN NOT FOUND;
		num_iud = num_iud + 1;
		iud_insertable := is_dovuto_annullato(iud_record.identificativo, 'IUD', iud_record.mygov_flusso_id);
		IF iud_insertable = false THEN			
			EXIT;  -- exit loop
		END IF;
	END LOOP;

	CLOSE iud_cursor;

	IF (num_iud > 0 AND iud_insertable = false) THEN
                PERFORM insert_mygov_dovuto_rifiutato(n_mygov_flusso_id
						    ,n_num_riga_flusso 
						    ,n_cod_iud
						    ,n_cod_iuv
						    ,(select mygov_anagrafica_stato_id from mygov_anagrafica_stato where cod_stato = 'ERROR_DOVUTO')
						    ,'IUD attivo presente nel database, non si accettano dovuti duplicati'
		                                    ,n_dt_creazione
		                                    ,n_dt_ultima_modifica);
		RETURN;	

	END IF;	
	

	IF n_cod_iuv IS NOT NULL THEN

	        /* ************     Valorizzo dt_creazione_cod_iuv solo se presente iuv in input    ********** */

		n_dt_creazione_cod_iuv := n_dt_creazione;
		
		/* ************  VERIFICA ESISTENZA cod_iuv SU TABELLA mygov_identificativo_univoco  ********** */
	    
		OPEN iuv_cursor;

		LOOP
			FETCH iuv_cursor INTO iuv_record;
			EXIT WHEN NOT FOUND;
			num_iuv = num_iuv + 1;
			iuv_insertable := is_dovuto_annullato(iuv_record.identificativo, 'IUV', iuv_record.mygov_flusso_id);
			IF iuv_insertable = false THEN			
				EXIT;  -- exit loop
			END IF;
		END LOOP;

		CLOSE iuv_cursor;

		IF (num_iuv > 0 AND iuv_insertable = false) THEN
			PERFORM insert_mygov_dovuto_rifiutato(n_mygov_flusso_id
							    ,n_num_riga_flusso 
							    ,n_cod_iud
							    ,n_cod_iuv
							    ,(select mygov_anagrafica_stato_id from mygov_anagrafica_stato where cod_stato = 'ERROR_DOVUTO')
							    ,'IUV attivo presente nel database, non si accettano dovuti duplicati'
							    ,n_dt_creazione
							    ,n_dt_ultima_modifica);
			RETURN;	
		END IF;

		INSERT INTO mygov_identificativo_univoco(mygov_identificativo_univoco_id
							,"version"
							, mygov_ente_id
							, mygov_flusso_id
							, cod_tipo_identificativo
							, identificativo
							, dt_inserimento)
						   VALUES (nextval('mygov_identificativo_univoco_id_seq')
							, 0
							, n_mygov_ente_id
							, n_mygov_flusso_id
							, 'IUV'
							, n_cod_iuv
							, n_dt_creazione);
							
		/* ************  Inserisce su mygov_avviso_digitale se ha iuv valido e flg su ente a true ed cod_funzionalita = 'AVVISO_DIGITALE' ********** */
		          							
		IF (SELECT  flg_attivo
		    FROM    mygov_ente_funzionalita 
		    WHERE   cod_ipa_ente = val_cod_ipa_ente
		    AND     cod_funzionalita = 'AVVISO_DIGITALE') AND (length(n_cod_iuv) = 15 OR length(n_cod_iuv) = 17) AND insert_avv_dig THEN
			PERFORM insert_mygov_avviso_digitale(
							    n_mygov_ente_id,
							    n_dt_creazione,
							    n_cod_iuv,
							    n_de_rp_sogg_pag_anagrafica_pagatore,
							    n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco,
							    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
							    n_dt_rp_dati_vers_data_esecuzione_pagamento,
							    n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
							    n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
								n_de_rp_sogg_pag_email_pagatore
							);
		END IF;	
		
		/* ************  Recupero flg_notifica_io dalla tabella mygov_ente_tipo_dovuto data la tipologia del dovuto da inserire (n_cod_tipo_dovuto character varying) ed l''id ente (n_mygov_ente_id bigint)********** */
		
		SELECT flg_notifica_io 
			INTO notifica_io
			FROM mygov_ente_tipo_dovuto
			WHERE mygov_ente_id = n_mygov_ente_id AND cod_tipo = n_cod_tipo_dovuto;
		
		/* ************  Inserisce su mygov_avviso_digitale se ha iuv valido, flg su ente a true, cod_funzionalita = 'NOTIFICA_AVVISI_IO' e per il dovuto ï¿½ prevista la notifica IO********** */
		
		IF (SELECT  flg_attivo
		    FROM    mygov_ente_funzionalita 
		    WHERE   cod_ipa_ente = val_cod_ipa_ente
		    AND     cod_funzionalita = 'NOTIFICA_AVVISI_IO') AND (length(n_cod_iuv) = 15 OR length(n_cod_iuv) = 17) AND notifica_io THEN
			PERFORM insert_mygov_avviso_digitale_notifica_io(
							    n_mygov_ente_id,
							    n_dt_creazione,
							    n_cod_iuv,
							    n_de_rp_sogg_pag_anagrafica_pagatore,
							    n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco,
							    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
							    n_dt_rp_dati_vers_data_esecuzione_pagamento,
							    n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
							    n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
								n_de_rp_sogg_pag_email_pagatore
							);
		END IF;	
		
		
		
	 END IF;
   
    
         INSERT INTO mygov_identificativo_univoco(mygov_identificativo_univoco_id
                                                 ,"version"
                                                 , mygov_ente_id
                                                 , mygov_flusso_id
                                                 , cod_tipo_identificativo
                                                 , identificativo
                                                 , dt_inserimento)
                                           VALUES (nextval('mygov_identificativo_univoco_id_seq')
                                                 , 0
                                                 , n_mygov_ente_id
                                                 , n_mygov_flusso_id
                                                 , 'IUD'
                                                 , n_cod_iud
                                                 , n_dt_creazione);
	    	    
	  INSERT INTO mygov_dovuto(
	    mygov_dovuto_id, "version", flg_dovuto_attuale, mygov_flusso_id, 
	    num_riga_flusso, mygov_anagrafica_stato_id, mygov_carrello_id, 
	    cod_iud, cod_iuv, dt_creazione, dt_ultima_modifica, cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
	    cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, de_rp_sogg_pag_anagrafica_pagatore, 
	    de_rp_sogg_pag_indirizzo_pagatore, de_rp_sogg_pag_civico_pagatore, 
	    cod_rp_sogg_pag_cap_pagatore, de_rp_sogg_pag_localita_pagatore, 
	    de_rp_sogg_pag_provincia_pagatore, cod_rp_sogg_pag_nazione_pagatore, 
	    de_rp_sogg_pag_email_pagatore, dt_rp_dati_vers_data_esecuzione_pagamento, 
	    cod_rp_dati_vers_tipo_versamento, num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
	    num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, cod_tipo_dovuto, 
	    de_rp_dati_vers_dati_sing_vers_causale_versamento, de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, dt_creazione_cod_iuv,
		bilancio)

	    VALUES (nextval('mygov_dovuto_mygov_dovuto_id_seq'), 0, true, n_mygov_flusso_id, 
	    n_num_riga_flusso, n_mygov_anagrafica_stato_id, n_mygov_carrello_id, 
	    n_cod_iud, n_cod_iuv, n_dt_creazione, n_dt_ultima_modifica, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
	    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, n_de_rp_sogg_pag_anagrafica_pagatore, 
	    n_de_rp_sogg_pag_indirizzo_pagatore, n_de_rp_sogg_pag_civico_pagatore, 
	    n_cod_rp_sogg_pag_cap_pagatore, n_de_rp_sogg_pag_localita_pagatore, 
	    n_de_rp_sogg_pag_provincia_pagatore, n_cod_rp_sogg_pag_nazione_pagatore, 
	    n_de_rp_sogg_pag_email_pagatore, n_dt_rp_dati_vers_data_esecuzione_pagamento, 
	    n_cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
	    n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, n_cod_tipo_dovuto,  
	    n_de_rp_dati_vers_dati_sing_vers_causale_versamento, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_dt_creazione_cod_iuv,
		n_bilancio);           
    END;
$$;


ALTER FUNCTION public.insert_mygov_dovuto_noinout(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_dt_ultima_modifica timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean) OWNER TO mypay4;

--
-- TOC entry 313 (class 1255 OID 202008)
-- Name: insert_mygov_dovuto_noinout(bigint, bigint, numeric, bigint, bigint, character varying, character varying, timestamp without time zone, timestamp without time zone, character, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, date, character varying, numeric, numeric, character varying, character varying, character varying, bigint, character varying, boolean, boolean); Type: FUNCTION; Schema: public; Owner: mypay4
--

CREATE FUNCTION public.insert_mygov_dovuto_noinout(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_dt_ultima_modifica timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean DEFAULT true, n_flg_genera_iuv boolean DEFAULT NULL::boolean, OUT result character varying, OUT result_desc character varying) RETURNS record
    LANGUAGE plpgsql
    AS $$  
  declare
    n_num_riga_flusso_out numeric;
  BEGIN        
    select * from public.insert_mygov_dovuto(n_mygov_ente_id, n_mygov_flusso_id, n_num_riga_flusso, n_mygov_anagrafica_stato_id, 
      n_mygov_carrello_id, n_cod_iud, n_cod_iuv, n_dt_creazione, n_dt_ultima_modifica, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
      n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, n_de_rp_sogg_pag_anagrafica_pagatore, n_de_rp_sogg_pag_indirizzo_pagatore, 
      n_de_rp_sogg_pag_civico_pagatore , n_cod_rp_sogg_pag_cap_pagatore, n_de_rp_sogg_pag_localita_pagatore, n_de_rp_sogg_pag_provincia_pagatore,
      n_cod_rp_sogg_pag_nazione_pagatore, n_de_rp_sogg_pag_email_pagatore, n_dt_rp_dati_vers_data_esecuzione_pagamento, 
      n_cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
      n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, n_cod_tipo_dovuto, n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
      n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_mygov_utente_id, n_bilancio, insert_avv_dig, n_flg_genera_iuv)
    into n_num_riga_flusso_out, result, result_desc;
    END;
$$;


ALTER FUNCTION public.insert_mygov_dovuto_noinout(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_dt_ultima_modifica timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean, n_flg_genera_iuv boolean, OUT result character varying, OUT result_desc character varying) OWNER TO mypay4;

--
-- TOC entry 312 (class 1255 OID 32785)
-- Name: insert_mygov_dovuto_ret(bigint, bigint, numeric, bigint, bigint, character varying, character varying, timestamp without time zone, timestamp without time zone, character, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, date, character varying, numeric, numeric, character varying, character varying, character varying, bigint, character varying, boolean, boolean); Type: FUNCTION; Schema: public; Owner: mypay4
--

CREATE FUNCTION public.insert_mygov_dovuto_ret(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, INOUT n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_dt_ultima_modifica timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean DEFAULT true, n_flg_genera_iuv boolean DEFAULT NULL::boolean, OUT result character varying, OUT result_desc character varying) RETURNS record
    LANGUAGE plpgsql
    AS $$
    DECLARE
	num_iud 		bigint := 0;
	num_iuv 		bigint := 0;
	num_cod_tipo 		bigint;
	obbligatorio 		boolean;
	stampa_data 		boolean;
	notifica_io		boolean;
	id_lock 		bigint;
        val_cod_ipa_ente	character varying;    
	
        iud_cursor		CURSOR FOR 
				SELECT * 
				FROM mygov_identificativo_univoco 
				WHERE identificativo = n_cod_iud 
				  AND cod_tipo_identificativo = 'IUD'
				  AND mygov_ente_id = n_mygov_ente_id;
				  
        iud_record		mygov_identificativo_univoco%ROWTYPE;
        iud_insertable		boolean := false;

        iuv_cursor		CURSOR FOR 
				SELECT * 
				FROM mygov_identificativo_univoco 
				WHERE identificativo = n_cod_iuv 
				  AND cod_tipo_identificativo = 'IUV'
				  AND mygov_ente_id = n_mygov_ente_id;
				  
        iuv_record		mygov_identificativo_univoco%ROWTYPE; 
        iuv_insertable		boolean := false;  

        n_dt_creazione_cod_iuv  timestamp without time zone; 
        	
    BEGIN  
    	

	/* ************  LOCK SU mygov_ente_id TABELLA mygov_ente  ********** */

	SELECT   mygov_ente_id 
	       , cod_ipa_ente
	FROM     mygov_ente 
	WHERE    mygov_ente_id = n_mygov_ente_id 
	INTO     id_lock
	       , val_cod_ipa_ente 
	FOR UPDATE;

	/* ************  VERIFICA esistenza tipo dovuto per ente ********** */ 

	select INTO num_cod_tipo sum(
	     (SELECT count(1)
		FROM mygov_ente_tipo_dovuto metd
		   , mygov_ente e
		WHERE  e.mygov_ente_id = metd.mygov_ente_id 
		   AND e.mygov_ente_id = n_mygov_ente_id
		   AND metd.cod_tipo = n_cod_tipo_dovuto) ); 
	    
	IF (num_cod_tipo = 0) THEN
		result := 'PAA_IMPORT_COD_TIPO_DOVUTO_INESISTENTE';
		result_desc := 'Il tipo dovuto non esiste per questo ente';
		RETURN;
	END IF;	
	
	/* ************  VERIFICA permessi dell''operatore su tipo dovuto del dovuto nuovo ********** */ 
		
	select INTO num_cod_tipo sum(
	     (SELECT count(1)
		FROM mygov_ente_tipo_dovuto metd
		   , mygov_operatore_ente_tipo_dovuto tdo
		   , mygov_operatore o
		   , mygov_utente u
		   , mygov_ente e
		WHERE  e.mygov_ente_id = metd.mygov_ente_id 
		   AND tdo.mygov_ente_tipo_dovuto_id = metd.mygov_ente_tipo_dovuto_id
		   AND tdo.mygov_operatore_id = o.mygov_operatore_id
		   AND o.cod_fed_user_id = u.cod_fed_user_id
		   AND o.cod_ipa_ente = e.cod_ipa_ente 
		   AND e.mygov_ente_id = n_mygov_ente_id
		   AND tdo.flg_attivo = true
		   AND metd.cod_tipo = n_cod_tipo_dovuto
		   AND u.mygov_utente_id = n_mygov_utente_id) 
		+
	    (SELECT count(1)
	       FROM mygov_utente u
	      WHERE split_part(u.cod_fed_user_id, '-', 2) = 'WS_USER' 
		AND u.mygov_utente_id = n_mygov_utente_id)); 
	    
	IF ((num_cod_tipo = 0) OR (num_cod_tipo > 1)) THEN
		result := 'PAA_IMPORT_OPERATORE_NO_PERMESSI';
		result_desc := 'L''operatore non ha i permessi per questo tipo dovuto e ente';
		RETURN;
	END IF;	

	/* ************  VERIFICA obbligatorieta data per tipo dovuto  ********** */ 
		
	SELECT flg_scadenza_obbligatoria, flg_stampa_data_scadenza
	INTO   obbligatorio, stampa_data 
	FROM   mygov_ente_tipo_dovuto
	WHERE  mygov_ente_id = n_mygov_ente_id
	AND    cod_tipo = n_cod_tipo_dovuto; 
	    
	IF ((obbligatorio OR stampa_data) AND n_dt_rp_dati_vers_data_esecuzione_pagamento IS NULL) THEN
		result := 'PAA_IMPORT_DATA_SCADENZA_PAGAMENTO_OBBLIGATORIA';
		result_desc := 'dataScadenzaPagamento deve essere valorizzata per questo tipo dovuto';
		RETURN;
	END IF;	

	IF (obbligatorio AND n_dt_rp_dati_vers_data_esecuzione_pagamento < n_dt_creazione::date) THEN
		result := 'PAA_IMPORT_DATA_SCADENZA_PAGAMENTO_OBBLIGATORIA';
		result_desc := 'dataScadenzaPagamento deve essere maggiore uguale alla data attuale per questo tipo dovuto';
		RETURN;
	END IF;		
               

	/* ************  VERIFICA ESISTENZA cod_iud SU TABELLA mygov_identificativo_univoco  ********** */

	OPEN iud_cursor;

	LOOP
		FETCH iud_cursor INTO iud_record;
		EXIT WHEN NOT FOUND;
		num_iud = num_iud + 1;
		iud_insertable := is_dovuto_annullato(iud_record.identificativo, 'IUD', iud_record.mygov_flusso_id);
		IF iud_insertable = false THEN			
			EXIT;  -- exit loop
		END IF;
	END LOOP;

	CLOSE iud_cursor;

	IF (num_iud > 0 AND iud_insertable = false) THEN
		result := 'PAA_IMPORT_IUD_DUPLICATO';	
		result_desc := 'IUD attivo presente nel database, non si accettano dovuti duplicati';
		RETURN;
	END IF;	
	

	IF n_cod_iuv IS NOT NULL THEN

	        /* ************     Valorizzo dt_creazione_cod_iuv solo se presente iuv in input    ********** */

		n_dt_creazione_cod_iuv := n_dt_creazione;
		
		/* ************  VERIFICA ESISTENZA cod_iuv SU TABELLA mygov_identificativo_univoco  ********** */
	    
		OPEN iuv_cursor;

		LOOP
			FETCH iuv_cursor INTO iuv_record;
			EXIT WHEN NOT FOUND;
			num_iuv = num_iuv + 1;
			iuv_insertable := is_dovuto_annullato(iuv_record.identificativo, 'IUV', iuv_record.mygov_flusso_id);
			IF iuv_insertable = false THEN			
				EXIT;  -- exit loop
			END IF;
		END LOOP;

		CLOSE iuv_cursor;

		IF (num_iuv > 0 AND iuv_insertable = false) THEN
			result := 'PAA_IMPORT_IUV_DUPLICATO';	
			result_desc := 'IUV attivo presente nel database, non si accettano dovuti duplicati';
			RETURN;	
		END IF;
							
		/* ************  Inserisce su mygov_avviso_digitale se ha iuv valido e flg su ente a true ed cod_funzionalita = 'AVVISO_DIGITALE' ********** */
		          							
		IF (SELECT  flg_attivo
		    FROM    mygov_ente_funzionalita 
		    WHERE   cod_ipa_ente = val_cod_ipa_ente
		    AND     cod_funzionalita = 'AVVISO_DIGITALE') AND (length(n_cod_iuv) = 15 OR length(n_cod_iuv) = 17) AND n_dt_rp_dati_vers_data_esecuzione_pagamento IS NOT NULL AND insert_avv_dig THEN
			PERFORM insert_mygov_avviso_digitale(
							    n_mygov_ente_id,
							    n_dt_creazione,
							    n_cod_iuv,
							    n_de_rp_sogg_pag_anagrafica_pagatore,
							    n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco,
							    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
							    n_dt_rp_dati_vers_data_esecuzione_pagamento,
							    n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
							    n_de_rp_dati_vers_dati_sing_vers_causale_versamento
							);
		END IF;	
		
		/* ************  Recupero flg_notifica_io dalla tabella mygov_ente_tipo_dovuto data la tipologia del dovuto da inserire (n_cod_tipo_dovuto character varying) ed l''id ente (n_mygov_ente_id bigint)********** */
		
		SELECT flg_notifica_io 
			INTO notifica_io
			FROM mygov_ente_tipo_dovuto
			WHERE mygov_ente_id = n_mygov_ente_id AND cod_tipo = n_cod_tipo_dovuto;
		
		/* ************  Inserisce su mygov_avviso_digitale se ha iuv valido, flg su ente a true, cod_funzionalita = 'NOTIFICA_AVVISI_IO' e per il dovuto ï¿œ prevista la notifica IO********** */
		
		IF (SELECT  flg_attivo
		    FROM    mygov_ente_funzionalita 
		    WHERE   cod_ipa_ente = val_cod_ipa_ente
		    AND     cod_funzionalita = 'NOTIFICA_AVVISI_IO') AND (length(n_cod_iuv) = 15 OR length(n_cod_iuv) = 17) AND notifica_io THEN
			PERFORM insert_mygov_avviso_digitale_notifica_io(
							    n_mygov_ente_id,
							    n_dt_creazione,
							    n_cod_iuv,
							    n_de_rp_sogg_pag_anagrafica_pagatore,
							    n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco,
							    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
							    n_dt_rp_dati_vers_data_esecuzione_pagamento,
							    n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
							    n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
								n_de_rp_sogg_pag_email_pagatore
							);
		END IF;	
		
		
		
	 END IF;
   
	IF (num_iud > 0 AND iud_insertable = true) THEN
		UPDATE mygov_identificativo_univoco SET mygov_flusso_id = n_mygov_flusso_id WHERE mygov_ente_id = n_mygov_ente_id AND cod_tipo_identificativo = 'IUD' AND identificativo = n_cod_iud;
	ELSEIF (num_iud = 0) THEN
		INSERT INTO mygov_identificativo_univoco(mygov_identificativo_univoco_id
							 ,"version"
							 , mygov_ente_id
							 , mygov_flusso_id
							 , cod_tipo_identificativo
							 , identificativo
							 , dt_inserimento)
						   VALUES (nextval('mygov_identificativo_univoco_id_seq')
							 , 0
							 , n_mygov_ente_id
							 , n_mygov_flusso_id
							 , 'IUD'
							 , n_cod_iud
							 , n_dt_creazione);							 
	END IF; 

	IF (n_cod_iuv IS NOT NULL AND num_iuv > 0 AND iuv_insertable = true) THEN
		UPDATE mygov_identificativo_univoco SET mygov_flusso_id = n_mygov_flusso_id WHERE mygov_ente_id = n_mygov_ente_id AND cod_tipo_identificativo = 'IUV' AND identificativo = n_cod_iuv;	
	ELSIF (n_cod_iuv IS NOT NULL AND num_iuv = 0) THEN
		INSERT INTO mygov_identificativo_univoco(mygov_identificativo_univoco_id
							,"version"
							, mygov_ente_id
							, mygov_flusso_id
							, cod_tipo_identificativo
							, identificativo
							, dt_inserimento)
						   VALUES (nextval('mygov_identificativo_univoco_id_seq')
							, 0
							, n_mygov_ente_id
							, n_mygov_flusso_id
							, 'IUV'
							, n_cod_iuv
							, n_dt_creazione);	
	END IF;	   
	  	    
	  INSERT INTO mygov_dovuto(
	    mygov_dovuto_id, "version", flg_dovuto_attuale, mygov_flusso_id, 
	    num_riga_flusso, mygov_anagrafica_stato_id, mygov_carrello_id, 
	    cod_iud, cod_iuv, dt_creazione, dt_ultima_modifica, cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
	    cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, de_rp_sogg_pag_anagrafica_pagatore, 
	    de_rp_sogg_pag_indirizzo_pagatore, de_rp_sogg_pag_civico_pagatore, 
	    cod_rp_sogg_pag_cap_pagatore, de_rp_sogg_pag_localita_pagatore, 
	    de_rp_sogg_pag_provincia_pagatore, cod_rp_sogg_pag_nazione_pagatore, 
	    de_rp_sogg_pag_email_pagatore, dt_rp_dati_vers_data_esecuzione_pagamento, 
	    cod_rp_dati_vers_tipo_versamento, num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
	    num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, cod_tipo_dovuto, 
	    de_rp_dati_vers_dati_sing_vers_causale_versamento, de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, dt_creazione_cod_iuv,
		bilancio,flg_genera_iuv)

	    VALUES (nextval('mygov_dovuto_mygov_dovuto_id_seq'), 0, true, n_mygov_flusso_id, 
	    n_num_riga_flusso, n_mygov_anagrafica_stato_id, n_mygov_carrello_id, 
	    n_cod_iud, n_cod_iuv, n_dt_creazione, n_dt_ultima_modifica, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
	    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, n_de_rp_sogg_pag_anagrafica_pagatore, 
	    n_de_rp_sogg_pag_indirizzo_pagatore, n_de_rp_sogg_pag_civico_pagatore, 
	    n_cod_rp_sogg_pag_cap_pagatore, n_de_rp_sogg_pag_localita_pagatore, 
	    n_de_rp_sogg_pag_provincia_pagatore, n_cod_rp_sogg_pag_nazione_pagatore, 
	    n_de_rp_sogg_pag_email_pagatore, n_dt_rp_dati_vers_data_esecuzione_pagamento, 
	    n_cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
	    n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, n_cod_tipo_dovuto,  
	    n_de_rp_dati_vers_dati_sing_vers_causale_versamento, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_dt_creazione_cod_iuv,
		n_bilancio,n_flg_genera_iuv); 
    END;
$$;


ALTER FUNCTION public.insert_mygov_dovuto_ret(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, INOUT n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_dt_ultima_modifica timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean, n_flg_genera_iuv boolean, OUT result character varying, OUT result_desc character varying) OWNER TO mypay4;

--
-- TOC entry 305 (class 1255 OID 20865)
-- Name: insert_mygov_dovuto_rifiutato(bigint, numeric, character varying, character varying, bigint, text, timestamp without time zone, timestamp without time zone); Type: FUNCTION; Schema: public; Owner: mypay4
--

CREATE FUNCTION public.insert_mygov_dovuto_rifiutato(new_mygov_flusso_id bigint, new_num_riga_flusso numeric, new_cod_iud character varying, new_cod_iuv character varying, new_mygov_anagrafica_stato_id bigint, new_de_rifiuto text, new_dt_creazione timestamp without time zone, new_dt_ultima_modifica timestamp without time zone) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
    
        INSERT INTO mygov_dovuto_rifiutato(mygov_dovuto_rifiutato_id
                                         , "version"
                                         , mygov_flusso_id
                                         , num_riga_flusso
                                         , cod_iud
                                         , cod_iuv
                                         , mygov_anagrafica_stato_id
                                         , de_rifiuto
                                         , dt_creazione
                                         , dt_ultima_modifica)
            VALUES (nextval('mygov_dovuto_rifiutato_mygov_dovuto_rifiutato_id_seq')
                  , 0
                  , new_mygov_flusso_id
                  , new_num_riga_flusso
                  , new_cod_iud
                  , new_cod_iuv
                  , new_mygov_anagrafica_stato_id
                  , new_de_rifiuto
                  , new_dt_creazione
                  , new_dt_ultima_modifica);
    
END;
$$;


ALTER FUNCTION public.insert_mygov_dovuto_rifiutato(new_mygov_flusso_id bigint, new_num_riga_flusso numeric, new_cod_iud character varying, new_cod_iuv character varying, new_mygov_anagrafica_stato_id bigint, new_de_rifiuto text, new_dt_creazione timestamp without time zone, new_dt_ultima_modifica timestamp without time zone) OWNER TO mypay4;

--
-- TOC entry 306 (class 1255 OID 20866)
-- Name: is_dovuto_annullato(character varying, character varying, bigint); Type: FUNCTION; Schema: public; Owner: mypay4
--

CREATE FUNCTION public.is_dovuto_annullato(identificativo character varying, tipo_identificativo character varying, c_mygov_flusso_id bigint) RETURNS boolean
    LANGUAGE plpgsql
    AS $$

DECLARE
	num_dovuti int;

BEGIN
	IF (tipo_identificativo = 'IUD') 
	THEN
		SELECT 
		INTO num_dovuti count(1)	
		FROM mygov_dovuto_elaborato dov
		    ,mygov_anagrafica_stato stato
		WHERE stato.mygov_anagrafica_stato_id = dov.mygov_anagrafica_stato_id
		  AND stato.cod_stato = 'ANNULLATO'
		  AND dov.mygov_flusso_id = c_mygov_flusso_id
		  AND dov.cod_iud = identificativo;
	ELSE
		SELECT 
		INTO num_dovuti count(1)	
		FROM mygov_dovuto_elaborato dov
		    ,mygov_anagrafica_stato stato
		WHERE stato.mygov_anagrafica_stato_id = dov.mygov_anagrafica_stato_id
		  AND stato.cod_stato = 'ANNULLATO'
		  AND dov.mygov_flusso_id = c_mygov_flusso_id
		  AND dov.cod_iuv = identificativo;
	END IF;

	IF (num_dovuti = 0) 
	THEN RETURN false;
	ELSE RETURN true; 
	END IF;
    
END;

$$;


ALTER FUNCTION public.is_dovuto_annullato(identificativo character varying, tipo_identificativo character varying, c_mygov_flusso_id bigint) OWNER TO mypay4;

--
-- TOC entry 307 (class 1255 OID 20867)
-- Name: modify_mygov_dovuto(bigint, bigint, numeric, bigint, bigint, character varying, character varying, timestamp without time zone, character, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, date, character varying, numeric, numeric, character varying, character varying, character varying, bigint, character varying, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.modify_mygov_dovuto(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean DEFAULT true) RETURNS void
    LANGUAGE plpgsql
    AS $$
    DECLARE
	num_cod_tipo		integer;	
	id_lock			bigint;
	obbligatorio 		boolean;
	notifica_io			boolean;
        val_cod_ipa_ente	character varying;  
	val_application_code	character varying(2);
		  
	found_mygov_dovuto	mygov_dovuto%ROWTYPE;
	found_mygov_avviso_digitale mygov_avviso_digitale%ROWTYPE;

        iuv_cursor		CURSOR FOR 
				SELECT * 
				FROM mygov_identificativo_univoco 
				WHERE identificativo = n_cod_iuv 
				  AND cod_tipo_identificativo = 'IUV'
				  AND mygov_ente_id = n_mygov_ente_id;
				  
        iuv_record		mygov_identificativo_univoco%ROWTYPE; 
        iuv_insertable		boolean := false;
        num_iuv			bigint := 0; 

			iuv_cursor_ex		CURSOR FOR 
				SELECT * 
				FROM mygov_identificativo_univoco 
				WHERE identificativo = n_cod_iuv 
				  AND cod_tipo_identificativo = 'IUV'
				  AND mygov_ente_id = n_mygov_ente_id;
				  
        iuv_record_ex		mygov_identificativo_univoco%ROWTYPE; 
        iuv_insertable_ex	boolean := false;
        num_iuv_ex		bigint := 0; 
        n_dt_creazione_cod_iuv  timestamp without time zone;
        	
    BEGIN  

	/* ************  LOCK SU mygov_ente_id TABELLA mygov_ente  ********** */

	SELECT   mygov_ente_id 
	       , cod_ipa_ente
	       , application_code
	FROM     mygov_ente 
	WHERE    mygov_ente_id = n_mygov_ente_id 
	INTO     id_lock
	       , val_cod_ipa_ente 
	       , val_application_code
	FOR UPDATE;
	
	/* ************  Recupero flg_notifica_io dalla tabella mygov_ente_tipo_dovuto data la tipologia del dovuto da inserire (n_cod_tipo_dovuto character varying) ed l''id ente (n_mygov_ente_id bigint)********** */
	
	SELECT flg_notifica_io 
		INTO notifica_io
		FROM mygov_ente_tipo_dovuto
		WHERE mygov_ente_id = n_mygov_ente_id AND cod_tipo = n_cod_tipo_dovuto;

	/* ************  VERIFICA permessi dell''operatore su tipo dovuto del nuovo dovuto ********** */  
	    
	SELECT INTO num_cod_tipo sum(
	     (SELECT count(1)
		FROM mygov_ente_tipo_dovuto metd
		   , mygov_operatore_ente_tipo_dovuto tdo
		   , mygov_operatore o
		   , mygov_utente u
		   , mygov_ente e
		WHERE  e.mygov_ente_id = metd.mygov_ente_id 
		   AND tdo.mygov_ente_tipo_dovuto_id = metd.mygov_ente_tipo_dovuto_id
		   AND tdo.mygov_operatore_id = o.mygov_operatore_id
		   AND o.cod_fed_user_id = u.cod_fed_user_id
		   AND o.cod_ipa_ente = e.cod_ipa_ente 
		   AND e.mygov_ente_id = n_mygov_ente_id
		   AND tdo.flg_attivo = true
		   AND metd.cod_tipo = n_cod_tipo_dovuto
		   AND u.mygov_utente_id = n_mygov_utente_id) 
		+
	    (SELECT count(1)
	       FROM mygov_utente u
	      WHERE split_part(u.cod_fed_user_id, '-', 2) = 'WS_USER' 
		AND u.mygov_utente_id = n_mygov_utente_id));
	    
	IF ((num_cod_tipo = 0) OR (num_cod_tipo > 1)) THEN
			
		PERFORM insert_mygov_dovuto_rifiutato(n_mygov_flusso_id
						    , n_num_riga_flusso 
						    , n_cod_iud
						    , n_cod_iuv
						    , (select mygov_anagrafica_stato_id from mygov_anagrafica_stato where cod_stato = 'ERROR_DOVUTO')
						    , 'L'' operatore non ha i permessi sul tipo dovuto per modificare il nuovo dovuto; per questo ente'
						    , n_dt_creazione
						    , n_dt_creazione);

			
		RETURN;
	END IF;

	/* ************  VERIFICA obbligatorieta data per tipo dovuto  ********** */ 
		
	SELECT flg_scadenza_obbligatoria OR flg_stampa_data_scadenza
	INTO   obbligatorio 
	FROM   mygov_ente_tipo_dovuto
	WHERE  mygov_ente_id = n_mygov_ente_id
	AND    cod_tipo = n_cod_tipo_dovuto; 
	    
	IF (obbligatorio AND n_dt_rp_dati_vers_data_esecuzione_pagamento IS NULL) THEN
		PERFORM insert_mygov_dovuto_rifiutato(n_mygov_flusso_id
						    ,n_num_riga_flusso 
						    ,n_cod_iud
						    ,n_cod_iuv
						    ,(select mygov_anagrafica_stato_id from mygov_anagrafica_stato where cod_stato = 'ERROR_DOVUTO')
						    ,'dataScadenzaPagamento deve essere valorizzata per questo tipo dovuto'
						    ,n_dt_creazione
						    ,n_dt_creazione);			
		RETURN;
	END IF;

	/* ************  VERIFICA ESISTENZA n_cod_iud SU TABELLA mygov_dovuto  ********** */

	SELECT   dovuto.*
	INTO     found_mygov_dovuto
	FROM     mygov_dovuto dovuto
	        ,mygov_flusso flusso
	        ,mygov_anagrafica_stato stato	    
	WHERE    flusso.mygov_flusso_id = dovuto.mygov_flusso_id
	AND      stato.mygov_anagrafica_stato_id = dovuto.mygov_anagrafica_stato_id
	AND      stato.cod_stato = 'INSERIMENTO_DOVUTO'
	AND      stato.de_tipo_stato = 'dovuto'
	AND      dovuto.cod_iud = n_cod_iud
	AND      dovuto.flg_dovuto_attuale = true
	AND      flusso.flg_attivo = true
	AND      flusso.mygov_ente_id = n_mygov_ente_id; 

	IF found_mygov_dovuto.mygov_dovuto_id IS NULL THEN 
                PERFORM insert_mygov_dovuto_rifiutato(n_mygov_flusso_id
						    ,n_num_riga_flusso 
						    ,n_cod_iud
						    ,n_cod_iuv
						    ,(select mygov_anagrafica_stato_id from mygov_anagrafica_stato where cod_stato = 'ERROR_DOVUTO')
						    ,'Dovuto non presente o in pagamento nel database, impossibile modificarlo'
		                                    ,n_dt_creazione
		                                    ,n_dt_creazione);
	        RETURN; 
	END IF; 

	IF found_mygov_dovuto.cod_tipo_dovuto <> n_cod_tipo_dovuto THEN 
                PERFORM insert_mygov_dovuto_rifiutato(n_mygov_flusso_id
						    ,n_num_riga_flusso 
						    ,n_cod_iud
						    ,n_cod_iuv
						    ,(select mygov_anagrafica_stato_id from mygov_anagrafica_stato where cod_stato = 'ERROR_DOVUTO')
						    ,'Non è possibile modificare il tipo dovuto di un dovuto'
		                                    ,n_dt_creazione
		                                    ,n_dt_creazione);
	        RETURN; 
	END IF; 	

        /* ************  VERIFICA permessi dell''operatore su tipo dovuto del dovuto esistente ********** */
	
	SELECT INTO num_cod_tipo sum(
	     (SELECT count(1) 	
		FROM mygov_ente_tipo_dovuto metd
		   , mygov_flusso mf
		   , mygov_operatore_ente_tipo_dovuto tdo
		   , mygov_operatore o
		   , mygov_utente u
		WHERE metd.cod_tipo = found_mygov_dovuto.cod_tipo_dovuto 
			AND metd.mygov_ente_id = mf.mygov_ente_id 
			AND mf.mygov_flusso_id = found_mygov_dovuto.mygov_flusso_id
			AND tdo.mygov_ente_tipo_dovuto_id = metd.mygov_ente_tipo_dovuto_id
			AND tdo.flg_attivo = true
			AND tdo.mygov_operatore_id = o.mygov_operatore_id
			AND o.cod_fed_user_id = u.cod_fed_user_id
			AND u.mygov_utente_id = n_mygov_utente_id) 
		+
	    (SELECT count(1)
	       FROM mygov_utente u
	      WHERE split_part(u.cod_fed_user_id, '-', 2) = 'WS_USER' 
		AND u.mygov_utente_id = n_mygov_utente_id));
	    
	IF ((num_cod_tipo = 0) OR (num_cod_tipo > 1)) THEN
		PERFORM insert_mygov_dovuto_rifiutato(n_mygov_flusso_id
						    ,n_num_riga_flusso 
						    ,n_cod_iud
						    ,n_cod_iuv
						    ,(select mygov_anagrafica_stato_id from mygov_anagrafica_stato where cod_stato = 'ERROR_DOVUTO')
						    ,'L'' operatore non ha i permessi sul tipo dovuto per modificare il dovuto esistente; per questo ente'
						    ,n_dt_creazione
						    ,n_dt_creazione);

			
		RETURN;
	END IF;	

	/* ************     Valorizzo dt_creazione_cod_iuv solo se presente iuv in input    ********** */

	IF n_cod_iuv IS NOT NULL THEN
		n_dt_creazione_cod_iuv := n_dt_creazione;
	END IF;

	IF found_mygov_dovuto.cod_iuv IS NULL THEN 

		PERFORM sposta_dovuto_in_dovuto_elaborato(found_mygov_dovuto.mygov_dovuto_id, found_mygov_dovuto.mygov_flusso_id, found_mygov_dovuto.num_riga_flusso, 
			    /*found_mygov_dovuto.mygov_anagrafica_stato_id*/ 
			    (select stato.mygov_anagrafica_stato_id
			    from mygov_anagrafica_stato stato
			    where stato.cod_stato = 'ANNULLATO'
				AND stato.de_tipo_stato = 'dovuto')
			    , found_mygov_dovuto.mygov_carrello_id, found_mygov_dovuto.cod_iud, found_mygov_dovuto.cod_iuv, n_dt_creazione, 
			    /*n_de_rp_versione_oggetto*/ '-', found_mygov_dovuto.cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, found_mygov_dovuto.cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, 
			    found_mygov_dovuto.de_rp_sogg_pag_anagrafica_pagatore, found_mygov_dovuto.de_rp_sogg_pag_indirizzo_pagatore, found_mygov_dovuto.de_rp_sogg_pag_civico_pagatore, 
			    found_mygov_dovuto.cod_rp_sogg_pag_cap_pagatore, found_mygov_dovuto.de_rp_sogg_pag_localita_pagatore, found_mygov_dovuto.de_rp_sogg_pag_provincia_pagatore, 
			    found_mygov_dovuto.cod_rp_sogg_pag_nazione_pagatore, found_mygov_dovuto.de_rp_sogg_pag_email_pagatore, found_mygov_dovuto.dt_rp_dati_vers_data_esecuzione_pagamento, 
			    found_mygov_dovuto.cod_rp_dati_vers_tipo_versamento, found_mygov_dovuto.num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
			    found_mygov_dovuto.num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, found_mygov_dovuto.cod_tipo_dovuto, found_mygov_dovuto.de_rp_dati_vers_dati_sing_vers_causale_versamento, 
			    found_mygov_dovuto.de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, found_mygov_dovuto.bilancio);
	
		IF n_cod_iuv IS NOT NULL THEN

			OPEN iuv_cursor;

			LOOP
				FETCH iuv_cursor INTO iuv_record;
				EXIT WHEN NOT FOUND;
				num_iuv = num_iuv + 1;
				iuv_insertable := is_dovuto_annullato(iuv_record.identificativo, 'IUV', iuv_record.mygov_flusso_id);
				IF iuv_insertable = false THEN			
					EXIT;  -- exit loop
				END IF;
			END LOOP;

			CLOSE iuv_cursor;

			IF (num_iuv > 0 AND iuv_insertable = false) THEN
				PERFORM insert_mygov_dovuto_rifiutato(n_mygov_flusso_id
								    ,n_num_riga_flusso 
								    ,n_cod_iud
								    ,n_cod_iuv
								    ,(select mygov_anagrafica_stato_id from mygov_anagrafica_stato where cod_stato = 'ERROR_DOVUTO')
								    ,'IUV duplicato nel database, non si accettano dovuti duplicati'
								    ,n_dt_creazione
								    ,n_dt_creazione);
				RETURN;	
			END IF;

		  
			INSERT INTO mygov_identificativo_univoco( mygov_identificativo_univoco_id
						,"version"
						, mygov_ente_id
						, mygov_flusso_id
						, cod_tipo_identificativo
						, identificativo
						, dt_inserimento)
					  VALUES (nextval('mygov_identificativo_univoco_id_seq')
						, 0
						, n_mygov_ente_id
						, n_mygov_flusso_id
						, 'IUV'
						, n_cod_iuv
						, n_dt_creazione);

		/* ************  Inserisco su mygov_avviso_digitale se ho iuv valido e flg su ente a true  ********** */
											
		IF (	SELECT  flg_attivo
			FROM    mygov_ente_funzionalita 
			WHERE   cod_ipa_ente = val_cod_ipa_ente
			AND     cod_funzionalita = 'AVVISO_DIGITALE')  AND (length(n_cod_iuv) = 15 OR length(n_cod_iuv) = 17) AND insert_avv_dig THEN
				PERFORM insert_mygov_avviso_digitale(
								    n_mygov_ente_id,
								    n_dt_creazione,
								    n_cod_iuv,
								    n_de_rp_sogg_pag_anagrafica_pagatore,
								    n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco,
								    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
								    n_dt_rp_dati_vers_data_esecuzione_pagamento,
								    n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
								    n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
									n_de_rp_sogg_pag_email_pagatore
								);
			END IF;
			

			
		/* ************  Inserisce su mygov_avviso_digitale se ha iuv valido, flg su ente a true, cod_funzionalita = 'NOTIFICA_AVVISI_IO' e per il dovuto e'' prevista la notifica IO********** */

		IF (SELECT  flg_attivo
		    FROM    mygov_ente_funzionalita 
		    WHERE   cod_ipa_ente = val_cod_ipa_ente
		    AND     cod_funzionalita = 'NOTIFICA_AVVISI_IO') AND (length(n_cod_iuv) = 15 OR length(n_cod_iuv) = 17) AND notifica_io THEN
			PERFORM insert_mygov_avviso_digitale_notifica_io(
							    n_mygov_ente_id,
							    n_dt_creazione,
							    n_cod_iuv,
							    n_de_rp_sogg_pag_anagrafica_pagatore,
							    n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco,
							    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
							    n_dt_rp_dati_vers_data_esecuzione_pagamento,
							    n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
							    n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
								n_de_rp_sogg_pag_email_pagatore
							);
			END IF;

		END IF;

		INSERT INTO mygov_dovuto(
		    mygov_dovuto_id, "version", flg_dovuto_attuale, mygov_flusso_id, 
		    num_riga_flusso, mygov_anagrafica_stato_id, mygov_carrello_id, 
		    cod_iud, cod_iuv, dt_creazione, dt_ultima_modifica, cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
		    cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, de_rp_sogg_pag_anagrafica_pagatore, 
		    de_rp_sogg_pag_indirizzo_pagatore, de_rp_sogg_pag_civico_pagatore, 
		    cod_rp_sogg_pag_cap_pagatore, de_rp_sogg_pag_localita_pagatore, 
		    de_rp_sogg_pag_provincia_pagatore, cod_rp_sogg_pag_nazione_pagatore, 
		    de_rp_sogg_pag_email_pagatore, dt_rp_dati_vers_data_esecuzione_pagamento, 
		    cod_rp_dati_vers_tipo_versamento, num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
		    num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, cod_tipo_dovuto,
		    de_rp_dati_vers_dati_sing_vers_causale_versamento, de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, dt_creazione_cod_iuv,
			bilancio)

		    VALUES (nextval('mygov_dovuto_mygov_dovuto_id_seq'), 0, true, n_mygov_flusso_id, 
		    n_num_riga_flusso, n_mygov_anagrafica_stato_id, n_mygov_carrello_id, 
		    n_cod_iud, n_cod_iuv, n_dt_creazione, n_dt_creazione, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
		    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, n_de_rp_sogg_pag_anagrafica_pagatore, 
		    n_de_rp_sogg_pag_indirizzo_pagatore, n_de_rp_sogg_pag_civico_pagatore, 
		    n_cod_rp_sogg_pag_cap_pagatore, n_de_rp_sogg_pag_localita_pagatore, 
		    n_de_rp_sogg_pag_provincia_pagatore, n_cod_rp_sogg_pag_nazione_pagatore, 
		    n_de_rp_sogg_pag_email_pagatore, n_dt_rp_dati_vers_data_esecuzione_pagamento, 
		    n_cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
		    n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, n_cod_tipo_dovuto,
		    n_de_rp_dati_vers_dati_sing_vers_causale_versamento, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_dt_creazione_cod_iuv,
			n_bilancio); 
		    

	ELSE /*SE HO IUV OLD */
		IF ((n_cod_iuv IS NOT NULL) AND (n_cod_iuv <> found_mygov_dovuto.cod_iuv)) THEN /* SE HO NEW E <> OLD */
		
			PERFORM insert_mygov_dovuto_rifiutato(n_mygov_flusso_id
							    ,n_num_riga_flusso 
							    ,n_cod_iud
							    ,n_cod_iuv
							    ,(select mygov_anagrafica_stato_id from mygov_anagrafica_stato where cod_stato = 'ERROR_DOVUTO')
							    ,'Non è possibile modificare il codice iuv di un dovuto'
							    ,n_dt_creazione
							    ,n_dt_creazione);
			RETURN; 
						
		ELSIF ((n_cod_iuv IS NOT NULL) AND (n_cod_iuv = found_mygov_dovuto.cod_iuv)) THEN /* SE HO NEW E = OLD */
		
		/* Gestione della modifica di un dovuto esistente, bisogna gestire il tipo di operazioneda da comunicare,
				non possiamo inviare tipo_operazione = U (modifica di un avviso digitale esistente), se non è stato già inviato l'avviso digitale*/
		
			IF (	SELECT  flg_attivo
				FROM    mygov_ente_funzionalita 
				WHERE   cod_ipa_ente = val_cod_ipa_ente
				AND     cod_funzionalita = 'AVVISO_DIGITALE') AND length(n_cod_iuv) = 15 AND insert_avv_dig THEN
				
				/*recupero avviso digitale */
				SELECT   *
				INTO     found_mygov_avviso_digitale
				FROM     mygov_avviso_digitale
				WHERE	 cod_ad_cod_avviso = '0' || val_application_code || found_mygov_dovuto.cod_iuv;
				
				/* Condizione che verifica se l'operazione di creazione di l'avviso digitale è stata sottomessa, 
					se non è stata sottomessa, apporto modifiche ma lascio come tipo operazione = C*/
				IF(
					found_mygov_avviso_digitale.tipo_operazione = 'C' AND
					(found_mygov_avviso_digitale.mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
								    FROM   mygov_anagrafica_stato
								    WHERE  cod_stato = 'NUOVO'
								    AND    de_tipo_stato = 'ad')) 
				) THEN
				
					UPDATE mygov_avviso_digitale 
					SET    de_ad_sog_pag_anag_pagatore = n_de_rp_sogg_pag_anagrafica_pagatore
						  ,cod_ad_sog_pag_id_univ_pag_tipo_id_univ = n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco
						  ,cod_ad_sog_pag_id_univ_pag_cod_id_univ = n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco
						  ,dt_ad_data_scadenza_pagamento = n_dt_rp_dati_vers_data_esecuzione_pagamento
						  ,dt_ad_data_scadenza_avviso = n_dt_rp_dati_vers_data_esecuzione_pagamento
						  ,num_ad_importo_avviso = n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento
						  ,de_ad_desc_pagamento = n_de_rp_dati_vers_dati_sing_vers_causale_versamento
						  ,dt_ultima_modifica = n_dt_creazione
						  ,tipo_operazione = 'C'
					WHERE  cod_ad_cod_avviso = '0' || val_application_code || found_mygov_dovuto.cod_iuv
					AND    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
										FROM   mygov_anagrafica_stato
										WHERE  cod_stato = 'NUOVO'
										AND    de_tipo_stato = 'ad');
								
			/* Se accerto che è stata effettuata almeno una sottomissione di creazione, allora posso settare il
				tipo_operazione = U, modifica di avviso digitale esistente */
				ELSE
					
					UPDATE mygov_avviso_digitale 
					SET    de_ad_sog_pag_anag_pagatore = n_de_rp_sogg_pag_anagrafica_pagatore
					  ,cod_ad_sog_pag_id_univ_pag_tipo_id_univ = n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco
					  ,cod_ad_sog_pag_id_univ_pag_cod_id_univ = n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco
					  ,dt_ad_data_scadenza_pagamento = n_dt_rp_dati_vers_data_esecuzione_pagamento
					  ,dt_ad_data_scadenza_avviso = n_dt_rp_dati_vers_data_esecuzione_pagamento
					  ,num_ad_importo_avviso = n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento
					  ,de_ad_desc_pagamento = n_de_rp_dati_vers_dati_sing_vers_causale_versamento
					  ,dt_ultima_modifica = n_dt_creazione
					  ,tipo_operazione = 'U'
					  , mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
									FROM   mygov_anagrafica_stato
									WHERE  cod_stato = 'NUOVO'
									AND    de_tipo_stato = 'ad')
					WHERE  cod_ad_cod_avviso = '0' || val_application_code || found_mygov_dovuto.cod_iuv
					AND    mygov_anagrafica_stato_id != (SELECT mygov_anagrafica_stato_id
										FROM   mygov_anagrafica_stato
										WHERE  cod_stato = 'ANNULLATO'
										AND    de_tipo_stato = 'ad');

				END IF;
				
										
			END IF;
			
			

			IF (	SELECT  flg_attivo
				FROM    mygov_ente_funzionalita 
				WHERE   cod_ipa_ente = val_cod_ipa_ente
				AND     cod_funzionalita = 'AVVISO_DIGITALE') AND length(n_cod_iuv) = 17 AND insert_avv_dig THEN
				
				/*recupero avviso digitale */
				SELECT   *
				INTO     found_mygov_avviso_digitale
				FROM     mygov_avviso_digitale
				WHERE	 cod_ad_cod_avviso = '3' || found_mygov_dovuto.cod_iuv;
				
				IF(
					found_mygov_avviso_digitale.tipo_operazione = 'C' AND
					(found_mygov_avviso_digitale.mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
								    FROM   mygov_anagrafica_stato
								    WHERE  cod_stato = 'NUOVO'
								    AND    de_tipo_stato = 'ad')) 
				) THEN

					UPDATE mygov_avviso_digitale 
					SET    de_ad_sog_pag_anag_pagatore = n_de_rp_sogg_pag_anagrafica_pagatore
						  ,cod_ad_sog_pag_id_univ_pag_tipo_id_univ = n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco
						  ,cod_ad_sog_pag_id_univ_pag_cod_id_univ = n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco
						  ,dt_ad_data_scadenza_pagamento = n_dt_rp_dati_vers_data_esecuzione_pagamento
						  ,dt_ad_data_scadenza_avviso = n_dt_rp_dati_vers_data_esecuzione_pagamento
						  ,num_ad_importo_avviso = n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento
						  ,de_ad_desc_pagamento = n_de_rp_dati_vers_dati_sing_vers_causale_versamento
						  ,dt_ultima_modifica = n_dt_creazione
						  ,tipo_operazione = 'C'
					WHERE  cod_ad_cod_avviso = '3' || found_mygov_dovuto.cod_iuv
					AND    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
										FROM   mygov_anagrafica_stato
										WHERE  cod_stato = 'NUOVO'
										AND    de_tipo_stato = 'ad');
										
				ELSE
					
						UPDATE mygov_avviso_digitale 
						SET    de_ad_sog_pag_anag_pagatore = n_de_rp_sogg_pag_anagrafica_pagatore
						  ,cod_ad_sog_pag_id_univ_pag_tipo_id_univ = n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco
						  ,cod_ad_sog_pag_id_univ_pag_cod_id_univ = n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco
						  ,dt_ad_data_scadenza_pagamento = n_dt_rp_dati_vers_data_esecuzione_pagamento
						  ,dt_ad_data_scadenza_avviso = n_dt_rp_dati_vers_data_esecuzione_pagamento
						  ,num_ad_importo_avviso = n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento
						  ,de_ad_desc_pagamento = n_de_rp_dati_vers_dati_sing_vers_causale_versamento
						  ,dt_ultima_modifica = n_dt_creazione
						  ,tipo_operazione = 'U'
						  , mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
										FROM   mygov_anagrafica_stato
										WHERE  cod_stato = 'NUOVO'
										AND    de_tipo_stato = 'ad')
					WHERE  cod_ad_cod_avviso = '3' || found_mygov_dovuto.cod_iuv
					AND    mygov_anagrafica_stato_id != (SELECT mygov_anagrafica_stato_id
										FROM   mygov_anagrafica_stato
										WHERE  cod_stato = 'ANNULLATO'
										AND    de_tipo_stato = 'ad');

				END IF;
										
			END IF;
			
			
	
			IF (	SELECT  flg_attivo
				FROM    mygov_ente_funzionalita 
				WHERE   cod_ipa_ente = val_cod_ipa_ente
				AND     cod_funzionalita = 'NOTIFICA_AVVISI_IO') AND length(n_cod_iuv) = 15 AND notifica_io THEN

				UPDATE mygov_avviso_digitale 
				SET    de_ad_sog_pag_anag_pagatore = n_de_rp_sogg_pag_anagrafica_pagatore
				      ,cod_ad_sog_pag_id_univ_pag_tipo_id_univ = n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco
				      ,cod_ad_sog_pag_id_univ_pag_cod_id_univ = n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco
				      ,dt_ad_data_scadenza_pagamento = n_dt_rp_dati_vers_data_esecuzione_pagamento
				      ,dt_ad_data_scadenza_avviso = n_dt_rp_dati_vers_data_esecuzione_pagamento
				      ,num_ad_importo_avviso = n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento
				      ,de_ad_desc_pagamento = n_de_rp_dati_vers_dati_sing_vers_causale_versamento
				      ,dt_ultima_modifica = n_dt_creazione
					  ,de_ad_email_soggetto = n_de_rp_sogg_pag_email_pagatore
					  ,tipo_operazione = 'U'
				WHERE  cod_ad_cod_avviso = '0_' || val_application_code || '_' || found_mygov_dovuto.cod_iuv 
				AND    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
								    FROM   mygov_anagrafica_stato
								    WHERE  cod_stato = 'NUOVO'
								    AND    de_tipo_stato = 'naIO');								    
			END IF;

			IF (	SELECT  flg_attivo
				FROM    mygov_ente_funzionalita 
				WHERE   cod_ipa_ente = val_cod_ipa_ente
				AND     cod_funzionalita = 'NOTIFICA_AVVISI_IO') AND length(n_cod_iuv) = 17 AND notifica_io THEN

				UPDATE mygov_avviso_digitale 
				SET    de_ad_sog_pag_anag_pagatore = n_de_rp_sogg_pag_anagrafica_pagatore
				      ,cod_ad_sog_pag_id_univ_pag_tipo_id_univ = n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco
				      ,cod_ad_sog_pag_id_univ_pag_cod_id_univ = n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco
				      ,dt_ad_data_scadenza_pagamento = n_dt_rp_dati_vers_data_esecuzione_pagamento
				      ,dt_ad_data_scadenza_avviso = n_dt_rp_dati_vers_data_esecuzione_pagamento
				      ,num_ad_importo_avviso = n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento
				      ,de_ad_desc_pagamento = n_de_rp_dati_vers_dati_sing_vers_causale_versamento
				      ,dt_ultima_modifica = n_dt_creazione
					  ,de_ad_email_soggetto = n_de_rp_sogg_pag_email_pagatore
					  ,tipo_operazione = 'U'
				WHERE  cod_ad_cod_avviso = '3_' || found_mygov_dovuto.cod_iuv
				AND    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
								    FROM   mygov_anagrafica_stato
								    WHERE  cod_stato = 'NUOVO'
								    AND    de_tipo_stato = 'naIO');								    
			END IF;
			
	
			
			
		ELSIF (n_cod_iuv IS NULL) THEN /* SE HO OLD E NO NEW */
			PERFORM insert_mygov_dovuto_rifiutato(n_mygov_flusso_id
							    ,n_num_riga_flusso 
							    ,n_cod_iud
							    ,n_cod_iuv
							    ,(select mygov_anagrafica_stato_id from mygov_anagrafica_stato where cod_stato = 'ERROR_DOVUTO')
							    ,'Non è possibile azzerare il codice iuv di un dovuto'
							    ,n_dt_creazione
							    ,n_dt_creazione);
			RETURN; 
		END IF;
		
		
	        PERFORM sposta_dovuto_in_dovuto_elaborato(found_mygov_dovuto.mygov_dovuto_id, found_mygov_dovuto.mygov_flusso_id, found_mygov_dovuto.num_riga_flusso,
		    (select stato.mygov_anagrafica_stato_id
		       from mygov_anagrafica_stato stato
		      where stato.cod_stato = 'ANNULLATO'
		        AND stato.de_tipo_stato = 'dovuto')
		    , found_mygov_dovuto.mygov_carrello_id, found_mygov_dovuto.cod_iud, found_mygov_dovuto.cod_iuv, n_dt_creazione, 
		     '-', found_mygov_dovuto.cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, found_mygov_dovuto.cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, 
		    found_mygov_dovuto.de_rp_sogg_pag_anagrafica_pagatore, found_mygov_dovuto.de_rp_sogg_pag_indirizzo_pagatore, found_mygov_dovuto.de_rp_sogg_pag_civico_pagatore, 
		    found_mygov_dovuto.cod_rp_sogg_pag_cap_pagatore, found_mygov_dovuto.de_rp_sogg_pag_localita_pagatore, found_mygov_dovuto.de_rp_sogg_pag_provincia_pagatore, 
		    found_mygov_dovuto.cod_rp_sogg_pag_nazione_pagatore, found_mygov_dovuto.de_rp_sogg_pag_email_pagatore, found_mygov_dovuto.dt_rp_dati_vers_data_esecuzione_pagamento, 
		    found_mygov_dovuto.cod_rp_dati_vers_tipo_versamento, found_mygov_dovuto.num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
		    found_mygov_dovuto.num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, found_mygov_dovuto.cod_tipo_dovuto, found_mygov_dovuto.de_rp_dati_vers_dati_sing_vers_causale_versamento, 
		    found_mygov_dovuto.de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, found_mygov_dovuto.bilancio);
		
		INSERT INTO mygov_dovuto(
		    mygov_dovuto_id, "version", flg_dovuto_attuale, mygov_flusso_id, 
		    num_riga_flusso, mygov_anagrafica_stato_id, mygov_carrello_id, 
		    cod_iud, cod_iuv, dt_creazione, dt_ultima_modifica, cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
		    cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, de_rp_sogg_pag_anagrafica_pagatore, 
		    de_rp_sogg_pag_indirizzo_pagatore, de_rp_sogg_pag_civico_pagatore, 
		    cod_rp_sogg_pag_cap_pagatore, de_rp_sogg_pag_localita_pagatore, 
		    de_rp_sogg_pag_provincia_pagatore, cod_rp_sogg_pag_nazione_pagatore, 
		    de_rp_sogg_pag_email_pagatore, dt_rp_dati_vers_data_esecuzione_pagamento, 
		    cod_rp_dati_vers_tipo_versamento, num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
		    num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, cod_tipo_dovuto,
		    de_rp_dati_vers_dati_sing_vers_causale_versamento, de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, dt_creazione_cod_iuv, bilancio)

		    VALUES (nextval('mygov_dovuto_mygov_dovuto_id_seq'), 0, true, n_mygov_flusso_id, 
		    n_num_riga_flusso, n_mygov_anagrafica_stato_id, n_mygov_carrello_id, 
		    n_cod_iud, n_cod_iuv, n_dt_creazione, n_dt_creazione, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
		    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, n_de_rp_sogg_pag_anagrafica_pagatore, 
		    n_de_rp_sogg_pag_indirizzo_pagatore, n_de_rp_sogg_pag_civico_pagatore, 
		    n_cod_rp_sogg_pag_cap_pagatore, n_de_rp_sogg_pag_localita_pagatore, 
		    n_de_rp_sogg_pag_provincia_pagatore, n_cod_rp_sogg_pag_nazione_pagatore, 
		    n_de_rp_sogg_pag_email_pagatore, n_dt_rp_dati_vers_data_esecuzione_pagamento, 
		    n_cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
		    n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, n_cod_tipo_dovuto,
		    n_de_rp_dati_vers_dati_sing_vers_causale_versamento, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_dt_creazione_cod_iuv, n_bilancio); 
	END IF;

	
    END;
$$;


ALTER FUNCTION public.modify_mygov_dovuto(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean) OWNER TO postgres;

--
-- TOC entry 308 (class 1255 OID 20870)
-- Name: modify_mygov_dovuto(bigint, bigint, numeric, bigint, bigint, character varying, character varying, timestamp without time zone, character, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, date, character varying, numeric, numeric, character varying, character varying, character varying, bigint, character varying, boolean, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.modify_mygov_dovuto(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, INOUT n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean DEFAULT true, n_flg_genera_iuv boolean DEFAULT NULL::boolean, OUT result character varying, OUT result_desc character varying) RETURNS record
    LANGUAGE plpgsql
    AS $_$
    DECLARE
	num_cod_tipo		integer;	
	id_lock			bigint;
	obbligatorio 		boolean;
	stampa_data 		boolean;
	notifica_io			boolean;
        val_cod_ipa_ente	character varying;  
	val_application_code	character varying(2);
		  
	found_mygov_dovuto	mygov_dovuto%ROWTYPE;

        iuv_cursor		CURSOR FOR 
				SELECT * 
				FROM mygov_identificativo_univoco 
				WHERE identificativo = n_cod_iuv 
				  AND cod_tipo_identificativo = 'IUV'
				  AND mygov_ente_id = n_mygov_ente_id;
				  
        iuv_record		mygov_identificativo_univoco%ROWTYPE; 
        iuv_insertable		boolean := false;
        num_iuv			bigint := 0; 
        n_dt_creazione_cod_iuv  timestamp without time zone;
        	
    BEGIN  

	/* ************  LOCK SU mygov_ente_id TABELLA mygov_ente  ********** */

	SELECT   mygov_ente_id 
	       , cod_ipa_ente
	       , application_code
	FROM     mygov_ente 
	WHERE    mygov_ente_id = n_mygov_ente_id 
	INTO     id_lock
	       , val_cod_ipa_ente 
	       , val_application_code
	FOR UPDATE;
	
	/* ************  Recupero flg_notifica_io dalla tabella mygov_ente_tipo_dovuto data la tipologia del dovuto da inserire (n_cod_tipo_dovuto character varying) ed l''id ente (n_mygov_ente_id bigint)********** */
	
	SELECT flg_notifica_io 
		INTO notifica_io
		FROM mygov_ente_tipo_dovuto
		WHERE mygov_ente_id = n_mygov_ente_id AND cod_tipo = n_cod_tipo_dovuto;



	/* ************  VERIFICA esistenza tipo dovuto per ente ********** */ 

	select INTO num_cod_tipo sum(
	     (SELECT count(1)
		FROM mygov_ente_tipo_dovuto metd
		   , mygov_ente e
		WHERE  e.mygov_ente_id = metd.mygov_ente_id 
		   AND e.mygov_ente_id = n_mygov_ente_id
		   AND metd.cod_tipo = n_cod_tipo_dovuto) ); 
	    
	IF (num_cod_tipo = 0) THEN
		result := 'PAA_IMPORT_COD_TIPO_DOVUTO_INESISTENTE';
		result_desc := 'Il tipo dovuto non esiste per questo ente';
		RETURN;
	END IF;			

	/* ************  VERIFICA permessi dell''operatore su tipo dovuto del nuovo dovuto ********** */  
	    
	SELECT INTO num_cod_tipo sum(
	     (SELECT count(1)
		FROM mygov_ente_tipo_dovuto metd
		   , mygov_operatore_ente_tipo_dovuto tdo
		   , mygov_operatore o
		   , mygov_utente u
		   , mygov_ente e
		WHERE  e.mygov_ente_id = metd.mygov_ente_id 
		   AND tdo.mygov_ente_tipo_dovuto_id = metd.mygov_ente_tipo_dovuto_id
		   AND tdo.mygov_operatore_id = o.mygov_operatore_id
		   AND o.cod_fed_user_id = u.cod_fed_user_id
		   AND o.cod_ipa_ente = e.cod_ipa_ente 
		   AND e.mygov_ente_id = n_mygov_ente_id
		   AND tdo.flg_attivo = true
		   AND metd.cod_tipo = n_cod_tipo_dovuto
		   AND u.mygov_utente_id = n_mygov_utente_id) 
		+
	    (SELECT count(1)
	       FROM mygov_utente u
	      WHERE split_part(u.cod_fed_user_id, '-', 2) = 'WS_USER' 
		AND u.mygov_utente_id = n_mygov_utente_id));
	    
	IF ((num_cod_tipo = 0) OR (num_cod_tipo > 1)) THEN			
		result := 'PAA_IMPORT_OPERATORE_NO_PERMESSI';
		result_desc := 'L''operatore non ha i permessi per questo tipo dovuto e ente';
		RETURN;
	END IF;

	/* ************  VERIFICA obbligatorieta data per tipo dovuto  ********** */ 
		
	SELECT flg_scadenza_obbligatoria, flg_stampa_data_scadenza
	INTO   obbligatorio, stampa_data 
	FROM   mygov_ente_tipo_dovuto
	WHERE  mygov_ente_id = n_mygov_ente_id
	AND    cod_tipo = n_cod_tipo_dovuto; 
	    
	IF ((obbligatorio OR stampa_data) AND n_dt_rp_dati_vers_data_esecuzione_pagamento IS NULL) THEN
		result := 'PAA_IMPORT_DATA_SCADENZA_PAGAMENTO_OBBLIGATORIA';
		result_desc := 'dataScadenzaPagamento deve essere valorizzata per questo tipo dovuto';
		RETURN;
	END IF;	

	IF (obbligatorio AND n_dt_rp_dati_vers_data_esecuzione_pagamento < n_dt_creazione::date) THEN
		result := 'PAA_IMPORT_DATA_SCADENZA_PAGAMENTO_OBBLIGATORIA';
		result_desc := 'dataScadenzaPagamento deve essere maggiore uguale alla data attuale per questo tipo dovuto';
		RETURN;
	END IF;	

	/* ************  VERIFICA ESISTENZA n_cod_iud SU TABELLA mygov_dovuto  ********** */

	SELECT   dovuto.*
	INTO     found_mygov_dovuto
	FROM     mygov_dovuto dovuto
	        ,mygov_flusso flusso
	        ,mygov_anagrafica_stato stato	    
	WHERE    flusso.mygov_flusso_id = dovuto.mygov_flusso_id
	AND      stato.mygov_anagrafica_stato_id = dovuto.mygov_anagrafica_stato_id
	AND      stato.cod_stato = 'INSERIMENTO_DOVUTO'
	AND      stato.de_tipo_stato = 'dovuto'
	AND      dovuto.cod_iud = n_cod_iud
	AND      dovuto.flg_dovuto_attuale = true
	AND      flusso.flg_attivo = true
	AND      flusso.mygov_ente_id = n_mygov_ente_id; 

	IF found_mygov_dovuto.mygov_dovuto_id IS NULL THEN 
	        result := 'PAA_IMPORT_DOVUTO_NON_PRESENTE'; 
		result_desc := 'Dovuto non presente o in pagamento nel database, impossibile modificarlo';
		RETURN;
	END IF; 

	IF found_mygov_dovuto.cod_tipo_dovuto <> n_cod_tipo_dovuto THEN 
	        result := 'PAA_IMPORT_TIPO_DOVUTO_NON_MODIFICABILE'; 
		result_desc := 'Non è possibile modificare il tipo dovuto di un dovuto';
		RETURN;
	END IF; 	

        /* ************  VERIFICA permessi dell''operatore su tipo dovuto del dovuto esistente ********** */
	
	SELECT INTO num_cod_tipo sum(
	     (SELECT count(1) 	
		FROM mygov_ente_tipo_dovuto metd
		   , mygov_flusso mf
		   , mygov_operatore_ente_tipo_dovuto tdo
		   , mygov_operatore o
		   , mygov_utente u
		WHERE metd.cod_tipo = found_mygov_dovuto.cod_tipo_dovuto 
			AND metd.mygov_ente_id = mf.mygov_ente_id 
			AND mf.mygov_flusso_id = found_mygov_dovuto.mygov_flusso_id
			AND tdo.mygov_ente_tipo_dovuto_id = metd.mygov_ente_tipo_dovuto_id
			AND tdo.flg_attivo = true
			AND tdo.mygov_operatore_id = o.mygov_operatore_id
			AND o.cod_fed_user_id = u.cod_fed_user_id
			AND u.mygov_utente_id = n_mygov_utente_id) 
		+
	    (SELECT count(1)
	       FROM mygov_utente u
	      WHERE split_part(u.cod_fed_user_id, '-', 2) = 'WS_USER' 
		AND u.mygov_utente_id = n_mygov_utente_id));
	    
	IF ((num_cod_tipo = 0) OR (num_cod_tipo > 1)) THEN
		result := 'PAA_IMPORT_OPERATORE_NO_PERMESSI';
		result_desc := 'L''operatore non ha i permessi per questo tipo dovuto e ente';
		RETURN;
	END IF;	

	/* ************     Valorizzo dt_creazione_cod_iuv solo se presente iuv in input    ********** */

	IF n_cod_iuv IS NOT NULL THEN
		n_dt_creazione_cod_iuv := n_dt_creazione;
	END IF;

	IF found_mygov_dovuto.cod_iuv IS NULL THEN 

		EXECUTE 'SELECT sposta_dovuto_in_dovuto_elaborato($1, $2, $3, 
			    (select stato.mygov_anagrafica_stato_id
			    from mygov_anagrafica_stato stato
			    where stato.cod_stato = ''ANNULLATO''
				AND stato.de_tipo_stato = ''dovuto'')
			    , $4, $5, $6, $7, ''-'', $8, $9, $10, $11, $12, 
			    $13, $14, $15, $16, $17, $18, $19, $20, $21, $22, $23, 
			    $24, $25)' INTO result USING found_mygov_dovuto.mygov_dovuto_id, found_mygov_dovuto.mygov_flusso_id,
			    found_mygov_dovuto.num_riga_flusso, found_mygov_dovuto.mygov_carrello_id, found_mygov_dovuto.cod_iud, found_mygov_dovuto.cod_iuv, n_dt_creazione, found_mygov_dovuto.cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco,
			    found_mygov_dovuto.cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, found_mygov_dovuto.de_rp_sogg_pag_anagrafica_pagatore, found_mygov_dovuto.de_rp_sogg_pag_indirizzo_pagatore, 
			    found_mygov_dovuto.de_rp_sogg_pag_civico_pagatore, found_mygov_dovuto.cod_rp_sogg_pag_cap_pagatore, found_mygov_dovuto.de_rp_sogg_pag_localita_pagatore, found_mygov_dovuto.de_rp_sogg_pag_provincia_pagatore,
			    found_mygov_dovuto.cod_rp_sogg_pag_nazione_pagatore, found_mygov_dovuto.de_rp_sogg_pag_email_pagatore, found_mygov_dovuto.dt_rp_dati_vers_data_esecuzione_pagamento,
			    found_mygov_dovuto.cod_rp_dati_vers_tipo_versamento, found_mygov_dovuto.num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, found_mygov_dovuto.num_rp_dati_vers_dati_sing_vers_commissione_carico_pa,
			    found_mygov_dovuto.cod_tipo_dovuto, found_mygov_dovuto.de_rp_dati_vers_dati_sing_vers_causale_versamento, found_mygov_dovuto.de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, found_mygov_dovuto.bilancio;

		IF result IS NOT NULL THEN
			result_desc := 'Dovuto non presente nel database o in pagamento, impossibile annullarlo';
			RETURN;
		END IF;
	
		IF n_cod_iuv IS NOT NULL THEN

			OPEN iuv_cursor;

			LOOP
				FETCH iuv_cursor INTO iuv_record;
				EXIT WHEN NOT FOUND;
				num_iuv = num_iuv + 1;
				iuv_insertable := is_dovuto_annullato(iuv_record.identificativo, 'IUV', iuv_record.mygov_flusso_id);
				IF iuv_insertable = false THEN			
					EXIT;  -- exit loop
				END IF;
			END LOOP;

			CLOSE iuv_cursor;

			IF (num_iuv > 0 AND iuv_insertable = false) THEN
				result := 'PAA_IMPORT_IUV_DUPLICATO';	
				result_desc := 'IUV duplicato nel database, non si accettano dovuti duplicati';
				RETURN;			
			END IF;

		/* ************  Inserisco su mygov_avviso_digitale se ho iuv valido e flg su ente a true  ********** */
											
			IF (	SELECT  flg_attivo
				FROM    mygov_ente_funzionalita 
				WHERE   cod_ipa_ente = val_cod_ipa_ente
				AND     cod_funzionalita = 'AVVISO_DIGITALE')  AND (length(n_cod_iuv) = 15 OR length(n_cod_iuv) = 17) AND n_dt_rp_dati_vers_data_esecuzione_pagamento IS NOT NULL AND insert_avv_dig THEN
					PERFORM insert_mygov_avviso_digitale(
									    n_mygov_ente_id,
									    n_dt_creazione,
									    n_cod_iuv,
									    n_de_rp_sogg_pag_anagrafica_pagatore,
									    n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco,
									    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
									    n_dt_rp_dati_vers_data_esecuzione_pagamento,
									    n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
									    n_de_rp_dati_vers_dati_sing_vers_causale_versamento
									);
			END IF;
			

			
		/* ************  Inserisce su mygov_avviso_digitale se ha iuv valido, flg su ente a true, cod_funzionalita = 'NOTIFICA_AVVISI_IO' e per il dovuto e'' prevista la notifica IO********** */

			IF (SELECT  flg_attivo
			    FROM    mygov_ente_funzionalita 
			    WHERE   cod_ipa_ente = val_cod_ipa_ente
			    AND     cod_funzionalita = 'NOTIFICA_AVVISI_IO') AND (length(n_cod_iuv) = 15 OR length(n_cod_iuv) = 17) AND notifica_io THEN
				PERFORM insert_mygov_avviso_digitale_notifica_io(
								    n_mygov_ente_id,
								    n_dt_creazione,
								    n_cod_iuv,
								    n_de_rp_sogg_pag_anagrafica_pagatore,
								    n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco,
								    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
								    n_dt_rp_dati_vers_data_esecuzione_pagamento,
								    n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
								    n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
									n_de_rp_sogg_pag_email_pagatore
								);
			END IF;

		END IF;

		IF (n_cod_iuv IS NOT NULL AND num_iuv > 0 AND iuv_insertable = true) THEN
			UPDATE mygov_identificativo_univoco SET mygov_flusso_id = n_mygov_flusso_id WHERE mygov_ente_id = n_mygov_ente_id AND cod_tipo_identificativo = 'IUV' AND identificativo = n_cod_iuv;	
		ELSIF (n_cod_iuv IS NOT NULL AND num_iuv = 0) THEN
			INSERT INTO mygov_identificativo_univoco(mygov_identificativo_univoco_id
								,"version"
								, mygov_ente_id
								, mygov_flusso_id
								, cod_tipo_identificativo
								, identificativo
								, dt_inserimento)
							   VALUES (nextval('mygov_identificativo_univoco_id_seq')
								, 0
								, n_mygov_ente_id
								, n_mygov_flusso_id
								, 'IUV'
								, n_cod_iuv
								, n_dt_creazione);	
		END IF;	 
		
		INSERT INTO mygov_dovuto(
		    mygov_dovuto_id, "version", flg_dovuto_attuale, mygov_flusso_id, 
		    num_riga_flusso, mygov_anagrafica_stato_id, mygov_carrello_id, 
		    cod_iud, cod_iuv, dt_creazione, dt_ultima_modifica, cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
		    cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, de_rp_sogg_pag_anagrafica_pagatore, 
		    de_rp_sogg_pag_indirizzo_pagatore, de_rp_sogg_pag_civico_pagatore, 
		    cod_rp_sogg_pag_cap_pagatore, de_rp_sogg_pag_localita_pagatore, 
		    de_rp_sogg_pag_provincia_pagatore, cod_rp_sogg_pag_nazione_pagatore, 
		    de_rp_sogg_pag_email_pagatore, dt_rp_dati_vers_data_esecuzione_pagamento, 
		    cod_rp_dati_vers_tipo_versamento, num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
		    num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, cod_tipo_dovuto,
		    de_rp_dati_vers_dati_sing_vers_causale_versamento, de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, dt_creazione_cod_iuv,
			bilancio,flg_genera_iuv)

		    VALUES (nextval('mygov_dovuto_mygov_dovuto_id_seq'), 0, true, n_mygov_flusso_id, 
		    n_num_riga_flusso, n_mygov_anagrafica_stato_id, n_mygov_carrello_id, 
		    n_cod_iud, n_cod_iuv, n_dt_creazione, n_dt_creazione, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
		    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, n_de_rp_sogg_pag_anagrafica_pagatore, 
		    n_de_rp_sogg_pag_indirizzo_pagatore, n_de_rp_sogg_pag_civico_pagatore, 
		    n_cod_rp_sogg_pag_cap_pagatore, n_de_rp_sogg_pag_localita_pagatore, 
		    n_de_rp_sogg_pag_provincia_pagatore, n_cod_rp_sogg_pag_nazione_pagatore, 
		    n_de_rp_sogg_pag_email_pagatore, n_dt_rp_dati_vers_data_esecuzione_pagamento, 
		    n_cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
		    n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, n_cod_tipo_dovuto,
		    n_de_rp_dati_vers_dati_sing_vers_causale_versamento, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_dt_creazione_cod_iuv,
			n_bilancio,n_flg_genera_iuv); 
		    

	ELSE /*SE HO IUV OLD */
		IF ((n_cod_iuv IS NOT NULL) AND (n_cod_iuv <> found_mygov_dovuto.cod_iuv)) THEN /* SE HO NEW E <> OLD */
			result := 'PAA_IMPORT_IUV_DUPLICATO'; 
			result_desc := 'Non è possibile modificare il codice iuv di un dovuto';
			RETURN;						
		ELSIF ((n_cod_iuv IS NOT NULL) AND (n_cod_iuv = found_mygov_dovuto.cod_iuv)) THEN /* SE HO NEW E = OLD */		
			IF (	SELECT  flg_attivo
				FROM    mygov_ente_funzionalita 
				WHERE   cod_ipa_ente = val_cod_ipa_ente
				AND     cod_funzionalita = 'AVVISO_DIGITALE') AND length(n_cod_iuv) = 15 THEN

				UPDATE mygov_avviso_digitale 
				SET    de_ad_sog_pag_anag_pagatore = n_de_rp_sogg_pag_anagrafica_pagatore
				      ,cod_ad_sog_pag_id_univ_pag_tipo_id_univ = n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco
				      ,cod_ad_sog_pag_id_univ_pag_cod_id_univ = n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco
				      ,dt_ad_data_scadenza_pagamento = n_dt_rp_dati_vers_data_esecuzione_pagamento
				      ,dt_ad_data_scadenza_avviso = n_dt_rp_dati_vers_data_esecuzione_pagamento
				      ,num_ad_importo_avviso = n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento
				      ,de_ad_desc_pagamento = n_de_rp_dati_vers_dati_sing_vers_causale_versamento
				      ,dt_ultima_modifica = n_dt_creazione
				WHERE  cod_ad_cod_avviso = '0' || val_application_code || found_mygov_dovuto.cod_iuv
				AND    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
								    FROM   mygov_anagrafica_stato
								    WHERE  cod_stato = 'NUOVO'
								    AND    de_tipo_stato = 'ad');								    
			END IF;

			IF (	SELECT  flg_attivo
				FROM    mygov_ente_funzionalita 
				WHERE   cod_ipa_ente = val_cod_ipa_ente
				AND     cod_funzionalita = 'AVVISO_DIGITALE') AND length(n_cod_iuv) = 17 AND n_dt_rp_dati_vers_data_esecuzione_pagamento IS NOT NULL THEN

				UPDATE mygov_avviso_digitale 
				SET    de_ad_sog_pag_anag_pagatore = n_de_rp_sogg_pag_anagrafica_pagatore
				      ,cod_ad_sog_pag_id_univ_pag_tipo_id_univ = n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco
				      ,cod_ad_sog_pag_id_univ_pag_cod_id_univ = n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco
				      ,dt_ad_data_scadenza_pagamento = n_dt_rp_dati_vers_data_esecuzione_pagamento
				      ,dt_ad_data_scadenza_avviso = n_dt_rp_dati_vers_data_esecuzione_pagamento
				      ,num_ad_importo_avviso = n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento
				      ,de_ad_desc_pagamento = n_de_rp_dati_vers_dati_sing_vers_causale_versamento
				      ,dt_ultima_modifica = n_dt_creazione
				WHERE  cod_ad_cod_avviso = '3' || found_mygov_dovuto.cod_iuv
				AND    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
								    FROM   mygov_anagrafica_stato
								    WHERE  cod_stato = 'NUOVO'
								    AND    de_tipo_stato = 'ad');								    
			END IF;
			
			
	
			IF (	SELECT  flg_attivo
				FROM    mygov_ente_funzionalita 
				WHERE   cod_ipa_ente = val_cod_ipa_ente
				AND     cod_funzionalita = 'NOTIFICA_AVVISI_IO') AND length(n_cod_iuv) = 15 AND notifica_io THEN

				UPDATE mygov_avviso_digitale 
				SET    de_ad_sog_pag_anag_pagatore = n_de_rp_sogg_pag_anagrafica_pagatore
				      ,cod_ad_sog_pag_id_univ_pag_tipo_id_univ = n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco
				      ,cod_ad_sog_pag_id_univ_pag_cod_id_univ = n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco
				      ,dt_ad_data_scadenza_pagamento = n_dt_rp_dati_vers_data_esecuzione_pagamento
				      ,dt_ad_data_scadenza_avviso = n_dt_rp_dati_vers_data_esecuzione_pagamento
				      ,num_ad_importo_avviso = n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento
				      ,de_ad_desc_pagamento = n_de_rp_dati_vers_dati_sing_vers_causale_versamento
				      ,dt_ultima_modifica = n_dt_creazione
					  ,de_ad_email_soggetto = n_de_rp_sogg_pag_email_pagatore
				WHERE  cod_ad_cod_avviso = '0_' || val_application_code || '_' || found_mygov_dovuto.cod_iuv 
				AND    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
								    FROM   mygov_anagrafica_stato
								    WHERE  cod_stato = 'NUOVO'
								    AND    de_tipo_stato = 'naIO');								    
			END IF;

			IF (	SELECT  flg_attivo
				FROM    mygov_ente_funzionalita 
				WHERE   cod_ipa_ente = val_cod_ipa_ente
				AND     cod_funzionalita = 'NOTIFICA_AVVISI_IO') AND length(n_cod_iuv) = 17 AND notifica_io THEN

				UPDATE mygov_avviso_digitale 
				SET    de_ad_sog_pag_anag_pagatore = n_de_rp_sogg_pag_anagrafica_pagatore
				      ,cod_ad_sog_pag_id_univ_pag_tipo_id_univ = n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco
				      ,cod_ad_sog_pag_id_univ_pag_cod_id_univ = n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco
				      ,dt_ad_data_scadenza_pagamento = n_dt_rp_dati_vers_data_esecuzione_pagamento
				      ,dt_ad_data_scadenza_avviso = n_dt_rp_dati_vers_data_esecuzione_pagamento
				      ,num_ad_importo_avviso = n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento
				      ,de_ad_desc_pagamento = n_de_rp_dati_vers_dati_sing_vers_causale_versamento
				      ,dt_ultima_modifica = n_dt_creazione
					  ,de_ad_email_soggetto = n_de_rp_sogg_pag_email_pagatore
				WHERE  cod_ad_cod_avviso = '3_' || found_mygov_dovuto.cod_iuv
				AND    mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id
								    FROM   mygov_anagrafica_stato
								    WHERE  cod_stato = 'NUOVO'
								    AND    de_tipo_stato = 'naIO');								    
			END IF;
			
	
			
			
		ELSIF (n_cod_iuv IS NULL) THEN /* SE HO OLD E NO NEW */
			result := 'PAA_IMPORT_IUV_NO_NULLABLE'; 
			result_desc := 'Non è possibile azzerare il codice iuv di un dovuto';
			RETURN;
		END IF;
		
		
	        EXECUTE 'select sposta_dovuto_in_dovuto_elaborato($1, $2, $3,
		    (select stato.mygov_anagrafica_stato_id
		       from mygov_anagrafica_stato stato
		      where stato.cod_stato = ''ANNULLATO''
		        AND stato.de_tipo_stato = ''dovuto'')
		    , $4, $5, $6, $7, ''-'', $8, $9, $10, $11, $12, 
		    $13, $14, $15, $16, $17, $18, $19, $20, $21, $22, $23, 
		    $24, $25)' INTO result USING found_mygov_dovuto.mygov_dovuto_id, found_mygov_dovuto.mygov_flusso_id, 
		    found_mygov_dovuto.num_riga_flusso, found_mygov_dovuto.mygov_carrello_id, found_mygov_dovuto.cod_iud, found_mygov_dovuto.cod_iuv, n_dt_creazione, 
		    found_mygov_dovuto.cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, found_mygov_dovuto.cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, 
		    found_mygov_dovuto.de_rp_sogg_pag_anagrafica_pagatore, found_mygov_dovuto.de_rp_sogg_pag_indirizzo_pagatore, found_mygov_dovuto.de_rp_sogg_pag_civico_pagatore, 
		    found_mygov_dovuto.cod_rp_sogg_pag_cap_pagatore, found_mygov_dovuto.de_rp_sogg_pag_localita_pagatore, found_mygov_dovuto.de_rp_sogg_pag_provincia_pagatore, 
		    found_mygov_dovuto.cod_rp_sogg_pag_nazione_pagatore, found_mygov_dovuto.de_rp_sogg_pag_email_pagatore, found_mygov_dovuto.dt_rp_dati_vers_data_esecuzione_pagamento, 
		    found_mygov_dovuto.cod_rp_dati_vers_tipo_versamento, found_mygov_dovuto.num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
		    found_mygov_dovuto.num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, found_mygov_dovuto.cod_tipo_dovuto, found_mygov_dovuto.de_rp_dati_vers_dati_sing_vers_causale_versamento, 
		    found_mygov_dovuto.de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, found_mygov_dovuto.bilancio;

		IF result IS NOT NULL THEN
			result_desc := 'Dovuto non presente nel database o in pagamento, impossibile annullarlo';
			RETURN;
		END IF;    
		
		INSERT INTO mygov_dovuto(
		    mygov_dovuto_id, "version", flg_dovuto_attuale, mygov_flusso_id, 
		    num_riga_flusso, mygov_anagrafica_stato_id, mygov_carrello_id, 
		    cod_iud, cod_iuv, dt_creazione, dt_ultima_modifica, cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
		    cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, de_rp_sogg_pag_anagrafica_pagatore, 
		    de_rp_sogg_pag_indirizzo_pagatore, de_rp_sogg_pag_civico_pagatore, 
		    cod_rp_sogg_pag_cap_pagatore, de_rp_sogg_pag_localita_pagatore, 
		    de_rp_sogg_pag_provincia_pagatore, cod_rp_sogg_pag_nazione_pagatore, 
		    de_rp_sogg_pag_email_pagatore, dt_rp_dati_vers_data_esecuzione_pagamento, 
		    cod_rp_dati_vers_tipo_versamento, num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
		    num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, cod_tipo_dovuto,
		    de_rp_dati_vers_dati_sing_vers_causale_versamento, de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, dt_creazione_cod_iuv, bilancio,flg_genera_iuv)

		    VALUES (nextval('mygov_dovuto_mygov_dovuto_id_seq'), 0, true, n_mygov_flusso_id, 
		    n_num_riga_flusso, n_mygov_anagrafica_stato_id, n_mygov_carrello_id, 
		    n_cod_iud, n_cod_iuv, n_dt_creazione, n_dt_creazione, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
		    n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, n_de_rp_sogg_pag_anagrafica_pagatore, 
		    n_de_rp_sogg_pag_indirizzo_pagatore, n_de_rp_sogg_pag_civico_pagatore, 
		    n_cod_rp_sogg_pag_cap_pagatore, n_de_rp_sogg_pag_localita_pagatore, 
		    n_de_rp_sogg_pag_provincia_pagatore, n_cod_rp_sogg_pag_nazione_pagatore, 
		    n_de_rp_sogg_pag_email_pagatore, n_dt_rp_dati_vers_data_esecuzione_pagamento, 
		    n_cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
		    n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, n_cod_tipo_dovuto,
		    n_de_rp_dati_vers_dati_sing_vers_causale_versamento, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_dt_creazione_cod_iuv, n_bilancio,n_flg_genera_iuv); 
	END IF;


    END;
$_$;


ALTER FUNCTION public.modify_mygov_dovuto(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, INOUT n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean, n_flg_genera_iuv boolean, OUT result character varying, OUT result_desc character varying) OWNER TO postgres;

--
-- TOC entry 314 (class 1255 OID 202010)
-- Name: modify_mygov_dovuto_noinout(bigint, bigint, numeric, bigint, bigint, character varying, character varying, timestamp without time zone, character, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, date, character varying, numeric, numeric, character varying, character varying, character varying, bigint, character varying, boolean, boolean); Type: FUNCTION; Schema: public; Owner: mypay4
--

CREATE FUNCTION public.modify_mygov_dovuto_noinout(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean DEFAULT true, n_flg_genera_iuv boolean DEFAULT NULL::boolean, OUT result character varying, OUT result_desc character varying) RETURNS record
    LANGUAGE plpgsql
    AS $$
  DECLARE
    n_num_riga_flusso_out numeric;
  BEGIN
    select * from public.modify_mygov_dovuto(n_mygov_ente_id, n_mygov_flusso_id, n_num_riga_flusso, n_mygov_anagrafica_stato_id, 
      n_mygov_carrello_id, n_cod_iud, n_cod_iuv, n_dt_creazione, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, 
      n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, n_de_rp_sogg_pag_anagrafica_pagatore, n_de_rp_sogg_pag_indirizzo_pagatore, 
      n_de_rp_sogg_pag_civico_pagatore , n_cod_rp_sogg_pag_cap_pagatore, n_de_rp_sogg_pag_localita_pagatore, n_de_rp_sogg_pag_provincia_pagatore,
      n_cod_rp_sogg_pag_nazione_pagatore, n_de_rp_sogg_pag_email_pagatore, n_dt_rp_dati_vers_data_esecuzione_pagamento, 
      n_cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, 
      n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, n_cod_tipo_dovuto, n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
      n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_mygov_utente_id, n_bilancio, insert_avv_dig, n_flg_genera_iuv)
    into n_num_riga_flusso_out, result, result_desc;
    END;
$$;


ALTER FUNCTION public.modify_mygov_dovuto_noinout(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean, n_flg_genera_iuv boolean, OUT result character varying, OUT result_desc character varying) OWNER TO mypay4;

--
-- TOC entry 309 (class 1255 OID 20873)
-- Name: sposta_dovuto_in_dovuto_elaborato(bigint, bigint, numeric, bigint, bigint, character varying, character varying, timestamp without time zone, character varying, character, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, date, character varying, numeric, numeric, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.sposta_dovuto_in_dovuto_elaborato(id_dovuto_cancellare bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_de_rp_versione_oggetto character varying, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_bilancio character varying) RETURNS character varying
    LANGUAGE plpgsql
    AS $$

    BEGIN   
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
$$;


ALTER FUNCTION public.sposta_dovuto_in_dovuto_elaborato(id_dovuto_cancellare bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_de_rp_versione_oggetto character varying, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_bilancio character varying) OWNER TO postgres;

--
-- TOC entry 310 (class 1255 OID 20874)
-- Name: update_insert_flusso(bigint, character varying, timestamp without time zone, timestamp without time zone); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_insert_flusso(n_mygov_ente_id bigint, n_iuf character varying, n_dt_creazione timestamp without time zone, n_dt_ultima_modifica timestamp without time zone, OUT sequence_value bigint, OUT eccezione character varying) RETURNS record
    LANGUAGE plpgsql
    AS $$

    DECLARE
	in_caricamento integer;
	inseriti integer;
	--sequence_value bigint;
    BEGIN  
	SELECT INTO sequence_value nextval('mygov_flusso_mygov_flusso_id_seq');
	
/* ******************************************************************

Controllo se sto caricando un flusso con lo stesso nome, 
oppure se l'eventuale flusso in caricamento lo e' da piu' di 24 ore

********************************************************************* */
               
	SELECT INTO in_caricamento count(1) as conta

	FROM mygov_flusso flusso
	    ,mygov_anagrafica_stato stati
	    
	WHERE flusso.mygov_anagrafica_stato_id = stati.mygov_anagrafica_stato_id
	  AND flusso.mygov_ente_id = n_mygov_ente_id
	  AND flusso.iuf = n_iuf
	  AND EXTRACT(EPOCH FROM (n_dt_ultima_modifica - INTERVAL '24 hour' - flusso.dt_ultima_modifica)) < 0
	  AND stati.cod_stato = 'LOAD_FLOW'
	  AND stati.de_tipo_stato = 'flusso';

	IF in_caricamento > 0
	THEN eccezione := 'InCaricamentoFlussoException';   		
	END IF;
	    
/* *****************************************************************

Controllo se ho caricato un flusso con lo stesso nome 
e il caricamento e' andato a buon fine 

******************************************************************** */

	SELECT INTO inseriti count(1) as conta

	FROM mygov_flusso flusso
	    ,mygov_anagrafica_stato stati
	    
	WHERE flusso.mygov_anagrafica_stato_id = stati.mygov_anagrafica_stato_id
	  AND flusso.mygov_ente_id = n_mygov_ente_id
	  AND flusso.iuf = n_iuf
	  AND flusso.flg_attivo = true
	  AND stati.cod_stato = 'CARICATO'
	  AND stati.de_tipo_stato = 'flusso';

	IF inseriti > 0
	THEN eccezione := 'duplicatoFlussoException';   		
	END IF;
	    	    

	INSERT INTO mygov_flusso(
	    mygov_flusso_id, "version", mygov_ente_id, mygov_anagrafica_stato_id, 
	    iuf, num_righe_totali, num_righe_importate_correttamente, dt_creazione, 
	    dt_ultima_modifica, flg_attivo, pdf_generati)
	VALUES (sequence_value, 0, n_mygov_ente_id, (SELECT mygov_anagrafica_stato_id 
						 FROM mygov_anagrafica_stato 
						WHERE cod_stato = 'LOAD_FLOW'
						  AND de_tipo_stato = 'flusso'), 
		    n_iuf, 0, 0, n_dt_creazione, 
		    n_dt_ultima_modifica, false, 0);

	    
    END;
$$;


ALTER FUNCTION public.update_insert_flusso(n_mygov_ente_id bigint, n_iuf character varying, n_dt_creazione timestamp without time zone, n_dt_ultima_modifica timestamp without time zone, OUT sequence_value bigint, OUT eccezione character varying) OWNER TO postgres;

--
-- TOC entry 311 (class 1255 OID 20875)
-- Name: update_mygov_export_dovuti(bigint, timestamp without time zone); Type: FUNCTION; Schema: public; Owner: mypay4
--

CREATE FUNCTION public.update_mygov_export_dovuti(n_mygov_export_dovuti_id bigint, n_dt_ultima_modifica timestamp without time zone) RETURNS integer
    LANGUAGE plpgsql
    AS $$

    BEGIN  
    	
           UPDATE mygov_export_dovuti
           
              SET mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id 
                                                 FROM mygov_anagrafica_stato 
                                                WHERE cod_stato = 'EXPORT_IN_ELAB'
                                                  AND de_tipo_stato = 'export'),
                  version = version + 1,
                  dt_ultima_modifica = n_dt_ultima_modifica
                                                  
	    WHERE mygov_export_dovuti_id = n_mygov_export_dovuti_id
	      AND mygov_anagrafica_stato_id = (SELECT mygov_anagrafica_stato_id 
                                                 FROM mygov_anagrafica_stato 
                                                WHERE cod_stato = 'LOAD_EXPORT'
                                                  AND de_tipo_stato = 'export');

          IF NOT FOUND THEN 
	        RETURN 0;
	  ELSE RETURN 1; 
	  END IF;
	    
    END;
$$;


ALTER FUNCTION public.update_mygov_export_dovuti(n_mygov_export_dovuti_id bigint, n_dt_ultima_modifica timestamp without time zone) OWNER TO mypay4;

SET default_tablespace = '';

--
-- TOC entry 272 (class 1259 OID 32814)
-- Name: bck_mygov_dovuto; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.bck_mygov_dovuto (
    mygov_dovuto_id bigint,
    version integer,
    flg_dovuto_attuale boolean,
    mygov_flusso_id bigint,
    num_riga_flusso numeric(12,0),
    mygov_anagrafica_stato_id bigint,
    mygov_carrello_id bigint,
    cod_iud character varying(35),
    cod_iuv character varying(35),
    dt_creazione timestamp without time zone,
    dt_ultima_modifica timestamp without time zone,
    cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character(1),
    cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying(35),
    de_rp_sogg_pag_anagrafica_pagatore character varying(70),
    de_rp_sogg_pag_indirizzo_pagatore character varying(70),
    de_rp_sogg_pag_civico_pagatore character varying(16),
    cod_rp_sogg_pag_cap_pagatore character varying(16),
    de_rp_sogg_pag_localita_pagatore character varying(35),
    de_rp_sogg_pag_provincia_pagatore character varying(2),
    cod_rp_sogg_pag_nazione_pagatore character varying(2),
    de_rp_sogg_pag_email_pagatore character varying(256),
    dt_rp_dati_vers_data_esecuzione_pagamento date,
    cod_rp_dati_vers_tipo_versamento character varying(32),
    num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric(12,2),
    num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric(12,2),
    de_rp_dati_vers_dati_sing_vers_causale_versamento character varying(1024),
    de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying(140),
    cod_tipo_dovuto character varying(64),
    mygov_avviso_id bigint,
    dt_creazione_cod_iuv timestamp without time zone,
    mygov_dati_marca_bollo_digitale_id bigint,
    de_causale_visualizzata character varying(1024),
    bilancio character varying(4096),
    flg_genera_iuv boolean,
    id_session character varying(36)
);


ALTER TABLE public.bck_mygov_dovuto OWNER TO mypay4;

--
-- TOC entry 270 (class 1259 OID 32772)
-- Name: dovuti_temp_x_import_rabbitmq; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.dovuti_temp_x_import_rabbitmq (
    "IUD" character varying(100) NOT NULL,
    "codIuv" character varying(100),
    "tipoIdentificativoUnivoco" character varying(100) NOT NULL,
    "codiceIdentificativoUnivoco" character varying(100) NOT NULL,
    "anagraficaPagatore" character varying(100) NOT NULL,
    "indirizzoPagatore" character varying(100),
    "civicoPagatore" character varying(100),
    "capPagatore" character varying(100),
    "localitaPagatore" character varying(100),
    "provinciaPagatore" character varying(100),
    "nazionePagatore" character varying(100),
    "emailPagatore" character varying(100),
    "dataEsecuzionePagamento" character varying(100) NOT NULL,
    "importoDovuto" character varying(100) NOT NULL,
    "commissioneCaricoPa" character varying(100),
    "tipoDovuto" character varying(100),
    "tipoVersamento" character varying(100),
    "causaleVersamento" character varying(100) NOT NULL,
    "datiSpecificiRiscossione" character varying(100) NOT NULL,
    bilancio character varying(100),
    azione character varying(100)
);


ALTER TABLE public.dovuti_temp_x_import_rabbitmq OWNER TO mypay4;

--
-- TOC entry 197 (class 1259 OID 20876)
-- Name: mygov_anagrafica_stato; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_anagrafica_stato (
    mygov_anagrafica_stato_id bigint NOT NULL,
    cod_stato character varying(80) NOT NULL,
    de_stato character varying(100) NOT NULL,
    de_tipo_stato character varying(6) NOT NULL,
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL
);


ALTER TABLE public.mygov_anagrafica_stato OWNER TO mypay4;

--
-- TOC entry 198 (class 1259 OID 20879)
-- Name: mygov_anagrafica_stato_mygov_anagrafica_stato_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_anagrafica_stato_mygov_anagrafica_stato_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_anagrafica_stato_mygov_anagrafica_stato_id_seq OWNER TO mypay4;

--
-- TOC entry 199 (class 1259 OID 20881)
-- Name: mygov_avviso; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_avviso (
    mygov_avviso_id bigint NOT NULL,
    version integer NOT NULL,
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL,
    mygov_ente_id bigint NOT NULL,
    cod_iuv character varying(35) NOT NULL,
    cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character(1) NOT NULL,
    cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying(35) NOT NULL,
    de_rp_sogg_pag_anagrafica_pagatore character varying(70) NOT NULL,
    de_rp_sogg_pag_indirizzo_pagatore character varying(70),
    de_rp_sogg_pag_civico_pagatore character varying(16),
    cod_rp_sogg_pag_cap_pagatore character varying(16),
    de_rp_sogg_pag_localita_pagatore character varying(35),
    de_rp_sogg_pag_provincia_pagatore character varying(2),
    cod_rp_sogg_pag_nazione_pagatore character varying(2),
    de_rp_sogg_pag_email_pagatore character varying(256) NOT NULL
);


ALTER TABLE public.mygov_avviso OWNER TO mypay4;

--
-- TOC entry 200 (class 1259 OID 20887)
-- Name: mygov_avviso_anonimo; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_avviso_anonimo (
    mygov_avviso_anonimo_id bigint NOT NULL,
    cod_ipa_ente character varying(80) NOT NULL,
    cod_iuv character varying(35) NOT NULL,
    id_session character varying(36) NOT NULL,
    de_email_address character varying(256),
    email_verificata boolean DEFAULT false NOT NULL,
    mygov_carrello_id bigint,
    dt_creazione timestamp without time zone,
    ente_sil_invia_risposta_pagamento_url character varying(2048),
    version integer NOT NULL
);


ALTER TABLE public.mygov_avviso_anonimo OWNER TO mypay4;

--
-- TOC entry 201 (class 1259 OID 20894)
-- Name: mygov_avviso_anonimo_mygov_avviso_anonimo_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_avviso_anonimo_mygov_avviso_anonimo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_avviso_anonimo_mygov_avviso_anonimo_id_seq OWNER TO mypay4;

--
-- TOC entry 202 (class 1259 OID 20896)
-- Name: mygov_avviso_digitale; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_avviso_digitale (
    mygov_avviso_digitale_id bigint NOT NULL,
    version integer NOT NULL,
    cod_ad_id_dominio character varying(35) NOT NULL,
    de_ad_anag_beneficiario character varying(35) NOT NULL,
    cod_ad_id_messaggio_richiesta character varying(20),
    cod_ad_cod_avviso character varying(20) NOT NULL,
    de_ad_sog_pag_anag_pagatore character varying(70) NOT NULL,
    cod_ad_sog_pag_id_univ_pag_tipo_id_univ character varying(1) NOT NULL,
    cod_ad_sog_pag_id_univ_pag_cod_id_univ character varying(35) NOT NULL,
    dt_ad_data_scadenza_pagamento date,
    dt_ad_data_scadenza_avviso date,
    num_ad_importo_avviso numeric(12,2) NOT NULL,
    de_ad_email_soggetto character varying(256),
    de_ad_cellulare_soggetto character varying(35),
    de_ad_desc_pagamento character varying(10000) NOT NULL,
    de_ad_url_avviso character varying(140),
    cod_id_flusso_av character varying(70),
    cod_e_ad_id_dominio character varying(35),
    cod_e_ad_id_messaggio_richiesta character varying(20),
    cod_id_flusso_e character varying(70),
    mygov_anagrafica_stato_id bigint NOT NULL,
    num_ad_tentativi_invio integer DEFAULT 1,
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL,
    cod_tassonomia_avviso character varying(2) DEFAULT '00'::character varying NOT NULL,
    dati_sing_vers_iban_accredito character varying(35),
    dati_sing_vers_iban_appoggio character varying(35),
    tipo_pagamento integer NOT NULL,
    tipo_operazione character varying(1) NOT NULL
);


ALTER TABLE public.mygov_avviso_digitale OWNER TO mypay4;

--
-- TOC entry 203 (class 1259 OID 20904)
-- Name: mygov_avviso_digitale_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_avviso_digitale_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_avviso_digitale_id_seq OWNER TO mypay4;

--
-- TOC entry 204 (class 1259 OID 20906)
-- Name: mygov_avviso_mygov_avviso_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_avviso_mygov_avviso_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_avviso_mygov_avviso_id_seq OWNER TO mypay4;

--
-- TOC entry 205 (class 1259 OID 20908)
-- Name: mygov_avviso_tassa_auto; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_avviso_tassa_auto (
    mygov_avviso_tassa_auto_id bigint NOT NULL,
    mygov_dovuto_id bigint NOT NULL,
    regione_residenza character varying(11),
    tipo_veicolo_targa character varying(1),
    veicolo_targa character varying(8),
    tipo_veicolo_telaio character varying(1),
    numero_telaio_veicolo character varying(17),
    intestatario_veicolo character varying(16),
    dt_validita_avviso timestamp without time zone,
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL
);


ALTER TABLE public.mygov_avviso_tassa_auto OWNER TO mypay4;

--
-- TOC entry 206 (class 1259 OID 20911)
-- Name: mygov_avviso_tassa_auto_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_avviso_tassa_auto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_avviso_tassa_auto_id_seq OWNER TO mypay4;

--
-- TOC entry 207 (class 1259 OID 20913)
-- Name: mygov_carrello; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_carrello (
    mygov_carrello_id bigint NOT NULL,
    version integer NOT NULL,
    mygov_anagrafica_stato_id bigint NOT NULL,
    cod_ack_rp character(2),
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica_rp timestamp without time zone,
    dt_ultima_modifica_e timestamp without time zone,
    cod_rp_silinviarp_id_psp character varying(35),
    cod_rp_silinviarp_id_intermediario_psp character varying(35),
    cod_rp_silinviarp_id_canale character varying(35),
    cod_rp_silinviarp_id_dominio character varying(35),
    cod_rp_silinviarp_id_univoco_versamento character varying(35),
    cod_rp_silinviarp_codice_contesto_pagamento character varying(35),
    de_rp_silinviarp_esito character varying(256),
    cod_rp_silinviarp_redirect integer,
    cod_rp_silinviarp_url character varying(256),
    cod_rp_silinviarp_fault_code character varying(256),
    de_rp_silinviarp_fault_string character varying(256),
    cod_rp_silinviarp_id character varying(256),
    de_rp_silinviarp_description character varying(1024),
    cod_rp_silinviarp_serial integer,
    de_rp_versione_oggetto character varying(16) NOT NULL,
    cod_rp_dom_id_dominio character(35),
    cod_rp_dom_id_stazione_richiedente character varying(35),
    cod_rp_id_messaggio_richiesta character varying(35),
    dt_rp_data_ora_messaggio_richiesta timestamp without time zone,
    cod_rp_autenticazione_soggetto character varying(4),
    cod_rp_sogg_vers_id_univ_vers_tipo_id_univoco character(1),
    cod_rp_sogg_vers_id_univ_vers_codice_id_univoco character varying(35),
    cod_rp_sogg_vers_anagrafica_versante character varying(70),
    de_rp_sogg_vers_indirizzo_versante character varying(70),
    de_rp_sogg_vers_civico_versante character varying(16),
    cod_rp_sogg_vers_cap_versante character varying(16),
    de_rp_sogg_vers_localita_versante character varying(35),
    de_rp_sogg_vers_provincia_versante character varying(35),
    cod_rp_sogg_vers_nazione_versante character varying(2),
    de_rp_sogg_vers_email_versante character varying(256),
    cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character(1),
    cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying(35),
    de_rp_sogg_pag_anagrafica_pagatore character varying(70),
    de_rp_sogg_pag_indirizzo_pagatore character varying(70),
    de_rp_sogg_pag_civico_pagatore character varying(16),
    cod_rp_sogg_pag_cap_pagatore character varying(16),
    de_rp_sogg_pag_localita_pagatore character varying(35),
    de_rp_sogg_pag_provincia_pagatore character varying(35),
    cod_rp_sogg_pag_nazione_pagatore character varying(2),
    de_rp_sogg_pag_email_pagatore character varying(256),
    dt_rp_dati_vers_data_esecuzione_pagamento timestamp without time zone,
    num_rp_dati_vers_importo_totale_da_versare numeric(12,2) NOT NULL,
    cod_rp_dati_vers_tipo_versamento character varying(32) NOT NULL,
    cod_rp_dati_vers_id_univoco_versamento character varying(35),
    cod_rp_dati_vers_codice_contesto_pagamento character varying(35),
    de_rp_dati_vers_iban_addebito character varying(35),
    de_rp_dati_vers_bic_addebito character varying(11),
    cod_e_silinviaesito_id_dominio character varying(35),
    cod_e_silinviaesito_id_univoco_versamento character varying(35),
    cod_e_silinviaesito_codice_contesto_pagamento character varying(35),
    de_e_silinviaesito_esito character varying(256),
    cod_e_silinviaesito_fault_code character varying(256),
    de_e_silinviaesito_fault_string character varying(256),
    cod_e_silinviaesito_id character varying(256),
    de_e_silinviaesito_description character varying(1024),
    cod_e_silinviaesito_serial integer,
    de_e_versione_oggetto character varying(16),
    cod_e_dom_id_dominio character(35),
    cod_e_dom_id_stazione_richiedente character varying(35),
    cod_e_id_messaggio_ricevuta character varying(35),
    cod_e_data_ora_messaggio_ricevuta timestamp without time zone,
    cod_e_riferimento_messaggio_richiesta character varying(35),
    cod_e_riferimento_data_richiesta timestamp without time zone,
    cod_e_istit_att_id_univ_att_tipo_id_univoco character(1),
    cod_e_istit_att_id_univ_att_codice_id_univoco character varying(35),
    de_e_istit_att_denominazione_attestante character varying(70),
    cod_e_istit_att_codice_unit_oper_attestante character varying(35),
    de_e_istit_att_denom_unit_oper_attestante character varying(70),
    de_e_istit_att_indirizzo_attestante character varying(70),
    de_e_istit_att_civico_attestante character varying(16),
    cod_e_istit_att_cap_attestante character varying(16),
    de_e_istit_att_localita_attestante character varying(35),
    de_e_istit_att_provincia_attestante character varying(35),
    cod_e_istit_att_nazione_attestante character varying(2),
    cod_e_ente_benef_id_univ_benef_tipo_id_univoco character(1),
    cod_e_ente_benef_id_univ_benef_codice_id_univoco character varying(35),
    de_e_ente_benef_denominazione_beneficiario character varying(70),
    cod_e_ente_benef_codice_unit_oper_beneficiario character varying(35),
    de_e_ente_benef_denom_unit_oper_beneficiario character varying(70),
    de_e_ente_benef_indirizzo_beneficiario character varying(70),
    de_e_ente_benef_civico_beneficiario character varying(16),
    cod_e_ente_benef_cap_beneficiario character varying(16),
    de_e_ente_benef_localita_beneficiario character varying(35),
    de_e_ente_benef_provincia_beneficiario character varying(35),
    cod_e_ente_benef_nazione_beneficiario character varying(2),
    cod_e_sogg_vers_id_univ_vers_tipo_id_univoco character(1),
    cod_e_sogg_vers_id_univ_vers_codice_id_univoco character varying(35),
    cod_e_sogg_vers_anagrafica_versante character varying(70),
    de_e_sogg_vers_indirizzo_versante character varying(70),
    de_e_sogg_vers_civico_versante character varying(16),
    cod_e_sogg_vers_cap_versante character varying(16),
    de_e_sogg_vers_localita_versante character varying(35),
    de_e_sogg_vers_provincia_versante character varying(35),
    cod_e_sogg_vers_nazione_versante character varying(2),
    de_e_sogg_vers_email_versante character varying(256),
    cod_e_sogg_pag_id_univ_pag_tipo_id_univoco character(1),
    cod_e_sogg_pag_id_univ_pag_codice_id_univoco character varying(35),
    cod_e_sogg_pag_anagrafica_pagatore character varying(70),
    de_e_sogg_pag_indirizzo_pagatore character varying(70),
    de_e_sogg_pag_civico_pagatore character varying(16),
    cod_e_sogg_pag_cap_pagatore character varying(16),
    de_e_sogg_pag_localita_pagatore character varying(35),
    de_e_sogg_pag_provincia_pagatore character varying(35),
    cod_e_sogg_pag_nazione_pagatore character varying(2),
    de_e_sogg_pag_email_pagatore character varying(256),
    cod_e_dati_pag_codice_esito_pagamento character(1),
    num_e_dati_pag_importo_totale_pagato numeric(12,2),
    cod_e_dati_pag_id_univoco_versamento character varying(35),
    cod_e_dati_pag_codice_contesto_pagamento character varying(35),
    id_session character varying(36),
    id_session_fesp character varying(36),
    tipo_carrello character varying(32),
    modello_pagamento integer,
    ente_sil_invia_risposta_pagamento_url character varying(2048),
    flg_notifica_esito boolean DEFAULT false,
    mygov_carrello_multi_beneficiario_id bigint,
    cod_rp_silinviarp_original_fault_code character varying(256),
    de_rp_silinviarp_original_fault_string character varying(256),
    de_rp_silinviarp_original_fault_description character varying(1024),
    cod_e_silinviaesito_original_fault_code character varying(256),
    de_e_silinviaesito_original_fault_string character varying(256),
    de_e_silinviaesito_original_fault_description character varying(1024),
    CONSTRAINT mygov_carrello_mygov_carrello_multi_beneficiario_id_is_not_null CHECK ((NOT (((tipo_carrello)::text = 'ESTERNO_ANONIMO_MULTIENTE'::text) AND (mygov_carrello_multi_beneficiario_id IS NULL))))
);


ALTER TABLE public.mygov_carrello OWNER TO mypay4;

--
-- TOC entry 208 (class 1259 OID 20921)
-- Name: mygov_carrello_multi_beneficiario; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_carrello_multi_beneficiario (
    mygov_carrello_multi_beneficiario_id bigint NOT NULL,
    version integer NOT NULL,
    mygov_anagrafica_stato_id bigint NOT NULL,
    cod_ipa_ente character varying(80) NOT NULL,
    cod_ack_carrello_rp character(2),
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone,
    id_session_carrello character varying(36),
    id_session_carrellofesp character varying(36),
    risposta_pagamento_url character varying(2048),
    de_rp_silinviacarrellorp_esito character varying(256),
    cod_rp_silinviacarrellorp_url character varying(256),
    cod_rp_silinviacarrellorp_fault_code character varying(256),
    de_rp_silinviacarrellorp_fault_string character varying(256),
    cod_rp_silinviacarrellorp_id character varying(256),
    de_rp_silinviacarrellorp_description character varying(1024),
    cod_rp_silinviacarrellorp_serial integer,
    cod_rp_silinviacarrellorp_original_fault_code character varying(256),
    de_rp_silinviacarrellorp_original_fault_string character varying(256),
    de_rp_silinviacarrellorp_original_fault_description character varying(1024)
);


ALTER TABLE public.mygov_carrello_multi_beneficiario OWNER TO mypay4;

--
-- TOC entry 209 (class 1259 OID 20927)
-- Name: mygov_carrello_multi_beneficiario_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_carrello_multi_beneficiario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_carrello_multi_beneficiario_id_seq OWNER TO mypay4;

--
-- TOC entry 210 (class 1259 OID 20929)
-- Name: mygov_carrello_mygov_carrello_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_carrello_mygov_carrello_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_carrello_mygov_carrello_id_seq OWNER TO mypay4;

--
-- TOC entry 277 (class 1259 OID 74610)
-- Name: mygov_carrello_queue; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_carrello_queue (
    mygov_carrello_id bigint NOT NULL,
    mygov_id_job bigint NOT NULL,
    mygov_id_stato character varying(35),
    dt_creazione timestamp without time zone NOT NULL,
    dt_start timestamp without time zone,
    dt_end timestamp without time zone
);


ALTER TABLE public.mygov_carrello_queue OWNER TO mypay4;

--
-- TOC entry 211 (class 1259 OID 20931)
-- Name: mygov_comune; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_comune (
    comune_id bigint NOT NULL,
    comune character varying(128) NOT NULL,
    provincia_id bigint NOT NULL,
    sigla_provincia character varying(2) NOT NULL,
    cod_belfiore character varying(5) NOT NULL,
    codice_istat character varying(6),
    var_cod_belfiore character varying(5),
    var_provincia character varying(2),
    var_comune character varying(128)
);


ALTER TABLE public.mygov_comune OWNER TO mypay4;

--
-- TOC entry 212 (class 1259 OID 20934)
-- Name: mygov_dati_marca_bollo_digitale; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_dati_marca_bollo_digitale (
    mygov_dati_marca_bollo_digitale_id bigint NOT NULL,
    version integer NOT NULL,
    tipo_bollo character varying(2) NOT NULL,
    hash_documento character varying(70) NOT NULL,
    provincia_residenza character varying(2) NOT NULL
);


ALTER TABLE public.mygov_dati_marca_bollo_digitale OWNER TO mypay4;

--
-- TOC entry 213 (class 1259 OID 20937)
-- Name: mygov_dati_marca_bollo_digitale_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_dati_marca_bollo_digitale_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_dati_marca_bollo_digitale_id_seq OWNER TO mypay4;

--
-- TOC entry 214 (class 1259 OID 20939)
-- Name: mygov_delega; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_delega (
    mygov_delega_id bigint NOT NULL,
    mygov_ente_id bigint NOT NULL,
    dt_valida_da date NOT NULL,
    dt_valida_a date NOT NULL,
    flg_sospesa boolean NOT NULL,
    mygov_soggetto_delegato_id bigint NOT NULL,
    mygov_soggetto_delegante_id bigint NOT NULL
);


ALTER TABLE public.mygov_delega OWNER TO mypay4;

--
-- TOC entry 215 (class 1259 OID 20942)
-- Name: mygov_delega_mygov_delega_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_delega_mygov_delega_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_delega_mygov_delega_id_seq OWNER TO mypay4;

--
-- TOC entry 216 (class 1259 OID 20944)
-- Name: mygov_dovuto; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_dovuto (
    mygov_dovuto_id bigint NOT NULL,
    version integer NOT NULL,
    flg_dovuto_attuale boolean NOT NULL,
    mygov_flusso_id bigint NOT NULL,
    num_riga_flusso numeric(12,0) NOT NULL,
    mygov_anagrafica_stato_id bigint NOT NULL,
    mygov_carrello_id bigint,
    cod_iud character varying(35) NOT NULL,
    cod_iuv character varying(35),
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL,
    cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character(1) NOT NULL,
    cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying(35) NOT NULL,
    de_rp_sogg_pag_anagrafica_pagatore character varying(70) NOT NULL,
    de_rp_sogg_pag_indirizzo_pagatore character varying(70),
    de_rp_sogg_pag_civico_pagatore character varying(16),
    cod_rp_sogg_pag_cap_pagatore character varying(16),
    de_rp_sogg_pag_localita_pagatore character varying(35),
    de_rp_sogg_pag_provincia_pagatore character varying(2),
    cod_rp_sogg_pag_nazione_pagatore character varying(2),
    de_rp_sogg_pag_email_pagatore character varying(256),
    dt_rp_dati_vers_data_esecuzione_pagamento date,
    cod_rp_dati_vers_tipo_versamento character varying(32) NOT NULL,
    num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric(12,2) NOT NULL,
    num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric(12,2),
    de_rp_dati_vers_dati_sing_vers_causale_versamento character varying(1024) NOT NULL,
    de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying(140) NOT NULL,
    cod_tipo_dovuto character varying(64) NOT NULL,
    mygov_avviso_id bigint,
    dt_creazione_cod_iuv timestamp without time zone,
    mygov_dati_marca_bollo_digitale_id bigint,
    de_causale_visualizzata character varying(1024),
    bilancio character varying(4096),
    flg_genera_iuv boolean,
    id_session character varying(36)
);


ALTER TABLE public.mygov_dovuto OWNER TO mypay4;

--
-- TOC entry 217 (class 1259 OID 20950)
-- Name: mygov_dovuto_carrello; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_dovuto_carrello (
    mygov_dovuto_carrello_id bigint NOT NULL,
    mygov_dovuto_id bigint,
    mygov_carrello_id bigint,
    num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric(12,2),
    cod_rp_dati_vers_dati_sing_vers_iban_accredito character varying(35),
    cod_rp_dati_vers_dati_sing_vers_bic_accredito character varying(11),
    cod_rp_dati_vers_dati_sing_vers_iban_appoggio character varying(35),
    cod_rp_dati_vers_dati_sing_vers_bic_appoggio character varying(11),
    cod_rp_dati_vers_dati_sing_vers_credenziali_pagatore character varying(35),
    version integer NOT NULL,
    de_rp_dati_vers_dati_sing_vers_causale_versamento_agid character varying(140)
);


ALTER TABLE public.mygov_dovuto_carrello OWNER TO mypay4;

--
-- TOC entry 218 (class 1259 OID 20953)
-- Name: mygov_dovuto_carrello_mygov_dovuto_carrello_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_dovuto_carrello_mygov_dovuto_carrello_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_dovuto_carrello_mygov_dovuto_carrello_id_seq OWNER TO mypay4;

--
-- TOC entry 219 (class 1259 OID 20955)
-- Name: mygov_dovuto_elaborato; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_dovuto_elaborato (
    mygov_dovuto_elaborato_id bigint NOT NULL,
    version integer NOT NULL,
    flg_dovuto_attuale boolean NOT NULL,
    mygov_flusso_id bigint NOT NULL,
    num_riga_flusso numeric(12,0) NOT NULL,
    mygov_anagrafica_stato_id bigint NOT NULL,
    mygov_carrello_id bigint,
    cod_iud character varying(35) NOT NULL,
    cod_iuv character varying(35),
    cod_ack_rp character(2),
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica_rp timestamp without time zone,
    dt_ultima_modifica_e timestamp without time zone,
    cod_rp_silinviarp_id_psp character varying(35),
    cod_rp_silinviarp_id_intermediario_psp character varying(35),
    cod_rp_silinviarp_id_canale character varying(35),
    cod_rp_silinviarp_id_dominio character varying(35),
    cod_rp_silinviarp_id_univoco_versamento character varying(35),
    cod_rp_silinviarp_codice_contesto_pagamento character varying(35),
    de_rp_silinviarp_esito character varying(256),
    cod_rp_silinviarp_redirect integer,
    cod_rp_silinviarp_url character varying(256),
    cod_rp_silinviarp_fault_code character varying(256),
    de_rp_silinviarp_fault_string character varying(256),
    cod_rp_silinviarp_id character varying(256),
    de_rp_silinviarp_description character varying(1024),
    cod_rp_silinviarp_serial integer,
    de_rp_versione_oggetto character varying(16) NOT NULL,
    cod_rp_dom_id_dominio character(35),
    cod_rp_dom_id_stazione_richiedente character varying(35),
    cod_rp_id_messaggio_richiesta character varying(35),
    dt_rp_data_ora_messaggio_richiesta timestamp without time zone,
    cod_rp_autenticazione_soggetto character varying(4),
    cod_rp_sogg_vers_id_univ_vers_tipo_id_univoco character(1),
    cod_rp_sogg_vers_id_univ_vers_codice_id_univoco character varying(35),
    cod_rp_sogg_vers_anagrafica_versante character varying(70),
    de_rp_sogg_vers_indirizzo_versante character varying(70),
    de_rp_sogg_vers_civico_versante character varying(16),
    cod_rp_sogg_vers_cap_versante character varying(16),
    de_rp_sogg_vers_localita_versante character varying(35),
    de_rp_sogg_vers_provincia_versante character varying(35),
    cod_rp_sogg_vers_nazione_versante character varying(2),
    de_rp_sogg_vers_email_versante character varying(256),
    cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character(1) NOT NULL,
    cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying(35) NOT NULL,
    de_rp_sogg_pag_anagrafica_pagatore character varying(70) NOT NULL,
    de_rp_sogg_pag_indirizzo_pagatore character varying(70),
    de_rp_sogg_pag_civico_pagatore character varying(16),
    cod_rp_sogg_pag_cap_pagatore character varying(16),
    de_rp_sogg_pag_localita_pagatore character varying(35),
    de_rp_sogg_pag_provincia_pagatore character varying(35),
    cod_rp_sogg_pag_nazione_pagatore character varying(2),
    de_rp_sogg_pag_email_pagatore character varying(256),
    dt_rp_dati_vers_data_esecuzione_pagamento date,
    num_rp_dati_vers_importo_totale_da_versare numeric(12,2),
    cod_rp_dati_vers_tipo_versamento character varying(32),
    cod_rp_dati_vers_id_univoco_versamento character varying(35),
    cod_rp_dati_vers_codice_contesto_pagamento character varying(35),
    de_rp_dati_vers_iban_addebito character varying(35),
    de_rp_dati_vers_bic_addebito character varying(11),
    num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric(12,2) NOT NULL,
    num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric(12,2),
    cod_rp_dati_vers_dati_sing_vers_iban_accredito character varying(35),
    cod_rp_dati_vers_dati_sing_vers_bic_accredito character varying(11),
    cod_rp_dati_vers_dati_sing_vers_iban_appoggio character varying(35),
    cod_rp_dati_vers_dati_sing_vers_bic_appoggio character varying(11),
    cod_rp_dati_vers_dati_sing_vers_credenziali_pagatore character varying(35),
    de_rp_dati_vers_dati_sing_vers_causale_versamento character varying(1024) NOT NULL,
    de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying(140) NOT NULL,
    cod_e_silinviaesito_id_dominio character varying(35),
    cod_e_silinviaesito_id_univoco_versamento character varying(35),
    cod_e_silinviaesito_codice_contesto_pagamento character varying(35),
    de_e_silinviaesito_esito character varying(256),
    cod_e_silinviaesito_fault_code character varying(256),
    de_e_silinviaesito_fault_string character varying(256),
    cod_e_silinviaesito_id character varying(256),
    de_e_silinviaesito_description character varying(1024),
    cod_e_silinviaesito_serial integer,
    de_e_versione_oggetto character varying(16),
    cod_e_dom_id_dominio character(35),
    cod_e_dom_id_stazione_richiedente character varying(35),
    cod_e_id_messaggio_ricevuta character varying(35),
    cod_e_data_ora_messaggio_ricevuta timestamp without time zone,
    cod_e_riferimento_messaggio_richiesta character varying(35),
    cod_e_riferimento_data_richiesta date,
    cod_e_istit_att_id_univ_att_tipo_id_univoco character(1),
    cod_e_istit_att_id_univ_att_codice_id_univoco character varying(35),
    de_e_istit_att_denominazione_attestante character varying(70),
    cod_e_istit_att_codice_unit_oper_attestante character varying(35),
    de_e_istit_att_denom_unit_oper_attestante character varying(70),
    de_e_istit_att_indirizzo_attestante character varying(70),
    de_e_istit_att_civico_attestante character varying(16),
    cod_e_istit_att_cap_attestante character varying(16),
    de_e_istit_att_localita_attestante character varying(35),
    de_e_istit_att_provincia_attestante character varying(35),
    cod_e_istit_att_nazione_attestante character varying(2),
    cod_e_ente_benef_id_univ_benef_tipo_id_univoco character(1),
    cod_e_ente_benef_id_univ_benef_codice_id_univoco character varying(35),
    de_e_ente_benef_denominazione_beneficiario character varying(70),
    cod_e_ente_benef_codice_unit_oper_beneficiario character varying(35),
    de_e_ente_benef_denom_unit_oper_beneficiario character varying(70),
    de_e_ente_benef_indirizzo_beneficiario character varying(70),
    de_e_ente_benef_civico_beneficiario character varying(16),
    cod_e_ente_benef_cap_beneficiario character varying(16),
    de_e_ente_benef_localita_beneficiario character varying(35),
    de_e_ente_benef_provincia_beneficiario character varying(35),
    cod_e_ente_benef_nazione_beneficiario character varying(2),
    cod_e_sogg_vers_id_univ_vers_tipo_id_univoco character(1),
    cod_e_sogg_vers_id_univ_vers_codice_id_univoco character varying(35),
    cod_e_sogg_vers_anagrafica_versante character varying(70),
    de_e_sogg_vers_indirizzo_versante character varying(70),
    de_e_sogg_vers_civico_versante character varying(16),
    cod_e_sogg_vers_cap_versante character varying(16),
    de_e_sogg_vers_localita_versante character varying(35),
    de_e_sogg_vers_provincia_versante character varying(35),
    cod_e_sogg_vers_nazione_versante character varying(2),
    de_e_sogg_vers_email_versante character varying(256),
    cod_e_sogg_pag_id_univ_pag_tipo_id_univoco character(1),
    cod_e_sogg_pag_id_univ_pag_codice_id_univoco character varying(35),
    cod_e_sogg_pag_anagrafica_pagatore character varying(70),
    de_e_sogg_pag_indirizzo_pagatore character varying(70),
    de_e_sogg_pag_civico_pagatore character varying(16),
    cod_e_sogg_pag_cap_pagatore character varying(16),
    de_e_sogg_pag_localita_pagatore character varying(35),
    de_e_sogg_pag_provincia_pagatore character varying(35),
    cod_e_sogg_pag_nazione_pagatore character varying(2),
    de_e_sogg_pag_email_pagatore character varying(256),
    cod_e_dati_pag_codice_esito_pagamento character(1),
    num_e_dati_pag_importo_totale_pagato numeric(12,2),
    cod_e_dati_pag_id_univoco_versamento character varying(35),
    cod_e_dati_pag_codice_contesto_pagamento character varying(35),
    num_e_dati_pag_dati_sing_pag_singolo_importo_pagato numeric(12,2),
    de_e_dati_pag_dati_sing_pag_esito_singolo_pagamento character varying(35),
    dt_e_dati_pag_dati_sing_pag_data_esito_singolo_pagamento date,
    cod_e_dati_pag_dati_sing_pag_id_univoco_riscoss character varying(35),
    de_e_dati_pag_dati_sing_pag_causale_versamento character varying(140),
    de_e_dati_pag_dati_sing_pag_dati_specifici_riscossione character varying(140),
    cod_tipo_dovuto character varying(64),
    dt_ultimo_cambio_stato timestamp without time zone NOT NULL,
    modello_pagamento integer,
    ente_sil_invia_risposta_pagamento_url character varying(2048),
    de_rt_inviart_tipo_firma character varying(15),
    blb_rt_payload bytea,
    indice_dati_singolo_pagamento integer,
    num_e_dati_pag_dati_sing_pag_commissioni_applicate_psp numeric(12,2),
    cod_e_dati_pag_dati_sing_pag_allegato_ricevuta_tipo character varying(2),
    blb_e_dati_pag_dati_sing_pag_allegato_ricevuta_test bytea,
    cod_rp_dati_vers_dati_sing_vers_dati_mbd_tipo_bollo character(2),
    cod_rp_dati_vers_dati_sing_vers_dati_mbd_hash_documento character(70),
    cod_rp_dati_vers_dati_sing_vers_dati_mbd_provincia_residenza character(2),
    de_rp_dati_vers_dati_sing_vers_causale_versamento_agid character varying(140),
    bilancio character varying(4096),
    cod_rp_silinviarp_original_fault_code character varying(256),
    de_rp_silinviarp_original_fault_string character varying(256),
    de_rp_silinviarp_original_fault_description character varying(1024),
    cod_e_silinviaesito_original_fault_code character varying(256),
    de_e_silinviaesito_original_fault_string character varying(256),
    de_e_silinviaesito_original_fault_description character varying(1024)
);


ALTER TABLE public.mygov_dovuto_elaborato OWNER TO mypay4;

--
-- TOC entry 220 (class 1259 OID 20961)
-- Name: mygov_dovuto_elaborato_mygov_dovuto_elaborato_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_dovuto_elaborato_mygov_dovuto_elaborato_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_dovuto_elaborato_mygov_dovuto_elaborato_id_seq OWNER TO mypay4;

--
-- TOC entry 221 (class 1259 OID 20963)
-- Name: mygov_dovuto_mygov_dovuto_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_dovuto_mygov_dovuto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_dovuto_mygov_dovuto_id_seq OWNER TO mypay4;

--
-- TOC entry 222 (class 1259 OID 20965)
-- Name: mygov_dovuto_rifiutato_mygov_dovuto_rifiutato_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_dovuto_rifiutato_mygov_dovuto_rifiutato_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_dovuto_rifiutato_mygov_dovuto_rifiutato_id_seq OWNER TO mypay4;

--
-- TOC entry 223 (class 1259 OID 20967)
-- Name: mygov_ente; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_ente (
    mygov_ente_id bigint NOT NULL,
    cod_ipa_ente character varying(80) NOT NULL,
    codice_fiscale_ente character varying(11) NOT NULL,
    de_nome_ente character varying(100) NOT NULL,
    email_amministratore character varying(50),
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL,
    cod_rp_dati_vers_tipo_versamento character varying(15) DEFAULT 'ALL'::character varying NOT NULL,
    num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric(12,2),
    cod_rp_dati_vers_dati_sing_vers_iban_accredito character varying(35),
    cod_rp_dati_vers_dati_sing_vers_bic_accredito character varying(11),
    cod_rp_dati_vers_dati_sing_vers_iban_appoggio character varying(35),
    cod_rp_dati_vers_dati_sing_vers_bic_appoggio character varying(11),
    mybox_client_key character varying(256) NOT NULL,
    mybox_client_secret character varying(256) NOT NULL,
    ente_sil_invia_risposta_pagamento_url character varying(2048),
    cod_global_location_number character varying(13),
    de_password character varying(15),
    cod_rp_dati_vers_dati_sing_vers_bic_accredito_seller boolean NOT NULL,
    de_rp_ente_benef_denominazione_beneficiario character varying(70),
    de_rp_ente_benef_indirizzo_beneficiario character varying(70),
    de_rp_ente_benef_civico_beneficiario character varying(16),
    cod_rp_ente_benef_cap_beneficiario character varying(16),
    de_rp_ente_benef_localita_beneficiario character varying(35),
    de_rp_ente_benef_provincia_beneficiario character varying(35),
    cod_rp_ente_benef_nazione_beneficiario character varying(2),
    de_rp_ente_benef_telefono_beneficiario character varying(70),
    de_rp_ente_benef_sito_web_beneficiario character varying(256),
    de_rp_ente_benef_email_beneficiario character varying(256),
    application_code character varying(2) NOT NULL,
    cod_codice_interbancario_cbill character varying(5),
    de_informazioni_ente text,
    de_logo_ente text,
    de_autorizzazione character varying(50),
    cd_stato_ente bigint NOT NULL,
    de_url_esterni_attiva text,
    lingua_aggiuntiva character varying(2),
    cod_tipo_ente character varying(35),
    dt_avvio date
);


ALTER TABLE public.mygov_ente OWNER TO mypay4;

--
-- TOC entry 224 (class 1259 OID 20973)
-- Name: mygov_ente_funzionalita; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_ente_funzionalita (
    mygov_ente_funzionalita_id bigint NOT NULL,
    cod_ipa_ente character varying(80) NOT NULL,
    cod_funzionalita character varying(80) NOT NULL,
    flg_attivo boolean NOT NULL
);


ALTER TABLE public.mygov_ente_funzionalita OWNER TO mypay4;

--
-- TOC entry 225 (class 1259 OID 20976)
-- Name: mygov_ente_funzionalita_mygov_ente_funzionalita_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_ente_funzionalita_mygov_ente_funzionalita_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_ente_funzionalita_mygov_ente_funzionalita_id_seq OWNER TO mypay4;

--
-- TOC entry 226 (class 1259 OID 20978)
-- Name: mygov_ente_mygov_ente_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_ente_mygov_ente_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_ente_mygov_ente_id_seq OWNER TO mypay4;

--
-- TOC entry 227 (class 1259 OID 20980)
-- Name: mygov_ente_sil; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_ente_sil (
    mygov_ente_sil_id bigint NOT NULL,
    mygov_ente_id bigint NOT NULL,
    nome_applicativo character varying(256),
    de_url_inoltro_esito_pagamento_push character varying(256),
    cod_service_account_jwt_entrata_client_id character varying(256),
    de_service_account_jwt_entrata_client_mail character varying(256),
    cod_service_account_jwt_entrata_secret_key_id character varying(256),
    cod_service_account_jwt_entrata_secret_key character varying(256),
    cod_service_account_jwt_uscita_client_id character varying(256),
    de_service_account_jwt_uscita_client_mail character varying(256),
    cod_service_account_jwt_uscita_secret_key_id character varying(256),
    cod_service_account_jwt_uscita_secret_key character varying(256),
    flg_jwt_attivo boolean DEFAULT false,
    mygov_ente_tipo_dovuto_id bigint
);


ALTER TABLE public.mygov_ente_sil OWNER TO mypay4;

--
-- TOC entry 228 (class 1259 OID 20987)
-- Name: mygov_ente_sil_mygov_ente_sil_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_ente_sil_mygov_ente_sil_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_ente_sil_mygov_ente_sil_id_seq OWNER TO mypay4;

--
-- TOC entry 229 (class 1259 OID 20989)
-- Name: mygov_ente_tipo_dovuto; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_ente_tipo_dovuto (
    mygov_ente_tipo_dovuto_id bigint NOT NULL,
    mygov_ente_id bigint NOT NULL,
    cod_tipo character varying(64),
    de_tipo character varying(256),
    iban_accredito_pi character varying(35),
    bic_accredito_pi character varying(11),
    iban_appoggio_pi character varying(35),
    bic_appoggio_pi character varying(11),
    iban_accredito_psp character varying(35),
    bic_accredito_psp character varying(11),
    iban_appoggio_psp character varying(35),
    bic_appoggio_psp character varying(11),
    cod_conto_corrente_postale character varying(18),
    cod_xsd_causale character varying(64) NOT NULL,
    bic_accredito_pi_seller boolean NOT NULL,
    bic_accredito_psp_seller boolean NOT NULL,
    spontaneo boolean NOT NULL,
    importo numeric(12,2),
    de_url_pagamento_dovuto character varying(256),
    de_bilancio_default text,
    flg_cf_anonimo boolean DEFAULT false NOT NULL,
    flg_scadenza_obbligatoria boolean DEFAULT true NOT NULL,
    flg_stampa_data_scadenza boolean DEFAULT true NOT NULL,
    de_intestatario_cc_postale character varying(50),
    de_settore_ente character varying(50),
    flg_notifica_io boolean DEFAULT false NOT NULL,
    flg_notifica_esito_push boolean DEFAULT false NOT NULL,
    max_tentativi_inoltro_esito integer DEFAULT 20,
    mygov_ente_sil_id bigint,
    flg_attivo boolean DEFAULT true NOT NULL,
    codice_contesto_pagamento character varying,
    flg_disabilita_stampa_avviso boolean DEFAULT false NOT NULL,
    cod_tassonomico character varying(35),
    macro_area character varying(4),
    tipo_servizio character varying(35),
    motivo_riscossione character varying(1024),
    CONSTRAINT ck_flg_scadenza_obbligatoria CHECK ((((NOT flg_scadenza_obbligatoria) AND spontaneo) OR (NOT spontaneo))),
    CONSTRAINT ck_flg_stampa_data_scadenza CHECK ((((NOT flg_stampa_data_scadenza) AND spontaneo) OR (NOT spontaneo)))
);


ALTER TABLE public.mygov_ente_tipo_dovuto OWNER TO mypay4;

--
-- TOC entry 230 (class 1259 OID 21005)
-- Name: mygov_ente_tipo_dovuto_mygov_ente_tipo_dovuto_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_ente_tipo_dovuto_mygov_ente_tipo_dovuto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_ente_tipo_dovuto_mygov_ente_tipo_dovuto_id_seq OWNER TO mypay4;

--
-- TOC entry 231 (class 1259 OID 21007)
-- Name: mygov_ente_tipo_progressivo; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_ente_tipo_progressivo (
    cod_ipa_ente character varying(80) NOT NULL,
    tipo_generatore character varying(80) NOT NULL,
    data_riferimento date NOT NULL,
    num_progressivo bigint NOT NULL
);


ALTER TABLE public.mygov_ente_tipo_progressivo OWNER TO mypay4;

--
-- TOC entry 232 (class 1259 OID 21010)
-- Name: mygov_esito_avviso_digitale; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_esito_avviso_digitale (
    mygov_esito_avviso_digitale_id bigint NOT NULL,
    version integer NOT NULL,
    mygov_avviso_digitale_id bigint NOT NULL,
    num_e_ad_esito_av_tipo_canale_esito integer NOT NULL,
    cod_e_ad_esito_av_id_canale_esito character varying(35),
    dt_e_ad_esito_av_data_esito date NOT NULL,
    num_e_ad_esito_av_codice_esito numeric(5,0) NOT NULL,
    de_e_ad_esito_av_desc_esito character varying(140)
);


ALTER TABLE public.mygov_esito_avviso_digitale OWNER TO mypay4;

--
-- TOC entry 233 (class 1259 OID 21013)
-- Name: mygov_esito_avviso_digitale_mygov_esito_avviso_digitale_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_esito_avviso_digitale_mygov_esito_avviso_digitale_id_seq
    START WITH 220195
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_esito_avviso_digitale_mygov_esito_avviso_digitale_id_seq OWNER TO mypay4;

--
-- TOC entry 234 (class 1259 OID 21015)
-- Name: mygov_export_dovuti; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_export_dovuti (
    mygov_export_dovuti_id bigint NOT NULL,
    version integer NOT NULL,
    mygov_ente_id bigint NOT NULL,
    mygov_anagrafica_stato_id bigint NOT NULL,
    de_nome_file_generato character varying(256),
    num_dimensione_file_generato bigint,
    dt_inizio_estrazione timestamp(0) without time zone NOT NULL,
    dt_fine_estrazione timestamp(0) without time zone NOT NULL,
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL,
    cod_tipo_dovuto character varying(64),
    cod_request_token text NOT NULL,
    mygov_utente_id bigint NOT NULL,
    flg_ricevuta boolean NOT NULL,
    flg_incrementale boolean NOT NULL,
    versione_tracciato character varying(35) NOT NULL
);


ALTER TABLE public.mygov_export_dovuti OWNER TO mypay4;

--
-- TOC entry 235 (class 1259 OID 21021)
-- Name: mygov_export_dovuti_mygov_export_dovuti_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_export_dovuti_mygov_export_dovuti_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_export_dovuti_mygov_export_dovuti_id_seq OWNER TO mypay4;

--
-- TOC entry 236 (class 1259 OID 21023)
-- Name: mygov_flusso; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_flusso (
    mygov_flusso_id bigint NOT NULL,
    version integer NOT NULL,
    mygov_ente_id bigint NOT NULL,
    mygov_anagrafica_stato_id bigint NOT NULL,
    iuf character varying(50) NOT NULL,
    num_righe_totali numeric(10,0),
    num_righe_importate_correttamente numeric(10,0),
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL,
    flg_attivo boolean NOT NULL,
    de_nome_operatore character varying(50),
    flg_spontaneo boolean DEFAULT false,
    de_percorso_file character varying(256),
    de_nome_file character varying(256),
    pdf_generati numeric(10,0),
    cod_request_token text,
    cod_errore character varying(256)
);


ALTER TABLE public.mygov_flusso OWNER TO mypay4;

--
-- TOC entry 237 (class 1259 OID 21030)
-- Name: mygov_flusso_avviso_digitale; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_flusso_avviso_digitale (
    mygov_flusso_avviso_digitale_id bigint NOT NULL,
    version integer NOT NULL,
    cod_fad_id_dominio character varying(35) NOT NULL,
    cod_fad_id_flusso character varying(70) NOT NULL,
    mygov_anagrafica_stato_id bigint NOT NULL,
    cod_fad_tipo_flusso character varying(20) NOT NULL,
    cod_fad_e_presa_in_carico_id_flusso character varying(70),
    num_fad_e_presa_in_carico_cod_e_presa_in_carico numeric(1,0),
    de_fad_e_presa_in_carico_desc_e_presa_in_carico character varying(140),
    de_fad_file_path character varying(512),
    de_fad_filename character varying(70),
    num_fad_dimensione_file bigint,
    num_fad_num_avvisi_nel_flusso numeric,
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL
);


ALTER TABLE public.mygov_flusso_avviso_digitale OWNER TO mypay4;

--
-- TOC entry 238 (class 1259 OID 21036)
-- Name: mygov_flusso_avviso_digitale_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_flusso_avviso_digitale_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_flusso_avviso_digitale_id_seq OWNER TO mypay4;

--
-- TOC entry 274 (class 1259 OID 74580)
-- Name: mygov_flusso_massivo; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_flusso_massivo (
    mygov_flusso_massivo_id bigint NOT NULL,
    version integer NOT NULL,
    mygov_anagrafica_stato_id bigint NOT NULL,
    iufm character varying(256) NOT NULL,
    tipo_flusso_massivo character varying(50) NOT NULL,
    num_operazioni_totali numeric(10,0),
    num_operazioni_elaborate_correttamente numeric(10,0),
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL,
    de_nome_operatore character varying(50),
    de_percorso_file character varying(256),
    de_nome_file character varying(256),
    cod_request_token text,
    cod_errore character varying(256)
);


ALTER TABLE public.mygov_flusso_massivo OWNER TO mypay4;

--
-- TOC entry 239 (class 1259 OID 21038)
-- Name: mygov_flusso_mygov_flusso_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_flusso_mygov_flusso_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_flusso_mygov_flusso_id_seq OWNER TO mypay4;

--
-- TOC entry 280 (class 1259 OID 202034)
-- Name: mygov_flusso_mygov_flusso_massivo_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_flusso_mygov_flusso_massivo_id_seq
    START WITH 300
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_flusso_mygov_flusso_massivo_id_seq OWNER TO mypay4;

--
-- TOC entry 273 (class 1259 OID 74563)
-- Name: mygov_flusso_tassonomia; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_flusso_tassonomia (
    mygov_flusso_tassonomia_id bigint NOT NULL,
    version integer NOT NULL,
    mygov_anagrafica_stato_id bigint NOT NULL,
    iuft character varying(256) NOT NULL,
    num_righe_totali numeric(10,0),
    num_righe_elaborate_correttamente numeric(10,0),
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL,
    de_nome_operatore character varying(50),
    de_percorso_file character varying(256),
    de_nome_file character varying(256),
    cod_request_token text,
    cod_errore character varying(256)
);


ALTER TABLE public.mygov_flusso_tassonomia OWNER TO mypay4;

--
-- TOC entry 278 (class 1259 OID 202014)
-- Name: mygov_flusso_tassonomia_mygov_flusso_tassonomia_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_flusso_tassonomia_mygov_flusso_tassonomia_id_seq
    START WITH 300
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_flusso_tassonomia_mygov_flusso_tassonomia_id_seq OWNER TO mypay4;

--
-- TOC entry 240 (class 1259 OID 21040)
-- Name: mygov_giornale; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_giornale (
    mygov_giornale_id bigint NOT NULL,
    version integer NOT NULL,
    data_ora_evento timestamp without time zone NOT NULL,
    identificativo_dominio character varying(35) NOT NULL,
    identificativo_univoco_versamento character varying(35) NOT NULL,
    codice_contesto_pagamento character varying(35) NOT NULL,
    identificativo_prestatore_servizi_pagamento character varying(35) NOT NULL,
    tipo_versamento character varying(35),
    componente character varying(35) NOT NULL,
    categoria_evento character varying(35) NOT NULL,
    tipo_evento character varying(35) NOT NULL,
    sotto_tipo_evento character varying(35) NOT NULL,
    identificativo_fruitore character varying(50) NOT NULL,
    identificativo_erogatore character varying(35) NOT NULL,
    identificativo_stazione_intermediario_pa character varying(50),
    canale_pagamento character varying(35),
    parametri_specifici_interfaccia character varying(16384),
    esito character varying(35)
);


ALTER TABLE public.mygov_giornale OWNER TO mypay4;

--
-- TOC entry 241 (class 1259 OID 21046)
-- Name: mygov_giornale_mygov_giornale_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_giornale_mygov_giornale_id_seq
    START WITH 13282
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_giornale_mygov_giornale_id_seq OWNER TO mypay4;

--
-- TOC entry 242 (class 1259 OID 21048)
-- Name: mygov_identificativo_univoco; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_identificativo_univoco (
    mygov_identificativo_univoco_id bigint NOT NULL,
    version integer NOT NULL,
    mygov_ente_id bigint NOT NULL,
    mygov_flusso_id bigint NOT NULL,
    cod_tipo_identificativo character varying(15) NOT NULL,
    identificativo character varying(35) NOT NULL,
    dt_inserimento timestamp without time zone NOT NULL
);


ALTER TABLE public.mygov_identificativo_univoco OWNER TO mypay4;

--
-- TOC entry 243 (class 1259 OID 21051)
-- Name: mygov_identificativo_univoco_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_identificativo_univoco_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_identificativo_univoco_id_seq OWNER TO mypay4;

--
-- TOC entry 244 (class 1259 OID 21053)
-- Name: mygov_import_dovuti; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_import_dovuti (
    mygov_import_dovuti_id bigint NOT NULL,
    version integer NOT NULL,
    mygov_ente_id bigint NOT NULL,
    mygov_utente_id bigint NOT NULL,
    mygov_anagrafica_stato_id bigint NOT NULL,
    cod_request_token text NOT NULL,
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL,
    de_nome_file_scarti character varying(256),
    cod_errore character varying(256)
);


ALTER TABLE public.mygov_import_dovuti OWNER TO mypay4;

--
-- TOC entry 245 (class 1259 OID 21059)
-- Name: mygov_import_dovuti_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_import_dovuti_id_seq
    START WITH 17
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_import_dovuti_id_seq OWNER TO mypay4;

--
-- TOC entry 246 (class 1259 OID 21061)
-- Name: mygov_nazione; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_nazione (
    nazione_id bigint NOT NULL,
    nome_nazione character varying(128) NOT NULL,
    codice_iso_alpha_2 character varying(2) NOT NULL
);


ALTER TABLE public.mygov_nazione OWNER TO mypay4;

--
-- TOC entry 247 (class 1259 OID 21064)
-- Name: mygov_operatore; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_operatore (
    mygov_operatore_id bigint NOT NULL,
    ruolo character varying(64),
    cod_fed_user_id character varying(128) NOT NULL,
    cod_ipa_ente character varying(80) NOT NULL,
    de_email_address character varying(256)
);


ALTER TABLE public.mygov_operatore OWNER TO mypay4;

--
-- TOC entry 248 (class 1259 OID 21067)
-- Name: mygov_operatore_ente_tipo_dovuto; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_operatore_ente_tipo_dovuto (
    mygov_operatore_ente_tipo_dovuto_id bigint NOT NULL,
    mygov_operatore_id bigint NOT NULL,
    mygov_ente_tipo_dovuto_id bigint,
    flg_attivo boolean NOT NULL
);


ALTER TABLE public.mygov_operatore_ente_tipo_dovuto OWNER TO mypay4;

--
-- TOC entry 249 (class 1259 OID 21070)
-- Name: mygov_operatore_mygov_operatore_ente_tipo_dovuto_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_operatore_mygov_operatore_ente_tipo_dovuto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_operatore_mygov_operatore_ente_tipo_dovuto_id_seq OWNER TO mypay4;

--
-- TOC entry 250 (class 1259 OID 21072)
-- Name: mygov_operatore_mygov_operatore_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_operatore_mygov_operatore_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_operatore_mygov_operatore_id_seq OWNER TO mypay4;

--
-- TOC entry 251 (class 1259 OID 21074)
-- Name: mygov_precaricato_anonimo_ente; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_precaricato_anonimo_ente (
    mygov_precaricato_anonimo_ente_id bigint NOT NULL,
    cod_ipa_ente character varying(80) NOT NULL,
    cod_iud character varying(35) NOT NULL,
    id_session character varying(36) NOT NULL,
    mygov_carrello_id bigint,
    dt_creazione timestamp without time zone,
    ente_sil_invia_risposta_pagamento_url character varying(2048),
    version integer NOT NULL
);


ALTER TABLE public.mygov_precaricato_anonimo_ente OWNER TO mypay4;

--
-- TOC entry 252 (class 1259 OID 21080)
-- Name: mygov_precaricato_anonimo_ente_mygov_precaricato_anonimo_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_precaricato_anonimo_ente_mygov_precaricato_anonimo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_precaricato_anonimo_ente_mygov_precaricato_anonimo_id_seq OWNER TO mypay4;

--
-- TOC entry 253 (class 1259 OID 21082)
-- Name: mygov_provincia; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_provincia (
    provincia_id bigint NOT NULL,
    provincia character varying(64) NOT NULL,
    sigla character(2) NOT NULL
);


ALTER TABLE public.mygov_provincia OWNER TO mypay4;

--
-- TOC entry 254 (class 1259 OID 21085)
-- Name: mygov_push_esito_sil; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_push_esito_sil (
    mygov_push_esito_sil_id bigint NOT NULL,
    mygov_dovuto_elaborato_id bigint NOT NULL,
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultimo_tentativo timestamp without time zone,
    num_tentativi_effettuati integer,
    flg_esito_invio_push boolean NOT NULL,
    cod_esito_invio_fault_code character varying(256),
    de_esito_invio_fault_description character varying(256)
);


ALTER TABLE public.mygov_push_esito_sil OWNER TO mypay4;

--
-- TOC entry 255 (class 1259 OID 21091)
-- Name: mygov_push_esito_sil_mygov_push_esito_sil_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_push_esito_sil_mygov_push_esito_sil_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_push_esito_sil_mygov_push_esito_sil_id_seq OWNER TO mypay4;

--
-- TOC entry 281 (class 1259 OID 202037)
-- Name: mygov_registro_operazione; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_registro_operazione (
    mygov_registro_operazione_id bigint NOT NULL,
    dt_operazione timestamp(0) without time zone DEFAULT now() NOT NULL,
    cod_fed_user_id_operatore character varying(128) NOT NULL,
    cod_tipo_operazione character varying(20) NOT NULL,
    de_oggetto_operazione character varying(256) NOT NULL,
    cod_stato_bool boolean NOT NULL
);


ALTER TABLE public.mygov_registro_operazione OWNER TO mypay4;

--
-- TOC entry 282 (class 1259 OID 202043)
-- Name: mygov_registro_operazione_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_registro_operazione_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_registro_operazione_id_seq OWNER TO mypay4;

--
-- TOC entry 256 (class 1259 OID 21093)
-- Name: mygov_revoca; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_revoca (
    mygov_revoca_id bigint NOT NULL,
    version integer NOT NULL,
    mygov_anagrafica_stato_id bigint NOT NULL,
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL,
    cod_id_dominio character varying(35) NOT NULL,
    de_rr_versione_oggetto character varying(16) NOT NULL,
    cod_id_univoco_versamento character varying(35) NOT NULL,
    cod_codice_contesto_pagamento character varying(35) NOT NULL,
    cod_rr_dom_id_dominio character varying(35) NOT NULL,
    cod_rr_dom_id_stazione_richiedente character varying(35),
    cod_rr_id_messaggio_revoca character varying(35) NOT NULL,
    dt_rr_data_ora_messaggio_revoca timestamp without time zone NOT NULL,
    de_rr_istit_att_denominazione_mittente character varying(70) NOT NULL,
    cod_rr_istit_att_unit_oper_mittente character varying(35),
    de_rr_istit_att_denom_unit_oper_mittente character varying(70),
    de_rr_istit_att_indirizzo_mittente character varying(70),
    de_rr_istit_att_civico_mittente character varying(16),
    cod_rr_istit_att_cap_mittente character varying(16),
    de_rr_istit_att_localita_mittente character varying(35),
    de_rr_istit_att_provincia_mittente character varying(35),
    cod_rr_istit_att_nazione_mittente character varying(2),
    cod_rr_istit_att_id_univ_mitt_tipo_id_univoco character varying(1) NOT NULL,
    cod_rr_istit_att_id_univ_mitt_codice_id_univoco character varying(35) NOT NULL,
    cod_rr_sogg_vers_id_univ_vers_tipo_id_univoco character varying(1),
    cod_rr_sogg_vers_id_univ_vers_codice_id_univoco character varying(35),
    cod_rr_sogg_vers_anagrafica_versante character varying(70),
    de_rr_sogg_vers_indirizzo_versante character varying(70),
    de_rr_sogg_vers_civico_versante character varying(16),
    cod_rr_sogg_vers_cap_versante character varying(16),
    de_rr_sogg_vers_localita_versante character varying(35),
    de_rr_sogg_vers_provincia_versante character varying(35),
    cod_rr_sogg_vers_nazione_versante character varying(2),
    de_rr_sogg_vers_email_versante character varying(256),
    cod_rr_sogg_pag_id_univ_pag_tipo_id_univoco character varying(1) NOT NULL,
    cod_rr_sogg_pag_id_univ_pag_codice_id_univoco character varying(35) NOT NULL,
    cod_rr_sogg_pag_anagrafica_pagatore character varying(70) NOT NULL,
    de_rr_sogg_pag_indirizzo_pagatore character varying(70),
    de_rr_sogg_pag_civico_pagatore character varying(16),
    cod_rr_sogg_pag_cap_pagatore character varying(16),
    de_rr_sogg_pag_localita_pagatore character varying(35),
    de_rr_sogg_pag_provincia_pagatore character varying(35),
    cod_rr_sogg_pag_nazione_pagatore character varying(2),
    de_rr_sogg_pag_email_pagatore character varying(256),
    num_rr_dati_rev_importo_totale_revocato numeric(12,2) NOT NULL,
    cod_rr_dati_rev_id_univoco_versamento character varying(35) NOT NULL,
    cod_rr_dati_rev_codice_contesto_pagamento character varying(35) NOT NULL,
    cod_rr_dati_rev_tipo_revoca character varying(1),
    de_er_versione_oggetto character varying(16),
    cod_er_dom_id_dominio character varying(35),
    cod_er_dom_id_stazione_richiedente character varying(35),
    cod_er_id_messaggio_esito character varying(35),
    dt_er_data_ora_messaggio_esito timestamp without time zone,
    cod_er_riferimento_messaggio_revoca character varying(35),
    dt_er_riferimento_data_revoca timestamp without time zone,
    de_er_istit_att_denominazione_mittente character varying(70),
    cod_er_istit_att_unit_oper_mittente character varying(35),
    de_er_istit_att_denom_unit_oper_mittente character varying(70),
    de_er_istit_att_indirizzo_mittente character varying(70),
    de_er_istit_att_civico_mittente character varying(16),
    cod_er_istit_att_cap_mittente character varying(16),
    de_er_istit_att_localita_mittente character varying(35),
    de_er_istit_att_provincia_mittente character varying(35),
    cod_er_istit_att_nazione_mittente character varying(2),
    cod_er_istit_att_id_univ_mitt_tipo_id_univoco character varying(1),
    cod_er_istit_att_id_univ_mitt_codice_id_univoco character varying(35),
    cod_er_sogg_vers_id_univ_vers_tipo_id_univoco character varying(1),
    cod_er_sogg_vers_id_univ_vers_codice_id_univoco character varying(35),
    cod_er_sogg_vers_anagrafica_versante character varying(70),
    de_er_sogg_vers_indirizzo_versante character varying(70),
    de_er_sogg_vers_civico_versante character varying(16),
    cod_er_sogg_vers_cap_versante character varying(16),
    de_er_sogg_vers_localita_versante character varying(35),
    de_er_sogg_vers_provincia_versante character varying(35),
    cod_er_sogg_vers_nazione_versante character varying(2),
    de_er_sogg_vers_email_versante character varying(256),
    cod_er_sogg_pag_id_univ_pag_tipo_id_univoco character varying(1),
    cod_er_sogg_pag_id_univ_pag_codice_id_univoco character varying(35),
    cod_er_sogg_pag_anagrafica_pagatore character varying(70),
    de_er_sogg_pag_indirizzo_pagatore character varying(70),
    de_er_sogg_pag_civico_pagatore character varying(16),
    cod_er_sogg_pag_cap_pagatore character varying(16),
    de_er_sogg_pag_localita_pagatore character varying(35),
    de_er_sogg_pag_provincia_pagatore character varying(35),
    cod_er_sogg_pag_nazione_pagatore character varying(2),
    de_er_sogg_pag_email_pagatore character varying(256),
    num_er_dati_rev_importo_totale_revocato numeric(12,2),
    cod_er_dati_rev_id_univoco_versamento character varying(35),
    cod_er_dati_rev_codice_contesto_pagamento character varying(35)
);


ALTER TABLE public.mygov_revoca OWNER TO mypay4;

--
-- TOC entry 257 (class 1259 OID 21099)
-- Name: mygov_revoca_dati_pagamenti; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_revoca_dati_pagamenti (
    mygov_revoca_dati_pagamenti_id bigint NOT NULL,
    version integer NOT NULL,
    mygov_revoca_id bigint NOT NULL,
    num_rr_dati_sing_rev_singolo_importo_revocato numeric(12,2) NOT NULL,
    cod_rr_dati_sing_rev_id_univoco_riscossione character varying(35) NOT NULL,
    de_rr_dati_sing_rev_causale_revoca character varying(140) NOT NULL,
    de_rr_dati_sing_rev_dati_aggiuntivi_revoca character varying(140) NOT NULL,
    num_er_dati_sing_rev_singolo_importo_revocato numeric(12,2),
    cod_er_dati_sing_rev_id_univoco_riscossione character varying(35),
    de_er_dati_sing_rev_causale_revoca character varying(140),
    de_er_dati_sing_rev_dati_aggiuntivi_revoca character varying(140),
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL
);


ALTER TABLE public.mygov_revoca_dati_pagamenti OWNER TO mypay4;

--
-- TOC entry 258 (class 1259 OID 21105)
-- Name: mygov_revoca_dati_pagamenti_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_revoca_dati_pagamenti_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_revoca_dati_pagamenti_id_seq OWNER TO mypay4;

--
-- TOC entry 259 (class 1259 OID 21107)
-- Name: mygov_revoca_mygov_revoca_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_revoca_mygov_revoca_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_revoca_mygov_revoca_id_seq OWNER TO mypay4;

--
-- TOC entry 260 (class 1259 OID 21109)
-- Name: mygov_soggetto_delega; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_soggetto_delega (
    mygov_soggetto_delega_id bigint NOT NULL,
    cod_rp_sogg_id_univ_tipo_id_univoco character varying(1) NOT NULL,
    cod_rp_sogg_id_univ_codice_id_univoco character varying(35) NOT NULL,
    de_rp_sogg_anagrafica character varying(70) NOT NULL,
    de_rp_sogg_indirizzo character varying(70) NOT NULL,
    de_rp_sogg_civico character varying(16) NOT NULL,
    cod_rp_sogg_cap character varying(16) NOT NULL,
    de_rp_sogg_localita character varying(35) NOT NULL,
    de_rp_sogg_provincia character varying(2) NOT NULL,
    cod_rp_sogg_nazione character varying(2) NOT NULL,
    de_rp_sogg_email character varying(256) NOT NULL
);


ALTER TABLE public.mygov_soggetto_delega OWNER TO mypay4;

--
-- TOC entry 261 (class 1259 OID 21115)
-- Name: mygov_soggetto_delega_mygov_soggetto_delega_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_soggetto_delega_mygov_soggetto_delega_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_soggetto_delega_mygov_soggetto_delega_id_seq OWNER TO mypay4;

--
-- TOC entry 262 (class 1259 OID 21117)
-- Name: mygov_soggetto_delegante_coordinate_addebito; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_soggetto_delegante_coordinate_addebito (
    mygov_soggetto_delegante_coordinate_addebito_id bigint NOT NULL,
    iban_addebito character varying(35) NOT NULL,
    bic_addebito character varying(11),
    mygov_delega_mygov_delega_id bigint NOT NULL
);


ALTER TABLE public.mygov_soggetto_delegante_coordinate_addebito OWNER TO mypay4;

--
-- TOC entry 263 (class 1259 OID 21120)
-- Name: mygov_soggetto_delegante_coordinate_addebito_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_soggetto_delegante_coordinate_addebito_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_soggetto_delegante_coordinate_addebito_id_seq OWNER TO mypay4;

--
-- TOC entry 264 (class 1259 OID 21122)
-- Name: mygov_spontaneo_anonimo; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_spontaneo_anonimo (
    mygov_spontaneo_anonimo_id bigint NOT NULL,
    cod_ipa_ente character varying(80) NOT NULL,
    cod_tipo_dovuto character varying(64) NOT NULL,
    de_rp_dati_vers_dati_sing_vers_causale_versamento character varying(1024) NOT NULL,
    num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric(12,2) NOT NULL,
    id_session character varying(36) NOT NULL,
    de_email_address character varying(256) NOT NULL,
    email_verificata boolean DEFAULT false NOT NULL,
    mygov_carrello_id bigint,
    dt_creazione timestamp without time zone,
    version integer NOT NULL,
    de_causale_visualizzata character varying(1024),
    de_bilancio text
);


ALTER TABLE public.mygov_spontaneo_anonimo OWNER TO mypay4;

--
-- TOC entry 265 (class 1259 OID 21129)
-- Name: mygov_spontaneo_anonimo_mygov_spontaneo_anonimo_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_spontaneo_anonimo_mygov_spontaneo_anonimo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_spontaneo_anonimo_mygov_spontaneo_anonimo_id_seq OWNER TO mypay4;

--
-- TOC entry 284 (class 1259 OID 364153)
-- Name: mygov_standard_tipo_dovuto; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_standard_tipo_dovuto (
    mygov_standard_tipo_dovuto_id bigint NOT NULL,
    cod_tipo character varying(64) NOT NULL,
    de_tipo character varying(256) NOT NULL,
    cod_xsd_causale character varying(64) DEFAULT 'mypay_default'::character varying NOT NULL,
    macro_area character varying(4),
    tipo_servizio character varying(35),
    motivo_riscossione character varying(1024),
    cod_tassonomico character varying(35)
);


ALTER TABLE public.mygov_standard_tipo_dovuto OWNER TO mypay4;

--
-- TOC entry 275 (class 1259 OID 74597)
-- Name: mygov_tassonomia; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_tassonomia (
    mygov_tassonomia_id bigint NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    tipo_ente character varying(35) NOT NULL,
    descrizione_tipo_ente character varying(2048) NOT NULL,
    prog_macro_area character varying(2) NOT NULL,
    nome_macro_area character varying(512) NOT NULL,
    desc_macro_area character varying(2048) NOT NULL,
    cod_tipo_servizio character varying(35) NOT NULL,
    tipo_servizio character varying(512) NOT NULL,
    descrizione_tipo_servizio character varying(2048) NOT NULL,
    motivo_riscossione character varying(35) NOT NULL,
    dt_inizio_validita timestamp without time zone NOT NULL,
    dt_fine_validita timestamp without time zone NOT NULL,
    codice_tassonomico character varying(35) NOT NULL,
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL,
    version_my_pay integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.mygov_tassonomia OWNER TO mypay4;

--
-- TOC entry 279 (class 1259 OID 202016)
-- Name: mygov_tassonomia_mygov_tassonomia_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_tassonomia_mygov_tassonomia_id_seq
    START WITH 300
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_tassonomia_mygov_tassonomia_id_seq OWNER TO mypay4;

--
-- TOC entry 266 (class 1259 OID 21131)
-- Name: mygov_utente; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_utente (
    mygov_utente_id bigint NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    cod_fed_user_id character varying(128) NOT NULL,
    cod_codice_fiscale_utente character varying(16) NOT NULL,
    flg_fed_authorized boolean DEFAULT false NOT NULL,
    de_email_address character varying(256),
    de_firstname character varying(64),
    de_lastname character varying(64),
    de_fed_legal_entity character varying(16) DEFAULT 'fisica'::character varying NOT NULL,
    dt_ultimo_login timestamp without time zone,
    indirizzo character varying(70),
    civico character varying(16),
    cap character varying(16),
    comune_id bigint,
    provincia_id bigint,
    nazione_id bigint,
    dt_set_address timestamp without time zone,
    email_source_type character(1) DEFAULT 'A'::bpchar NOT NULL,
    de_email_address_new character varying(256)
);


ALTER TABLE public.mygov_utente OWNER TO mypay4;

--
-- TOC entry 267 (class 1259 OID 21140)
-- Name: mygov_utente_mygov_utente_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_utente_mygov_utente_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_utente_mygov_utente_id_seq OWNER TO mypay4;

--
-- TOC entry 283 (class 1259 OID 364108)
-- Name: mygov_validazione_email; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_validazione_email (
    codice character varying(10) NOT NULL,
    dt_ultimo_tentativo timestamp without time zone,
    dt_primo_invio timestamp without time zone NOT NULL,
    dt_ultimo_invio timestamp without time zone NOT NULL,
    num_invii integer NOT NULL,
    num_tentativi integer NOT NULL,
    mygov_utente_id bigint NOT NULL
);


ALTER TABLE public.mygov_validazione_email OWNER TO mypay4;

--
-- TOC entry 268 (class 1259 OID 21142)
-- Name: mygov_wisp; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.mygov_wisp (
    mygov_wisp_id bigint NOT NULL,
    version integer NOT NULL,
    dt_creazione timestamp without time zone,
    dt_ultima_modifica timestamp without time zone,
    de_tipo_carrello character varying(36) NOT NULL,
    cod_id_session character varying(36) NOT NULL,
    cod_req_id_dominio character varying(35) NOT NULL,
    de_req_ente_creditore character varying(140) NOT NULL,
    cod_req_key_pa character varying(40) NOT NULL,
    de_req_primitiva character varying(30) NOT NULL,
    num_req_num_pagamenti_rpt integer NOT NULL,
    de_req_storno_pagamento character varying(2) NOT NULL,
    de_req_bollo_digitale character varying(2) NOT NULL,
    de_req_terzo_modello_pagamento character varying(2) NOT NULL,
    cod_req_id_psp character varying(35),
    de_req_tipo_versamento character varying(4),
    num_req_importo_transazione numeric(10,2),
    de_req_versione_interfaccia_wisp character varying(3),
    cod_req_iban_accredito character varying(27),
    de_req_conto_poste character varying(2),
    de_req_pagamenti_modello_2 character varying(2),
    de_rsp_errore_type character varying(128),
    cod_rsp_ok_key_wisp character varying(40),
    de_scelta_effettuazione_scelta character varying(2),
    cod_scelta_identificativo_psp character varying(35),
    cod_scelta_identificativo_intermediario_psp character varying(35),
    cod_scelta_identificativo_canale character varying(35),
    de_scelta_tipo_versamento character varying(32),
    cod_fault_bean_id character varying(256),
    de_fault_bean_fault_code character varying(256),
    de_fault_bean_fault_string character varying(256),
    de_fault_bean_description character varying(1024),
    cod_req_codice_lingua character varying(2),
    cod_fault_bean_original_fault_code character varying(256),
    de_fault_bean_original_fault_string character varying(256),
    de_fault_bean_original_fault_description character varying(1024)
);


ALTER TABLE public.mygov_wisp OWNER TO mypay4;

--
-- TOC entry 269 (class 1259 OID 21148)
-- Name: mygov_wisp_id_seq; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.mygov_wisp_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mygov_wisp_id_seq OWNER TO mypay4;

--
-- TOC entry 276 (class 1259 OID 74608)
-- Name: sq_job_id; Type: SEQUENCE; Schema: public; Owner: mypay4
--

CREATE SEQUENCE public.sq_job_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.sq_job_id OWNER TO mypay4;

--
-- TOC entry 271 (class 1259 OID 32808)
-- Name: test; Type: TABLE; Schema: public; Owner: mypay4
--

CREATE TABLE public.test (
    campo1 character varying,
    campo2 character varying
);


ALTER TABLE public.test OWNER TO mypay4;

--
-- TOC entry 286 (class 1259 OID 391848)
-- Name: test_lock; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.test_lock (
    id numeric NOT NULL,
    valore text
);


ALTER TABLE public.test_lock OWNER TO postgres;

--
-- TOC entry 285 (class 1259 OID 391447)
-- Name: test_pagamenti; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.test_pagamenti (
    nr_pagamenti numeric,
    giorno timestamp with time zone,
    ora text
);


ALTER TABLE public.test_pagamenti OWNER TO postgres;

--
-- TOC entry 2586 (class 2606 OID 21638)
-- Name: mygov_provincia mygo_provincia_primary_key; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_provincia
    ADD CONSTRAINT mygo_provincia_primary_key PRIMARY KEY (provincia_id);


--
-- TOC entry 2435 (class 2606 OID 21640)
-- Name: mygov_anagrafica_stato mygov_anagrafica_stato_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_anagrafica_stato
    ADD CONSTRAINT mygov_anagrafica_stato_pkey PRIMARY KEY (mygov_anagrafica_stato_id);


--
-- TOC entry 2442 (class 2606 OID 21642)
-- Name: mygov_avviso_anonimo mygov_avviso_anonimo_id; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_avviso_anonimo
    ADD CONSTRAINT mygov_avviso_anonimo_id PRIMARY KEY (mygov_avviso_anonimo_id);


--
-- TOC entry 2448 (class 2606 OID 21644)
-- Name: mygov_avviso_digitale mygov_avviso_digitale_cod_ad_id_messaggio_richiesta_key; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_avviso_digitale
    ADD CONSTRAINT mygov_avviso_digitale_cod_ad_id_messaggio_richiesta_key UNIQUE (cod_ad_id_dominio, cod_ad_id_messaggio_richiesta);


--
-- TOC entry 2450 (class 2606 OID 21646)
-- Name: mygov_avviso_digitale mygov_avviso_digitale_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_avviso_digitale
    ADD CONSTRAINT mygov_avviso_digitale_pkey PRIMARY KEY (mygov_avviso_digitale_id);


--
-- TOC entry 2439 (class 2606 OID 21648)
-- Name: mygov_avviso mygov_avviso_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_avviso
    ADD CONSTRAINT mygov_avviso_pkey PRIMARY KEY (mygov_avviso_id);


--
-- TOC entry 2453 (class 2606 OID 21650)
-- Name: mygov_avviso_tassa_auto mygov_avviso_tassa_auto_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_avviso_tassa_auto
    ADD CONSTRAINT mygov_avviso_tassa_auto_pkey PRIMARY KEY (mygov_avviso_tassa_auto_id);


--
-- TOC entry 2470 (class 2606 OID 21652)
-- Name: mygov_carrello_multi_beneficiario mygov_carrello_multi_beneficiario_primary_key; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_carrello_multi_beneficiario
    ADD CONSTRAINT mygov_carrello_multi_beneficiario_primary_key PRIMARY KEY (mygov_carrello_multi_beneficiario_id);


--
-- TOC entry 2467 (class 2606 OID 21654)
-- Name: mygov_carrello mygov_carrello_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_carrello
    ADD CONSTRAINT mygov_carrello_pkey PRIMARY KEY (mygov_carrello_id);


--
-- TOC entry 2627 (class 2606 OID 74614)
-- Name: mygov_carrello_queue mygov_carrello_queue_id_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_carrello_queue
    ADD CONSTRAINT mygov_carrello_queue_id_pkey PRIMARY KEY (mygov_carrello_id, mygov_id_job);


--
-- TOC entry 2474 (class 2606 OID 21656)
-- Name: mygov_comune mygov_comune_primary_key; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_comune
    ADD CONSTRAINT mygov_comune_primary_key PRIMARY KEY (comune_id);


--
-- TOC entry 2476 (class 2606 OID 21658)
-- Name: mygov_dati_marca_bollo_digitale mygov_dati_marca_bollo_digitale_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_dati_marca_bollo_digitale
    ADD CONSTRAINT mygov_dati_marca_bollo_digitale_pkey PRIMARY KEY (mygov_dati_marca_bollo_digitale_id);


--
-- TOC entry 2481 (class 2606 OID 21660)
-- Name: mygov_delega mygov_delega_pk; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_delega
    ADD CONSTRAINT mygov_delega_pk PRIMARY KEY (mygov_delega_id);


--
-- TOC entry 2499 (class 2606 OID 21662)
-- Name: mygov_dovuto_carrello mygov_dovuto_carrello_pk; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_dovuto_carrello
    ADD CONSTRAINT mygov_dovuto_carrello_pk PRIMARY KEY (mygov_dovuto_carrello_id);


--
-- TOC entry 2513 (class 2606 OID 21664)
-- Name: mygov_dovuto_elaborato mygov_dovuto_elaborato_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_dovuto_elaborato
    ADD CONSTRAINT mygov_dovuto_elaborato_pkey PRIMARY KEY (mygov_dovuto_elaborato_id);


--
-- TOC entry 2495 (class 2606 OID 21666)
-- Name: mygov_dovuto mygov_dovuto_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_dovuto
    ADD CONSTRAINT mygov_dovuto_pkey PRIMARY KEY (mygov_dovuto_id);


--
-- TOC entry 2524 (class 2606 OID 21668)
-- Name: mygov_ente_funzionalita mygov_ente_funzionalita_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_ente_funzionalita
    ADD CONSTRAINT mygov_ente_funzionalita_pkey PRIMARY KEY (mygov_ente_funzionalita_id);


--
-- TOC entry 2518 (class 2606 OID 21670)
-- Name: mygov_ente mygov_ente_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_ente
    ADD CONSTRAINT mygov_ente_pkey PRIMARY KEY (mygov_ente_id);


--
-- TOC entry 2526 (class 2606 OID 21672)
-- Name: mygov_ente_sil mygov_ente_sil_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_ente_sil
    ADD CONSTRAINT mygov_ente_sil_pkey PRIMARY KEY (mygov_ente_sil_id);


--
-- TOC entry 2529 (class 2606 OID 21674)
-- Name: mygov_ente_tipo_dovuto mygov_ente_tipo_dovuto_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_ente_tipo_dovuto
    ADD CONSTRAINT mygov_ente_tipo_dovuto_pkey PRIMARY KEY (mygov_ente_tipo_dovuto_id);


--
-- TOC entry 2520 (class 2606 OID 21676)
-- Name: mygov_ente mygov_ente_ukey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_ente
    ADD CONSTRAINT mygov_ente_ukey UNIQUE (cod_ipa_ente);


--
-- TOC entry 2522 (class 2606 OID 202003)
-- Name: mygov_ente mygov_ente_ukey_cf; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_ente
    ADD CONSTRAINT mygov_ente_ukey_cf UNIQUE (codice_fiscale_ente);


--
-- TOC entry 2535 (class 2606 OID 21678)
-- Name: mygov_esito_avviso_digitale mygov_esito_avviso_digitale_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_esito_avviso_digitale
    ADD CONSTRAINT mygov_esito_avviso_digitale_pkey PRIMARY KEY (mygov_esito_avviso_digitale_id);


--
-- TOC entry 2540 (class 2606 OID 21680)
-- Name: mygov_export_dovuti mygov_export_dovuti_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_export_dovuti
    ADD CONSTRAINT mygov_export_dovuti_pkey PRIMARY KEY (mygov_export_dovuti_id);


--
-- TOC entry 2552 (class 2606 OID 21682)
-- Name: mygov_flusso_avviso_digitale mygov_flusso_avviso_digitale_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_flusso_avviso_digitale
    ADD CONSTRAINT mygov_flusso_avviso_digitale_pkey PRIMARY KEY (mygov_flusso_avviso_digitale_id);


--
-- TOC entry 2622 (class 2606 OID 74587)
-- Name: mygov_flusso_massivo mygov_flusso_massivo_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_flusso_massivo
    ADD CONSTRAINT mygov_flusso_massivo_pkey PRIMARY KEY (mygov_flusso_massivo_id);


--
-- TOC entry 2548 (class 2606 OID 21684)
-- Name: mygov_flusso mygov_flusso_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_flusso
    ADD CONSTRAINT mygov_flusso_pkey PRIMARY KEY (mygov_flusso_id);


--
-- TOC entry 2619 (class 2606 OID 74570)
-- Name: mygov_flusso_tassonomia mygov_flusso_tassonomia_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_flusso_tassonomia
    ADD CONSTRAINT mygov_flusso_tassonomia_pkey PRIMARY KEY (mygov_flusso_tassonomia_id);


--
-- TOC entry 2556 (class 2606 OID 21686)
-- Name: mygov_giornale mygov_giornale_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_giornale
    ADD CONSTRAINT mygov_giornale_pkey PRIMARY KEY (mygov_giornale_id);


--
-- TOC entry 2561 (class 2606 OID 21688)
-- Name: mygov_identificativo_univoco mygov_identificativo_univoco_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_identificativo_univoco
    ADD CONSTRAINT mygov_identificativo_univoco_pkey PRIMARY KEY (mygov_identificativo_univoco_id);


--
-- TOC entry 2567 (class 2606 OID 21690)
-- Name: mygov_import_dovuti mygov_import_dovuti_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_import_dovuti
    ADD CONSTRAINT mygov_import_dovuti_pkey PRIMARY KEY (mygov_import_dovuti_id);


--
-- TOC entry 2569 (class 2606 OID 21692)
-- Name: mygov_nazione mygov_nazione_primary_key; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_nazione
    ADD CONSTRAINT mygov_nazione_primary_key PRIMARY KEY (nazione_id);


--
-- TOC entry 2579 (class 2606 OID 21694)
-- Name: mygov_operatore_ente_tipo_dovuto mygov_operatore_ente_tipo_dovuto_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_operatore_ente_tipo_dovuto
    ADD CONSTRAINT mygov_operatore_ente_tipo_dovuto_pkey PRIMARY KEY (mygov_operatore_ente_tipo_dovuto_id);


--
-- TOC entry 2573 (class 2606 OID 21696)
-- Name: mygov_operatore mygov_operatore_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_operatore
    ADD CONSTRAINT mygov_operatore_pkey PRIMARY KEY (mygov_operatore_id);


--
-- TOC entry 2575 (class 2606 OID 21698)
-- Name: mygov_operatore mygov_operatore_ukey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_operatore
    ADD CONSTRAINT mygov_operatore_ukey UNIQUE (cod_fed_user_id, cod_ipa_ente);


--
-- TOC entry 2584 (class 2606 OID 21700)
-- Name: mygov_precaricato_anonimo_ente mygov_precaricato_ente_anonimo_id; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_precaricato_anonimo_ente
    ADD CONSTRAINT mygov_precaricato_ente_anonimo_id PRIMARY KEY (mygov_precaricato_anonimo_ente_id);


--
-- TOC entry 2589 (class 2606 OID 21702)
-- Name: mygov_push_esito_sil mygov_push_esito_sil_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_push_esito_sil
    ADD CONSTRAINT mygov_push_esito_sil_pkey PRIMARY KEY (mygov_push_esito_sil_id);


--
-- TOC entry 2629 (class 2606 OID 202042)
-- Name: mygov_registro_operazione mygov_registro_operazioni_pk; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_registro_operazione
    ADD CONSTRAINT mygov_registro_operazioni_pk PRIMARY KEY (mygov_registro_operazione_id);


--
-- TOC entry 2595 (class 2606 OID 21704)
-- Name: mygov_revoca_dati_pagamenti mygov_revoca_dati_pagamenti_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_revoca_dati_pagamenti
    ADD CONSTRAINT mygov_revoca_dati_pagamenti_pkey PRIMARY KEY (mygov_revoca_dati_pagamenti_id);


--
-- TOC entry 2592 (class 2606 OID 21706)
-- Name: mygov_revoca mygov_revoca_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_revoca
    ADD CONSTRAINT mygov_revoca_pkey PRIMARY KEY (mygov_revoca_id);


--
-- TOC entry 2598 (class 2606 OID 21708)
-- Name: mygov_soggetto_delega mygov_soggetto_delega_pk; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_soggetto_delega
    ADD CONSTRAINT mygov_soggetto_delega_pk PRIMARY KEY (mygov_soggetto_delega_id);


--
-- TOC entry 2601 (class 2606 OID 21710)
-- Name: mygov_soggetto_delegante_coordinate_addebito mygov_soggetto_delegante_coordinate_addebito_pk; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_soggetto_delegante_coordinate_addebito
    ADD CONSTRAINT mygov_soggetto_delegante_coordinate_addebito_pk PRIMARY KEY (mygov_soggetto_delegante_coordinate_addebito_id);


--
-- TOC entry 2604 (class 2606 OID 21712)
-- Name: mygov_spontaneo_anonimo mygov_spontaneo_anonimo_id; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_spontaneo_anonimo
    ADD CONSTRAINT mygov_spontaneo_anonimo_id PRIMARY KEY (mygov_spontaneo_anonimo_id);


--
-- TOC entry 2633 (class 2606 OID 364161)
-- Name: mygov_standard_tipo_dovuto mygov_standard_tipo_dovuto_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_standard_tipo_dovuto
    ADD CONSTRAINT mygov_standard_tipo_dovuto_pkey PRIMARY KEY (mygov_standard_tipo_dovuto_id);


--
-- TOC entry 2624 (class 2606 OID 74604)
-- Name: mygov_tassonomia mygov_tassonomia_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_tassonomia
    ADD CONSTRAINT mygov_tassonomia_pkey PRIMARY KEY (mygov_tassonomia_id);


--
-- TOC entry 2609 (class 2606 OID 21714)
-- Name: mygov_utente mygov_utente_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_utente
    ADD CONSTRAINT mygov_utente_pkey PRIMARY KEY (mygov_utente_id);


--
-- TOC entry 2611 (class 2606 OID 21716)
-- Name: mygov_utente mygov_utente_ukey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_utente
    ADD CONSTRAINT mygov_utente_ukey UNIQUE (cod_fed_user_id);


--
-- TOC entry 2631 (class 2606 OID 364135)
-- Name: mygov_validazione_email mygov_validazione_email_pk; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_validazione_email
    ADD CONSTRAINT mygov_validazione_email_pk PRIMARY KEY (mygov_utente_id);


--
-- TOC entry 2613 (class 2606 OID 21718)
-- Name: mygov_wisp mygov_wisp_cod_req_key_pa_key; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_wisp
    ADD CONSTRAINT mygov_wisp_cod_req_key_pa_key UNIQUE (cod_req_key_pa);


--
-- TOC entry 2615 (class 2606 OID 21720)
-- Name: mygov_wisp mygov_wisp_pkey; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_wisp
    ADD CONSTRAINT mygov_wisp_pkey PRIMARY KEY (mygov_wisp_id);


--
-- TOC entry 2532 (class 2606 OID 21722)
-- Name: mygov_ente_tipo_progressivo pk_mygov_ente_tipo_progressivo; Type: CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_ente_tipo_progressivo
    ADD CONSTRAINT pk_mygov_ente_tipo_progressivo PRIMARY KEY (cod_ipa_ente, tipo_generatore);


--
-- TOC entry 2635 (class 2606 OID 391855)
-- Name: test_lock test_lock_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_lock
    ADD CONSTRAINT test_lock_pkey PRIMARY KEY (id);


--
-- TOC entry 2436 (class 1259 OID 21723)
-- Name: fki_avviso_ente_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_avviso_ente_id_idx ON public.mygov_avviso USING btree (mygov_ente_id);


--
-- TOC entry 2451 (class 1259 OID 21724)
-- Name: fki_avviso_tassa_auto_mygov_dovuto_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_avviso_tassa_auto_mygov_dovuto_id_idx ON public.mygov_avviso_tassa_auto USING btree (mygov_dovuto_id);


--
-- TOC entry 2593 (class 1259 OID 21725)
-- Name: fki_c; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_c ON public.mygov_revoca_dati_pagamenti USING btree (mygov_revoca_id);


--
-- TOC entry 2454 (class 1259 OID 21726)
-- Name: fki_carrello_anagrafica_stato_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_carrello_anagrafica_stato_id_idx ON public.mygov_carrello USING btree (mygov_anagrafica_stato_id);


--
-- TOC entry 2468 (class 1259 OID 21727)
-- Name: fki_carrello_multi_beneficiario_anagrafica_stato_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_carrello_multi_beneficiario_anagrafica_stato_id_idx ON public.mygov_carrello_multi_beneficiario USING btree (mygov_anagrafica_stato_id);


--
-- TOC entry 2455 (class 1259 OID 21728)
-- Name: fki_carrello_multi_beneficiario_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_carrello_multi_beneficiario_id_idx ON public.mygov_carrello USING btree (mygov_carrello_multi_beneficiario_id);


--
-- TOC entry 2477 (class 1259 OID 21729)
-- Name: fki_delega_ente_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_delega_ente_id_idx ON public.mygov_delega USING btree (mygov_ente_id);


--
-- TOC entry 2478 (class 1259 OID 21730)
-- Name: fki_delega_soggetto_delegante_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_delega_soggetto_delegante_id_idx ON public.mygov_delega USING btree (mygov_soggetto_delegante_id);


--
-- TOC entry 2479 (class 1259 OID 21731)
-- Name: fki_delega_soggetto_delegato_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_delega_soggetto_delegato_id_idx ON public.mygov_delega USING btree (mygov_soggetto_delegato_id);


--
-- TOC entry 2482 (class 1259 OID 21732)
-- Name: fki_dovuto_anagrafica_stato_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_dovuto_anagrafica_stato_id_idx ON public.mygov_dovuto USING btree (mygov_anagrafica_stato_id);


--
-- TOC entry 2483 (class 1259 OID 21733)
-- Name: fki_dovuto_avviso_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_dovuto_avviso_id_idx ON public.mygov_dovuto USING btree (mygov_avviso_id);


--
-- TOC entry 2496 (class 1259 OID 21734)
-- Name: fki_dovuto_carrello_carrello_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_dovuto_carrello_carrello_id_idx ON public.mygov_dovuto_carrello USING btree (mygov_carrello_id);


--
-- TOC entry 2497 (class 1259 OID 21735)
-- Name: fki_dovuto_carrello_dovuto_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_dovuto_carrello_dovuto_id_idx ON public.mygov_dovuto_carrello USING btree (mygov_dovuto_id);


--
-- TOC entry 2484 (class 1259 OID 21736)
-- Name: fki_dovuto_carrello_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_dovuto_carrello_id_idx ON public.mygov_dovuto USING btree (mygov_carrello_id);


--
-- TOC entry 2485 (class 1259 OID 21737)
-- Name: fki_dovuto_dati_marca_bollo_digitale_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_dovuto_dati_marca_bollo_digitale_id_idx ON public.mygov_dovuto USING btree (mygov_dati_marca_bollo_digitale_id);


--
-- TOC entry 2500 (class 1259 OID 21738)
-- Name: fki_dovuto_elaborato_carrello_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_dovuto_elaborato_carrello_id_idx ON public.mygov_dovuto_elaborato USING btree (mygov_carrello_id);


--
-- TOC entry 2456 (class 1259 OID 21739)
-- Name: fki_dtcreazione_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_dtcreazione_idx ON public.mygov_carrello USING btree (dt_creazione);


--
-- TOC entry 2514 (class 1259 OID 21740)
-- Name: fki_ente_cd_stato_ente_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_ente_cd_stato_ente_idx ON public.mygov_ente USING btree (cd_stato_ente);


--
-- TOC entry 2530 (class 1259 OID 21741)
-- Name: fki_ente_tipo_progressivo_cod_ipa_ente_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_ente_tipo_progressivo_cod_ipa_ente_idx ON public.mygov_ente_tipo_progressivo USING btree (cod_ipa_ente);


--
-- TOC entry 2533 (class 1259 OID 21742)
-- Name: fki_esito_avviso_digitale_avviso_digitale_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_esito_avviso_digitale_avviso_digitale_id_idx ON public.mygov_esito_avviso_digitale USING btree (mygov_avviso_digitale_id);


--
-- TOC entry 2536 (class 1259 OID 21743)
-- Name: fki_export_dovuti_anagrafica_stato_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_export_dovuti_anagrafica_stato_id_idx ON public.mygov_export_dovuti USING btree (mygov_anagrafica_stato_id);


--
-- TOC entry 2537 (class 1259 OID 21744)
-- Name: fki_export_dovuti_ente_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_export_dovuti_ente_id_idx ON public.mygov_export_dovuti USING btree (mygov_ente_id);


--
-- TOC entry 2541 (class 1259 OID 21745)
-- Name: fki_flusso_anagrafica_stato_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_flusso_anagrafica_stato_id_idx ON public.mygov_flusso USING btree (mygov_anagrafica_stato_id);


--
-- TOC entry 2542 (class 1259 OID 21746)
-- Name: fki_flusso_ente_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_flusso_ente_id_idx ON public.mygov_flusso USING btree (mygov_ente_id);


--
-- TOC entry 2620 (class 1259 OID 74595)
-- Name: fki_flusso_massivo_anagrafica_stato_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_flusso_massivo_anagrafica_stato_id_idx ON public.mygov_flusso_massivo USING btree (mygov_anagrafica_stato_id);


--
-- TOC entry 2543 (class 1259 OID 21747)
-- Name: fki_flusso_mygov_anagrafica_stato_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_flusso_mygov_anagrafica_stato_id_idx ON public.mygov_flusso USING btree (mygov_anagrafica_stato_id);


--
-- TOC entry 2544 (class 1259 OID 21748)
-- Name: fki_flusso_mygov_ente_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_flusso_mygov_ente_id_idx ON public.mygov_flusso USING btree (mygov_ente_id);


--
-- TOC entry 2616 (class 1259 OID 74578)
-- Name: fki_flusso_tassonomia_anagrafica_stato_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_flusso_tassonomia_anagrafica_stato_id_idx ON public.mygov_flusso_tassonomia USING btree (mygov_anagrafica_stato_id);


--
-- TOC entry 2562 (class 1259 OID 21749)
-- Name: fki_import_dovuti_anagrafica_stato_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_import_dovuti_anagrafica_stato_id_idx ON public.mygov_import_dovuti USING btree (mygov_anagrafica_stato_id);


--
-- TOC entry 2563 (class 1259 OID 21750)
-- Name: fki_import_dovuti_ente_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_import_dovuti_ente_id_idx ON public.mygov_import_dovuti USING btree (mygov_ente_id);


--
-- TOC entry 2564 (class 1259 OID 21751)
-- Name: fki_import_dovuti_utente_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_import_dovuti_utente_id_idx ON public.mygov_import_dovuti USING btree (mygov_utente_id);


--
-- TOC entry 2443 (class 1259 OID 21752)
-- Name: fki_mygov_avviso_digitale_mygov_anagrafica_stato_fkey; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_avviso_digitale_mygov_anagrafica_stato_fkey ON public.mygov_avviso_digitale USING btree (mygov_anagrafica_stato_id);


--
-- TOC entry 2471 (class 1259 OID 21753)
-- Name: fki_mygov_comune_provincia_fk; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_comune_provincia_fk ON public.mygov_comune USING btree (provincia_id);


--
-- TOC entry 2501 (class 1259 OID 21754)
-- Name: fki_mygov_dovuto_elaborato_mygov_anagrafica_stato_fkey; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_dovuto_elaborato_mygov_anagrafica_stato_fkey ON public.mygov_dovuto_elaborato USING btree (mygov_anagrafica_stato_id);


--
-- TOC entry 2502 (class 1259 OID 21755)
-- Name: fki_mygov_dovuto_elaborato_mygov_flusso_fkey; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_dovuto_elaborato_mygov_flusso_fkey ON public.mygov_dovuto_elaborato USING btree (mygov_flusso_id);


--
-- TOC entry 2527 (class 1259 OID 21756)
-- Name: fki_mygov_ente_tipo_dovuto_mygov_ente_fkey; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_ente_tipo_dovuto_mygov_ente_fkey ON public.mygov_ente_tipo_dovuto USING btree (mygov_ente_id);


--
-- TOC entry 2538 (class 1259 OID 21757)
-- Name: fki_mygov_export_dovuti_mygov_utente_fkey; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_export_dovuti_mygov_utente_fkey ON public.mygov_export_dovuti USING btree (mygov_utente_id);


--
-- TOC entry 2549 (class 1259 OID 21758)
-- Name: fki_mygov_flusso_avviso_digitale_mygov_anagrafica_stato_fkey; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_flusso_avviso_digitale_mygov_anagrafica_stato_fkey ON public.mygov_flusso_avviso_digitale USING btree (mygov_anagrafica_stato_id);


--
-- TOC entry 2576 (class 1259 OID 21759)
-- Name: fki_mygov_operatore_ente_tipo_dovuto_mygov_ente_tipo_dovuto_fke; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_operatore_ente_tipo_dovuto_mygov_ente_tipo_dovuto_fke ON public.mygov_operatore_ente_tipo_dovuto USING btree (mygov_ente_tipo_dovuto_id);


--
-- TOC entry 2577 (class 1259 OID 21760)
-- Name: fki_mygov_operatore_ente_tipo_dovuto_mygov_operatore_fkey; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_operatore_ente_tipo_dovuto_mygov_operatore_fkey ON public.mygov_operatore_ente_tipo_dovuto USING btree (mygov_operatore_id);


--
-- TOC entry 2570 (class 1259 OID 21761)
-- Name: fki_mygov_operatore_mygov_ente_fk; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_operatore_mygov_ente_fk ON public.mygov_operatore USING btree (cod_ipa_ente);


--
-- TOC entry 2440 (class 1259 OID 21762)
-- Name: fki_mygov_pagamento_anonimo_mygov_carrello_fkey; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_pagamento_anonimo_mygov_carrello_fkey ON public.mygov_avviso_anonimo USING btree (mygov_carrello_id);


--
-- TOC entry 2580 (class 1259 OID 21763)
-- Name: fki_mygov_precaricato_anonimo_ente_fkey; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_precaricato_anonimo_ente_fkey ON public.mygov_precaricato_anonimo_ente USING btree (mygov_precaricato_anonimo_ente_id);


--
-- TOC entry 2602 (class 1259 OID 21764)
-- Name: fki_mygov_spontaneo_anonimo_mygov_carrello_fkey; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_spontaneo_anonimo_mygov_carrello_fkey ON public.mygov_spontaneo_anonimo USING btree (mygov_carrello_id);


--
-- TOC entry 2605 (class 1259 OID 21765)
-- Name: fki_mygov_utente_comune_id; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_utente_comune_id ON public.mygov_utente USING btree (comune_id);


--
-- TOC entry 2606 (class 1259 OID 21766)
-- Name: fki_mygov_utente_nazione_id; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_utente_nazione_id ON public.mygov_utente USING btree (nazione_id);


--
-- TOC entry 2607 (class 1259 OID 21767)
-- Name: fki_mygov_utente_provincia_id; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_mygov_utente_provincia_id ON public.mygov_utente USING btree (provincia_id);


--
-- TOC entry 2571 (class 1259 OID 21768)
-- Name: fki_operatore_cod_fed_user_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_operatore_cod_fed_user_id_idx ON public.mygov_operatore USING btree (cod_fed_user_id);


--
-- TOC entry 2581 (class 1259 OID 21769)
-- Name: fki_precaricato_anonimo_ente_carrello_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_precaricato_anonimo_ente_carrello_id_idx ON public.mygov_precaricato_anonimo_ente USING btree (mygov_carrello_id);


--
-- TOC entry 2590 (class 1259 OID 21770)
-- Name: fki_revoca_anagrafica_stato_id_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_revoca_anagrafica_stato_id_idx ON public.mygov_revoca USING btree (mygov_anagrafica_stato_id);


--
-- TOC entry 2599 (class 1259 OID 21771)
-- Name: fki_soggetto_delegante_coordinate_addebito_delega_mygov_delega_; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_soggetto_delegante_coordinate_addebito_delega_mygov_delega_ ON public.mygov_soggetto_delegante_coordinate_addebito USING btree (mygov_delega_mygov_delega_id);


--
-- TOC entry 2457 (class 1259 OID 21772)
-- Name: fki_stato_tipo_carrello_dtcreazione_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX fki_stato_tipo_carrello_dtcreazione_idx ON public.mygov_carrello USING btree (mygov_anagrafica_stato_id, tipo_carrello, dt_creazione);


--
-- TOC entry 2545 (class 1259 OID 21773)
-- Name: flusso_ente_spontaneo; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX flusso_ente_spontaneo ON public.mygov_flusso USING btree (mygov_ente_id, flg_spontaneo);


--
-- TOC entry 2503 (class 1259 OID 21774)
-- Name: idx_cod_rp_silinviarp_id_univoco_vers_lower; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_cod_rp_silinviarp_id_univoco_vers_lower ON public.mygov_dovuto_elaborato USING btree (cod_tipo_dovuto, dt_ultimo_cambio_stato, lower((cod_rp_silinviarp_id_univoco_versamento)::text));


--
-- TOC entry 2504 (class 1259 OID 21775)
-- Name: idx_lower_cod_rp_sogg_vers_id_univ_vers_iuv; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_lower_cod_rp_sogg_vers_id_univ_vers_iuv ON public.mygov_dovuto_elaborato USING btree (lower((cod_rp_sogg_vers_id_univ_vers_codice_id_univoco)::text));


--
-- TOC entry 2505 (class 1259 OID 21776)
-- Name: idx_lower_dovuto_elab_cod_rp_sogg_pag_codice_id_univoco; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_lower_dovuto_elab_cod_rp_sogg_pag_codice_id_univoco ON public.mygov_dovuto_elaborato USING btree (lower((cod_rp_sogg_pag_id_univ_pag_codice_id_univoco)::text));


--
-- TOC entry 2444 (class 1259 OID 21777)
-- Name: idx_mygov_avviso_digitale_cod_avviso; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_avviso_digitale_cod_avviso ON public.mygov_avviso_digitale USING btree (cod_ad_cod_avviso);


--
-- TOC entry 2445 (class 1259 OID 21778)
-- Name: idx_mygov_avviso_digitale_cod_id_flusso_av; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_avviso_digitale_cod_id_flusso_av ON public.mygov_avviso_digitale USING btree (cod_id_flusso_av);


--
-- TOC entry 2446 (class 1259 OID 21779)
-- Name: idx_mygov_avviso_digitale_id_dominio; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_avviso_digitale_id_dominio ON public.mygov_avviso_digitale USING btree (cod_ad_id_dominio);


--
-- TOC entry 2458 (class 1259 OID 21780)
-- Name: idx_mygov_carrello_cod_codice_esito; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_carrello_cod_codice_esito ON public.mygov_carrello USING btree (cod_e_dati_pag_codice_esito_pagamento);


--
-- TOC entry 2459 (class 1259 OID 21781)
-- Name: idx_mygov_carrello_cod_rp_id_messaggio_richiesta; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_carrello_cod_rp_id_messaggio_richiesta ON public.mygov_carrello USING btree (cod_rp_id_messaggio_richiesta);


--
-- TOC entry 2460 (class 1259 OID 21782)
-- Name: idx_mygov_carrello_id_session; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_carrello_id_session ON public.mygov_carrello USING btree (id_session);


--
-- TOC entry 2461 (class 1259 OID 21783)
-- Name: idx_mygov_carrello_id_session_fesp; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_carrello_id_session_fesp ON public.mygov_carrello USING btree (id_session_fesp);


--
-- TOC entry 2462 (class 1259 OID 21784)
-- Name: idx_mygov_carrello_modello_pagamento; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_carrello_modello_pagamento ON public.mygov_carrello USING btree (modello_pagamento);


--
-- TOC entry 2625 (class 1259 OID 74615)
-- Name: idx_mygov_carrello_queue_idstato; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE UNIQUE INDEX idx_mygov_carrello_queue_idstato ON public.mygov_carrello_queue USING btree (mygov_id_stato, mygov_id_job, mygov_carrello_id);


--
-- TOC entry 2463 (class 1259 OID 21785)
-- Name: idx_mygov_carrello_tipo_carrello; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_carrello_tipo_carrello ON public.mygov_carrello USING btree (tipo_carrello);


--
-- TOC entry 2464 (class 1259 OID 21786)
-- Name: idx_mygov_carrello_version; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_carrello_version ON public.mygov_carrello USING btree (mygov_carrello_id, version);


--
-- TOC entry 2472 (class 1259 OID 21787)
-- Name: idx_mygov_comune_comune; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_comune_comune ON public.mygov_comune USING btree (comune);


--
-- TOC entry 2486 (class 1259 OID 21788)
-- Name: idx_mygov_dovuto_cf_sogg_pag; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_dovuto_cf_sogg_pag ON public.mygov_dovuto USING btree (cod_rp_sogg_pag_id_univ_pag_codice_id_univoco);


--
-- TOC entry 2487 (class 1259 OID 21789)
-- Name: idx_mygov_dovuto_cod_iud; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_dovuto_cod_iud ON public.mygov_dovuto USING btree (cod_iud);


--
-- TOC entry 2488 (class 1259 OID 21790)
-- Name: idx_mygov_dovuto_cod_iuv; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_dovuto_cod_iuv ON public.mygov_dovuto USING btree (cod_iuv);


--
-- TOC entry 2489 (class 1259 OID 21791)
-- Name: idx_mygov_dovuto_data_esecuzione_pagamento; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_dovuto_data_esecuzione_pagamento ON public.mygov_dovuto USING btree (dt_rp_dati_vers_data_esecuzione_pagamento);


--
-- TOC entry 2490 (class 1259 OID 21792)
-- Name: idx_mygov_dovuto_data_esecuzione_pagamento_lower_iuv; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_dovuto_data_esecuzione_pagamento_lower_iuv ON public.mygov_dovuto USING btree (lower((cod_iuv)::text), dt_rp_dati_vers_data_esecuzione_pagamento);


--
-- TOC entry 2491 (class 1259 OID 21793)
-- Name: idx_mygov_dovuto_data_esecuzione_pagamento_tipo_dovuto; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_dovuto_data_esecuzione_pagamento_tipo_dovuto ON public.mygov_dovuto USING btree (cod_tipo_dovuto, dt_rp_dati_vers_data_esecuzione_pagamento);


--
-- TOC entry 2506 (class 1259 OID 21794)
-- Name: idx_mygov_dovuto_elaborato_cod_iud; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_dovuto_elaborato_cod_iud ON public.mygov_dovuto_elaborato USING btree (cod_iud);


--
-- TOC entry 2507 (class 1259 OID 21795)
-- Name: idx_mygov_dovuto_elaborato_cod_iuv; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_dovuto_elaborato_cod_iuv ON public.mygov_dovuto_elaborato USING btree (cod_iuv);


--
-- TOC entry 2508 (class 1259 OID 21796)
-- Name: idx_mygov_dovuto_elaborato_cod_rp_sogg_pag_codice_id_univoco; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_dovuto_elaborato_cod_rp_sogg_pag_codice_id_univoco ON public.mygov_dovuto_elaborato USING btree (cod_rp_sogg_pag_id_univ_pag_codice_id_univoco);


--
-- TOC entry 2509 (class 1259 OID 21797)
-- Name: idx_mygov_dovuto_elaborato_cod_sogg_pag_id_univoco_codice_esito; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_dovuto_elaborato_cod_sogg_pag_id_univoco_codice_esito ON public.mygov_dovuto_elaborato USING btree (cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, cod_e_dati_pag_codice_esito_pagamento);


--
-- TOC entry 2510 (class 1259 OID 21798)
-- Name: idx_mygov_dovuto_elaborato_dt_esito_pagamento; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_dovuto_elaborato_dt_esito_pagamento ON public.mygov_dovuto_elaborato USING btree (dt_e_dati_pag_dati_sing_pag_data_esito_singolo_pagamento);


--
-- TOC entry 2511 (class 1259 OID 21799)
-- Name: idx_mygov_dovuto_elaborato_mygov_carrello; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_dovuto_elaborato_mygov_carrello ON public.mygov_dovuto_elaborato USING btree (mygov_carrello_id);


--
-- TOC entry 2492 (class 1259 OID 21800)
-- Name: idx_mygov_dovuto_id_version; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_dovuto_id_version ON public.mygov_dovuto USING btree (mygov_dovuto_id, version);


--
-- TOC entry 2493 (class 1259 OID 21801)
-- Name: idx_mygov_dovuto_mygov_car_id; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_dovuto_mygov_car_id ON public.mygov_dovuto USING btree (mygov_carrello_id);


--
-- TOC entry 2515 (class 1259 OID 21802)
-- Name: idx_mygov_ente_e; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_ente_e ON public.mygov_ente USING btree (codice_fiscale_ente);


--
-- TOC entry 2516 (class 1259 OID 21803)
-- Name: idx_mygov_ente_mygov_carrello_cod_ipa_ente; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_ente_mygov_carrello_cod_ipa_ente ON public.mygov_ente USING btree (cod_ipa_ente);


--
-- TOC entry 2550 (class 1259 OID 21804)
-- Name: idx_mygov_flusso_avviso_digitale_cod_fad_id_flusso; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX idx_mygov_flusso_avviso_digitale_cod_fad_id_flusso ON public.mygov_flusso_avviso_digitale USING btree (cod_fad_id_flusso);


--
-- TOC entry 2433 (class 1259 OID 21805)
-- Name: idxu_mygov_anagrafica_stato_cod_stato_de_tipo_stato; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE UNIQUE INDEX idxu_mygov_anagrafica_stato_cod_stato_de_tipo_stato ON public.mygov_anagrafica_stato USING btree (cod_stato, de_tipo_stato);


--
-- TOC entry 2437 (class 1259 OID 21806)
-- Name: idxu_mygov_avviso_ente_iuv; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE UNIQUE INDEX idxu_mygov_avviso_ente_iuv ON public.mygov_avviso USING btree (mygov_ente_id, cod_iuv);


--
-- TOC entry 2559 (class 1259 OID 21807)
-- Name: idxu_mygov_identificativo_univoco_ente_tipo_identificativo_iden; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE UNIQUE INDEX idxu_mygov_identificativo_univoco_ente_tipo_identificativo_iden ON public.mygov_identificativo_univoco USING btree (mygov_ente_id, cod_tipo_identificativo, identificativo);


--
-- TOC entry 2465 (class 1259 OID 386740)
-- Name: ix_mygov_carrello_iuv_codice_id_univoco; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX ix_mygov_carrello_iuv_codice_id_univoco ON public.mygov_carrello USING btree (cod_rp_sogg_vers_id_univ_vers_codice_id_univoco);


--
-- TOC entry 2546 (class 1259 OID 21808)
-- Name: mygov_flusso_cod_request_token_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE UNIQUE INDEX mygov_flusso_cod_request_token_idx ON public.mygov_flusso USING btree (cod_request_token);


--
-- TOC entry 2617 (class 1259 OID 74579)
-- Name: mygov_flusso_massivo_cod_request_token_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE UNIQUE INDEX mygov_flusso_massivo_cod_request_token_idx ON public.mygov_flusso_tassonomia USING btree (cod_request_token);


--
-- TOC entry 2553 (class 1259 OID 21809)
-- Name: mygov_giornale_categoria_evento; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX mygov_giornale_categoria_evento ON public.mygov_giornale USING btree (categoria_evento);


--
-- TOC entry 2554 (class 1259 OID 21810)
-- Name: mygov_giornale_esito; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX mygov_giornale_esito ON public.mygov_giornale USING btree (esito);


--
-- TOC entry 2557 (class 1259 OID 21811)
-- Name: mygov_giornale_psp_index; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX mygov_giornale_psp_index ON public.mygov_giornale USING btree (identificativo_prestatore_servizi_pagamento);


--
-- TOC entry 2558 (class 1259 OID 21812)
-- Name: mygov_giornale_tipo_evento; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX mygov_giornale_tipo_evento ON public.mygov_giornale USING btree (tipo_evento);


--
-- TOC entry 2565 (class 1259 OID 21813)
-- Name: mygov_import_dovuti_cod_request_token_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE UNIQUE INDEX mygov_import_dovuti_cod_request_token_idx ON public.mygov_import_dovuti USING btree (cod_request_token);


--
-- TOC entry 2582 (class 1259 OID 21814)
-- Name: mygov_precaricato_anonimo_ente_id; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE UNIQUE INDEX mygov_precaricato_anonimo_ente_id ON public.mygov_precaricato_anonimo_ente USING btree (cod_ipa_ente, cod_iud, id_session, mygov_carrello_id);


--
-- TOC entry 2596 (class 1259 OID 21815)
-- Name: mygov_soggetto_delega_id_univoco_idx; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE INDEX mygov_soggetto_delega_id_univoco_idx ON public.mygov_soggetto_delega USING btree (cod_rp_sogg_id_univ_tipo_id_univoco, cod_rp_sogg_id_univ_codice_id_univoco);


--
-- TOC entry 2587 (class 1259 OID 21816)
-- Name: sigla_unique_id; Type: INDEX; Schema: public; Owner: mypay4
--

CREATE UNIQUE INDEX sigla_unique_id ON public.mygov_provincia USING btree (sigla);


--
-- TOC entry 2661 (class 2606 OID 21817)
-- Name: mygov_ente_tipo_progressivo fk_mygov_ente_tipo_progressivo_mygov_ente; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_ente_tipo_progressivo
    ADD CONSTRAINT fk_mygov_ente_tipo_progressivo_mygov_ente FOREIGN KEY (cod_ipa_ente) REFERENCES public.mygov_ente(cod_ipa_ente);


--
-- TOC entry 2637 (class 2606 OID 21827)
-- Name: mygov_avviso_digitale mygov_avviso_digitale_mygov_anagrafica_stato_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_avviso_digitale
    ADD CONSTRAINT mygov_avviso_digitale_mygov_anagrafica_stato_fkey FOREIGN KEY (mygov_anagrafica_stato_id) REFERENCES public.mygov_anagrafica_stato(mygov_anagrafica_stato_id);


--
-- TOC entry 2636 (class 2606 OID 21832)
-- Name: mygov_avviso mygov_avviso_mygov_ente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_avviso
    ADD CONSTRAINT mygov_avviso_mygov_ente_fkey FOREIGN KEY (mygov_ente_id) REFERENCES public.mygov_ente(mygov_ente_id);


--
-- TOC entry 2638 (class 2606 OID 21837)
-- Name: mygov_avviso_tassa_auto mygov_avviso_tassa_auto_dovuto_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_avviso_tassa_auto
    ADD CONSTRAINT mygov_avviso_tassa_auto_dovuto_fkey FOREIGN KEY (mygov_dovuto_id) REFERENCES public.mygov_dovuto(mygov_dovuto_id);


--
-- TOC entry 2641 (class 2606 OID 21842)
-- Name: mygov_carrello_multi_beneficiario mygov_carrello_multi_beneficiario_mygov_anagrafica_stato_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_carrello_multi_beneficiario
    ADD CONSTRAINT mygov_carrello_multi_beneficiario_mygov_anagrafica_stato_fkey FOREIGN KEY (mygov_anagrafica_stato_id) REFERENCES public.mygov_anagrafica_stato(mygov_anagrafica_stato_id);


--
-- TOC entry 2639 (class 2606 OID 21847)
-- Name: mygov_carrello mygov_carrello_mygov_anagrafica_stato_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_carrello
    ADD CONSTRAINT mygov_carrello_mygov_anagrafica_stato_fkey FOREIGN KEY (mygov_anagrafica_stato_id) REFERENCES public.mygov_anagrafica_stato(mygov_anagrafica_stato_id);


--
-- TOC entry 2640 (class 2606 OID 21852)
-- Name: mygov_carrello mygov_carrello_mygov_carrello_multi_beneficiario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_carrello
    ADD CONSTRAINT mygov_carrello_mygov_carrello_multi_beneficiario_fkey FOREIGN KEY (mygov_carrello_multi_beneficiario_id) REFERENCES public.mygov_carrello_multi_beneficiario(mygov_carrello_multi_beneficiario_id);


--
-- TOC entry 2642 (class 2606 OID 21857)
-- Name: mygov_comune mygov_comune_provincia_fk; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_comune
    ADD CONSTRAINT mygov_comune_provincia_fk FOREIGN KEY (provincia_id) REFERENCES public.mygov_provincia(provincia_id);


--
-- TOC entry 2643 (class 2606 OID 21862)
-- Name: mygov_delega mygov_delega_mygov_soggetto_delegante; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_delega
    ADD CONSTRAINT mygov_delega_mygov_soggetto_delegante FOREIGN KEY (mygov_soggetto_delegante_id) REFERENCES public.mygov_soggetto_delega(mygov_soggetto_delega_id);


--
-- TOC entry 2644 (class 2606 OID 21867)
-- Name: mygov_delega mygov_delega_mygov_soggetto_delegato; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_delega
    ADD CONSTRAINT mygov_delega_mygov_soggetto_delegato FOREIGN KEY (mygov_soggetto_delegato_id) REFERENCES public.mygov_soggetto_delega(mygov_soggetto_delega_id);


--
-- TOC entry 2651 (class 2606 OID 21872)
-- Name: mygov_dovuto_carrello mygov_dovuto_carrello_mygov_carrello_fk; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_dovuto_carrello
    ADD CONSTRAINT mygov_dovuto_carrello_mygov_carrello_fk FOREIGN KEY (mygov_carrello_id) REFERENCES public.mygov_carrello(mygov_carrello_id);


--
-- TOC entry 2652 (class 2606 OID 21877)
-- Name: mygov_dovuto_carrello mygov_dovuto_carrello_mygov_dovuto_fk; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_dovuto_carrello
    ADD CONSTRAINT mygov_dovuto_carrello_mygov_dovuto_fk FOREIGN KEY (mygov_dovuto_id) REFERENCES public.mygov_dovuto(mygov_dovuto_id);


--
-- TOC entry 2653 (class 2606 OID 21882)
-- Name: mygov_dovuto_elaborato mygov_dovuto_elaborato_mygov_anagrafica_stato_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_dovuto_elaborato
    ADD CONSTRAINT mygov_dovuto_elaborato_mygov_anagrafica_stato_fkey FOREIGN KEY (mygov_anagrafica_stato_id) REFERENCES public.mygov_anagrafica_stato(mygov_anagrafica_stato_id);


--
-- TOC entry 2654 (class 2606 OID 21887)
-- Name: mygov_dovuto_elaborato mygov_dovuto_elaborato_mygov_carrello_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_dovuto_elaborato
    ADD CONSTRAINT mygov_dovuto_elaborato_mygov_carrello_fkey FOREIGN KEY (mygov_carrello_id) REFERENCES public.mygov_carrello(mygov_carrello_id);


--
-- TOC entry 2655 (class 2606 OID 21892)
-- Name: mygov_dovuto_elaborato mygov_dovuto_elaborato_mygov_flusso_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_dovuto_elaborato
    ADD CONSTRAINT mygov_dovuto_elaborato_mygov_flusso_fkey FOREIGN KEY (mygov_flusso_id) REFERENCES public.mygov_flusso(mygov_flusso_id);


--
-- TOC entry 2646 (class 2606 OID 21897)
-- Name: mygov_dovuto mygov_dovuto_mygov_anagrafica_stato_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_dovuto
    ADD CONSTRAINT mygov_dovuto_mygov_anagrafica_stato_fkey FOREIGN KEY (mygov_anagrafica_stato_id) REFERENCES public.mygov_anagrafica_stato(mygov_anagrafica_stato_id);


--
-- TOC entry 2647 (class 2606 OID 21902)
-- Name: mygov_dovuto mygov_dovuto_mygov_avviso_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_dovuto
    ADD CONSTRAINT mygov_dovuto_mygov_avviso_fkey FOREIGN KEY (mygov_avviso_id) REFERENCES public.mygov_avviso(mygov_avviso_id);


--
-- TOC entry 2648 (class 2606 OID 21907)
-- Name: mygov_dovuto mygov_dovuto_mygov_carrello_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_dovuto
    ADD CONSTRAINT mygov_dovuto_mygov_carrello_fkey FOREIGN KEY (mygov_carrello_id) REFERENCES public.mygov_carrello(mygov_carrello_id);


--
-- TOC entry 2649 (class 2606 OID 21912)
-- Name: mygov_dovuto mygov_dovuto_mygov_dati_mbd_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_dovuto
    ADD CONSTRAINT mygov_dovuto_mygov_dati_mbd_fkey FOREIGN KEY (mygov_dati_marca_bollo_digitale_id) REFERENCES public.mygov_dati_marca_bollo_digitale(mygov_dati_marca_bollo_digitale_id);


--
-- TOC entry 2650 (class 2606 OID 21917)
-- Name: mygov_dovuto mygov_dovuto_mygov_flusso_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_dovuto
    ADD CONSTRAINT mygov_dovuto_mygov_flusso_fkey FOREIGN KEY (mygov_flusso_id) REFERENCES public.mygov_flusso(mygov_flusso_id);


--
-- TOC entry 2657 (class 2606 OID 21922)
-- Name: mygov_ente_sil mygov_ente_sil_mygov_ente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_ente_sil
    ADD CONSTRAINT mygov_ente_sil_mygov_ente_fkey FOREIGN KEY (mygov_ente_id) REFERENCES public.mygov_ente(mygov_ente_id);


--
-- TOC entry 2658 (class 2606 OID 21927)
-- Name: mygov_ente_sil mygov_ente_sil_mygov_ente_tipo_dovuto_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_ente_sil
    ADD CONSTRAINT mygov_ente_sil_mygov_ente_tipo_dovuto_fkey FOREIGN KEY (mygov_ente_tipo_dovuto_id) REFERENCES public.mygov_ente_tipo_dovuto(mygov_ente_tipo_dovuto_id);


--
-- TOC entry 2656 (class 2606 OID 21932)
-- Name: mygov_ente mygov_ente_stato_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_ente
    ADD CONSTRAINT mygov_ente_stato_fkey FOREIGN KEY (cd_stato_ente) REFERENCES public.mygov_anagrafica_stato(mygov_anagrafica_stato_id);


--
-- TOC entry 2659 (class 2606 OID 21937)
-- Name: mygov_ente_tipo_dovuto mygov_ente_tipo_dovuto_mygov_ente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_ente_tipo_dovuto
    ADD CONSTRAINT mygov_ente_tipo_dovuto_mygov_ente_fkey FOREIGN KEY (mygov_ente_id) REFERENCES public.mygov_ente(mygov_ente_id);


--
-- TOC entry 2660 (class 2606 OID 21942)
-- Name: mygov_ente_tipo_dovuto mygov_ente_tipo_dovuto_mygov_ente_sil_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_ente_tipo_dovuto
    ADD CONSTRAINT mygov_ente_tipo_dovuto_mygov_ente_sil_fkey FOREIGN KEY (mygov_ente_sil_id) REFERENCES public.mygov_ente_sil(mygov_ente_sil_id);


--
-- TOC entry 2662 (class 2606 OID 21947)
-- Name: mygov_esito_avviso_digitale mygov_esito_avviso_digitale_mygov_avviso_digitale_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_esito_avviso_digitale
    ADD CONSTRAINT mygov_esito_avviso_digitale_mygov_avviso_digitale_fkey FOREIGN KEY (mygov_avviso_digitale_id) REFERENCES public.mygov_avviso_digitale(mygov_avviso_digitale_id);


--
-- TOC entry 2663 (class 2606 OID 21952)
-- Name: mygov_export_dovuti mygov_export_dovuti_mygov_anagrafica_stato_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_export_dovuti
    ADD CONSTRAINT mygov_export_dovuti_mygov_anagrafica_stato_fkey FOREIGN KEY (mygov_anagrafica_stato_id) REFERENCES public.mygov_anagrafica_stato(mygov_anagrafica_stato_id);


--
-- TOC entry 2664 (class 2606 OID 21957)
-- Name: mygov_export_dovuti mygov_export_dovuti_mygov_ente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_export_dovuti
    ADD CONSTRAINT mygov_export_dovuti_mygov_ente_fkey FOREIGN KEY (mygov_ente_id) REFERENCES public.mygov_ente(mygov_ente_id);


--
-- TOC entry 2665 (class 2606 OID 21962)
-- Name: mygov_export_dovuti mygov_export_dovuti_mygov_utente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_export_dovuti
    ADD CONSTRAINT mygov_export_dovuti_mygov_utente_fkey FOREIGN KEY (mygov_utente_id) REFERENCES public.mygov_utente(mygov_utente_id);


--
-- TOC entry 2666 (class 2606 OID 21967)
-- Name: mygov_flusso mygov_flussi_mygov_anagrafica_stato_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_flusso
    ADD CONSTRAINT mygov_flussi_mygov_anagrafica_stato_fkey FOREIGN KEY (mygov_anagrafica_stato_id) REFERENCES public.mygov_anagrafica_stato(mygov_anagrafica_stato_id);


--
-- TOC entry 2685 (class 2606 OID 74588)
-- Name: mygov_flusso_massivo mygov_flussi_mygov_anagrafica_stato_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_flusso_massivo
    ADD CONSTRAINT mygov_flussi_mygov_anagrafica_stato_fkey FOREIGN KEY (mygov_anagrafica_stato_id) REFERENCES public.mygov_anagrafica_stato(mygov_anagrafica_stato_id);


--
-- TOC entry 2684 (class 2606 OID 74571)
-- Name: mygov_flusso_tassonomia mygov_flussi_tassonomia_mygov_anagrafica_stato_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_flusso_tassonomia
    ADD CONSTRAINT mygov_flussi_tassonomia_mygov_anagrafica_stato_fkey FOREIGN KEY (mygov_anagrafica_stato_id) REFERENCES public.mygov_anagrafica_stato(mygov_anagrafica_stato_id);


--
-- TOC entry 2668 (class 2606 OID 21972)
-- Name: mygov_flusso_avviso_digitale mygov_flusso_avviso_digitale_mygov_anagrafica_stato_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_flusso_avviso_digitale
    ADD CONSTRAINT mygov_flusso_avviso_digitale_mygov_anagrafica_stato_fkey FOREIGN KEY (mygov_anagrafica_stato_id) REFERENCES public.mygov_anagrafica_stato(mygov_anagrafica_stato_id);


--
-- TOC entry 2667 (class 2606 OID 21977)
-- Name: mygov_flusso mygov_flusso_mygov_ente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_flusso
    ADD CONSTRAINT mygov_flusso_mygov_ente_fkey FOREIGN KEY (mygov_ente_id) REFERENCES public.mygov_ente(mygov_ente_id);


--
-- TOC entry 2669 (class 2606 OID 21982)
-- Name: mygov_import_dovuti mygov_import_dovuti_mygov_anagrafica_stato_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_import_dovuti
    ADD CONSTRAINT mygov_import_dovuti_mygov_anagrafica_stato_fkey FOREIGN KEY (mygov_anagrafica_stato_id) REFERENCES public.mygov_anagrafica_stato(mygov_anagrafica_stato_id);


--
-- TOC entry 2670 (class 2606 OID 21987)
-- Name: mygov_import_dovuti mygov_import_dovuti_mygov_ente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_import_dovuti
    ADD CONSTRAINT mygov_import_dovuti_mygov_ente_fkey FOREIGN KEY (mygov_ente_id) REFERENCES public.mygov_ente(mygov_ente_id);


--
-- TOC entry 2671 (class 2606 OID 21992)
-- Name: mygov_import_dovuti mygov_import_dovuti_mygov_utente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_import_dovuti
    ADD CONSTRAINT mygov_import_dovuti_mygov_utente_fkey FOREIGN KEY (mygov_utente_id) REFERENCES public.mygov_utente(mygov_utente_id);


--
-- TOC entry 2674 (class 2606 OID 21997)
-- Name: mygov_operatore_ente_tipo_dovuto mygov_operatore_ente_tipo_dovuto_mygov_ente_tipo_dovuto_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_operatore_ente_tipo_dovuto
    ADD CONSTRAINT mygov_operatore_ente_tipo_dovuto_mygov_ente_tipo_dovuto_fkey FOREIGN KEY (mygov_ente_tipo_dovuto_id) REFERENCES public.mygov_ente_tipo_dovuto(mygov_ente_tipo_dovuto_id);


--
-- TOC entry 2675 (class 2606 OID 22002)
-- Name: mygov_operatore_ente_tipo_dovuto mygov_operatore_ente_tipo_dovuto_mygov_operatore_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_operatore_ente_tipo_dovuto
    ADD CONSTRAINT mygov_operatore_ente_tipo_dovuto_mygov_operatore_fkey FOREIGN KEY (mygov_operatore_id) REFERENCES public.mygov_operatore(mygov_operatore_id);


--
-- TOC entry 2672 (class 2606 OID 22007)
-- Name: mygov_operatore mygov_operatore_mygov_ente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_operatore
    ADD CONSTRAINT mygov_operatore_mygov_ente_fkey FOREIGN KEY (cod_ipa_ente) REFERENCES public.mygov_ente(cod_ipa_ente);


--
-- TOC entry 2673 (class 2606 OID 22012)
-- Name: mygov_operatore mygov_operatore_mygov_utente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_operatore
    ADD CONSTRAINT mygov_operatore_mygov_utente_fkey FOREIGN KEY (cod_fed_user_id) REFERENCES public.mygov_utente(cod_fed_user_id);


--
-- TOC entry 2676 (class 2606 OID 22017)
-- Name: mygov_precaricato_anonimo_ente mygov_precaricato_ente_anonimo_mygov_carrello_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_precaricato_anonimo_ente
    ADD CONSTRAINT mygov_precaricato_ente_anonimo_mygov_carrello_fkey FOREIGN KEY (mygov_carrello_id) REFERENCES public.mygov_carrello(mygov_carrello_id);


--
-- TOC entry 2677 (class 2606 OID 22022)
-- Name: mygov_push_esito_sil mygov_push_esito_sil_mygov_dovuto_elaborato_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_push_esito_sil
    ADD CONSTRAINT mygov_push_esito_sil_mygov_dovuto_elaborato_fkey FOREIGN KEY (mygov_dovuto_elaborato_id) REFERENCES public.mygov_dovuto_elaborato(mygov_dovuto_elaborato_id);


--
-- TOC entry 2679 (class 2606 OID 22027)
-- Name: mygov_revoca_dati_pagamenti mygov_revoca_dati_pagamenti_mygov_revoca_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_revoca_dati_pagamenti
    ADD CONSTRAINT mygov_revoca_dati_pagamenti_mygov_revoca_fkey FOREIGN KEY (mygov_revoca_id) REFERENCES public.mygov_revoca(mygov_revoca_id);


--
-- TOC entry 2678 (class 2606 OID 22032)
-- Name: mygov_revoca mygov_revoca_mygov_anagrafica_stato_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_revoca
    ADD CONSTRAINT mygov_revoca_mygov_anagrafica_stato_fkey FOREIGN KEY (mygov_anagrafica_stato_id) REFERENCES public.mygov_anagrafica_stato(mygov_anagrafica_stato_id);


--
-- TOC entry 2680 (class 2606 OID 22037)
-- Name: mygov_soggetto_delegante_coordinate_addebito mygov_soggetto_delegante_coordinate_addebito_mygov_delega; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_soggetto_delegante_coordinate_addebito
    ADD CONSTRAINT mygov_soggetto_delegante_coordinate_addebito_mygov_delega FOREIGN KEY (mygov_delega_mygov_delega_id) REFERENCES public.mygov_delega(mygov_delega_id);


--
-- TOC entry 2681 (class 2606 OID 22047)
-- Name: mygov_utente mygov_utente_comune_id; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_utente
    ADD CONSTRAINT mygov_utente_comune_id FOREIGN KEY (comune_id) REFERENCES public.mygov_comune(comune_id);


--
-- TOC entry 2645 (class 2606 OID 22052)
-- Name: mygov_delega mygov_utente_delega_mygov_ente; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_delega
    ADD CONSTRAINT mygov_utente_delega_mygov_ente FOREIGN KEY (mygov_ente_id) REFERENCES public.mygov_ente(mygov_ente_id);


--
-- TOC entry 2682 (class 2606 OID 22057)
-- Name: mygov_utente mygov_utente_nazione_id; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_utente
    ADD CONSTRAINT mygov_utente_nazione_id FOREIGN KEY (nazione_id) REFERENCES public.mygov_nazione(nazione_id);


--
-- TOC entry 2683 (class 2606 OID 22062)
-- Name: mygov_utente mygov_utente_provincia_id; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_utente
    ADD CONSTRAINT mygov_utente_provincia_id FOREIGN KEY (provincia_id) REFERENCES public.mygov_provincia(provincia_id);


--
-- TOC entry 2686 (class 2606 OID 364129)
-- Name: mygov_validazione_email mygov_validazione_email_fk; Type: FK CONSTRAINT; Schema: public; Owner: mypay4
--

ALTER TABLE ONLY public.mygov_validazione_email
    ADD CONSTRAINT mygov_validazione_email_fk FOREIGN KEY (mygov_utente_id) REFERENCES public.mygov_utente(mygov_utente_id);


--
-- TOC entry 2814 (class 0 OID 0)
-- Dependencies: 313
-- Name: FUNCTION insert_mygov_dovuto_noinout(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_dt_ultima_modifica timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean, n_flg_genera_iuv boolean, OUT result character varying, OUT result_desc character varying); Type: ACL; Schema: public; Owner: mypay4
--

GRANT ALL ON FUNCTION public.insert_mygov_dovuto_noinout(n_mygov_ente_id bigint, n_mygov_flusso_id bigint, n_num_riga_flusso numeric, n_mygov_anagrafica_stato_id bigint, n_mygov_carrello_id bigint, n_cod_iud character varying, n_cod_iuv character varying, n_dt_creazione timestamp without time zone, n_dt_ultima_modifica timestamp without time zone, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco character, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco character varying, n_de_rp_sogg_pag_anagrafica_pagatore character varying, n_de_rp_sogg_pag_indirizzo_pagatore character varying, n_de_rp_sogg_pag_civico_pagatore character varying, n_cod_rp_sogg_pag_cap_pagatore character varying, n_de_rp_sogg_pag_localita_pagatore character varying, n_de_rp_sogg_pag_provincia_pagatore character varying, n_cod_rp_sogg_pag_nazione_pagatore character varying, n_de_rp_sogg_pag_email_pagatore character varying, n_dt_rp_dati_vers_data_esecuzione_pagamento date, n_cod_rp_dati_vers_tipo_versamento character varying, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric, n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa numeric, n_cod_tipo_dovuto character varying, n_de_rp_dati_vers_dati_sing_vers_causale_versamento character varying, n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione character varying, n_mygov_utente_id bigint, n_bilancio character varying, insert_avv_dig boolean, n_flg_genera_iuv boolean, OUT result character varying, OUT result_desc character varying) TO postgres;


-- Completed on 2022-09-22 16:13:57

--
-- PostgreSQL database dump complete
--

