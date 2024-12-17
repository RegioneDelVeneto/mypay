CREATE TABLE public.mygov_dovuto_multibeneficiario_elaborato
(
    mygov_dovuto_multibeneficiario_elaborato_id bigint NOT NULL,
    cod_iud character varying(35) COLLATE pg_catalog."default" NOT NULL,
    cod_iuv character varying(35) COLLATE pg_catalog."default",
    codice_fiscale_ente character varying(11) COLLATE pg_catalog."default" NOT NULL,
    de_rp_ente_benef_denominazione_beneficiario character varying(70) COLLATE pg_catalog."default",
    cod_rp_dati_vers_dati_sing_vers_iban_accredito character varying(35) COLLATE pg_catalog."default",
    de_rp_ente_benef_indirizzo_beneficiario character varying(70) COLLATE pg_catalog."default",
    de_rp_ente_benef_civico_beneficiario character varying(16) COLLATE pg_catalog."default",
    cod_rp_ente_benef_cap_beneficiario character varying(16) COLLATE pg_catalog."default",
    de_rp_ente_benef_localita_beneficiario character varying(35) COLLATE pg_catalog."default",
    de_rp_ente_benef_provincia_beneficiario character varying(35) COLLATE pg_catalog."default",
    cod_rp_ente_benef_nazione_beneficiario character varying(2) COLLATE pg_catalog."default",
    num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento numeric(12,2) NOT NULL,
    mygov_dovuto_elaborato_id bigint,
    dt_creazione timestamp without time zone NOT NULL,
    dt_ultima_modifica timestamp without time zone NOT NULL,
    exported boolean,
    blb_rt_payload bytea,
    CONSTRAINT mygov_dovuto_multibeneficiario_elaborato_pkey PRIMARY KEY (mygov_dovuto_multibeneficiario_elaborato_id),
    CONSTRAINT mygov_dovuto_multibeneficiario_elaborato_mygov_dovuto_fkey FOREIGN KEY (mygov_dovuto_elaborato_id)
        REFERENCES public.mygov_dovuto_elaborato (mygov_dovuto_elaborato_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- Index: fki_dovuto_elaborato_id_idx

-- DROP INDEX public.fki_dovuto_elaborato_id_idx;

CREATE INDEX fki_dovuto_elaborato_id_idx
    ON public.mygov_dovuto_multibeneficiario_elaborato USING btree
    (mygov_dovuto_elaborato_id ASC NULLS LAST);
-- Index: idx_mygov_dovuto_multibeneficiario_elaborato_cod_iud

-- DROP INDEX public.idx_mygov_dovuto_multibeneficiario_elaborato_cod_iud;

CREATE INDEX idx_mygov_dovuto_multibeneficiario_elaborato_cod_iud
    ON public.mygov_dovuto_multibeneficiario_elaborato USING btree
    (cod_iud COLLATE pg_catalog."default" ASC NULLS LAST);
-- Index: idx_mygov_dovuto_multibeneficiario_elaborato_cod_iuv

-- DROP INDEX public.idx_mygov_dovuto_multibeneficiario_elaborato_cod_iuv;

CREATE INDEX idx_mygov_dovuto_multibeneficiario_elaborato_cod_iuv
    ON public.mygov_dovuto_multibeneficiario_elaborato USING btree
    (cod_iuv COLLATE pg_catalog."default" ASC NULLS LAST);
	
	
-- SEQUENCE: public.mygov_dov_multi_elab_mygov_dov_multi_elab_id_seq
-- DROP SEQUENCE public.mygov_dov_multi_elab_mygov_dov_multi_elab_id_seq;

CREATE SEQUENCE public.mygov_dov_multi_elab_mygov_dov_multi_elab_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;