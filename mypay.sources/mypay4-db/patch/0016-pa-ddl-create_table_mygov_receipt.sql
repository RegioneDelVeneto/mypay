-- public.mygov_receipt definition

-- Drop table

-- DROP TABLE public.mygov_receipt;

CREATE TABLE public.mygov_receipt (
	mygov_receipt_id int8 NOT NULL,
	dt_creazione timestamp NOT NULL,
	mygov_dovuto_elaborato_id int8 NULL,
	receipt_id varchar(35) NOT NULL,
	notice_number varchar(18) NOT NULL,
	fiscal_code varchar(11) NOT NULL,
	outcome varchar(2) NOT NULL,
	creditor_reference_id varchar(35) NOT NULL,
	payment_amount numeric(12, 2) NOT NULL,
	description varchar(140) NOT NULL,
	company_name varchar(140) NOT NULL,
	office_name varchar(140) NOT NULL,
	unique_identifier_type_debtor bpchar(1) NOT NULL,
	unique_identifier_value_debtor varchar(16) NOT NULL,
	full_name_debtor varchar(70) NOT NULL,
	street_name_debtor varchar(70) NOT NULL,
	civic_number_debtor varchar(16) NOT NULL,
	postal_code_debtor varchar(16) NOT NULL,
	city_debtor varchar(35) NOT NULL,
	state_province_region_debtor varchar(35) NOT NULL,
	country_debtor varchar(2) NOT NULL,
	email_debtor varchar(256) NOT NULL,
	id_psp varchar(35) NOT NULL,
	psp_fiscal_code varchar(70) NOT NULL,
	psp_partita_iva varchar(20) NOT NULL,
	psp_company_name varchar(35) NOT NULL,
	id_channel varchar(35) NOT NULL,
	channel_description varchar(35) NOT NULL,
	unique_identifier_type_payer bpchar(1) NOT NULL,
	unique_identifier_value_payer varchar(16) NOT NULL,
	full_name_payer varchar(70) NOT NULL,
	street_name_payer varchar(70) NOT NULL,
	civic_number_payer varchar(16) NOT NULL,
	postal_code_payer varchar(16) NOT NULL,
	city_payer varchar(35) NOT NULL,
	state_province_region_payer varchar(35) NOT NULL,
	country_payer varchar(2) NOT NULL,
	email_payer varchar(256) NOT NULL,
	payment_method varchar(35) NOT NULL,
	fee numeric(12, 2) NOT NULL,
	payment_date_time timestamp NOT NULL,
	application_date timestamp NOT NULL,
	transfer_date timestamp NOT NULL,
	transfer_amount_1 numeric(12, 2)  NOT NULL,
	fiscal_code_pa_1 varchar(11) NOT NULL,
	iban_1 varchar(35) NOT NULL,
	remittance_information_1 varchar(140) NOT NULL,
	transfer_category_1 varchar(140) NOT NULL,
	transfer_amount_2 numeric(12, 2) NULL,
	fiscal_code_pa_2 varchar(11) NULL,
	iban_2 varchar(35) NULL,
	remittance_information_2 varchar(140) NULL,
	transfer_category_2 varchar(140) NULL,
	CONSTRAINT mygov_receipt_pkey PRIMARY KEY (mygov_receipt_id),
	CONSTRAINT mygov_receipt_mygov_dovuto_elaborato_fkey FOREIGN KEY (mygov_dovuto_elaborato_id) REFERENCES public.mygov_dovuto_elaborato(mygov_dovuto_elaborato_id)
);
CREATE INDEX fki_mygov_receipt_dovuto_elaborato_id_idx ON public.mygov_receipt USING btree (mygov_dovuto_elaborato_id);
CREATE SEQUENCE public.mygov_receipt_mygov_receipt_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;