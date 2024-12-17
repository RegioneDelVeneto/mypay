ALTER TABLE public.mygov_receipt ALTER COLUMN unique_identifier_value_payer DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN unique_identifier_type_payer DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN full_name_payer DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN street_name_payer DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN civic_number_payer DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN postal_code_payer DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN city_payer DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN state_province_region_payer DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN country_payer DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN email_payer DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN street_name_debtor DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN civic_number_debtor DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN postal_code_debtor DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN city_debtor DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN state_province_region_debtor DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN country_debtor DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN email_debtor DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN psp_partita_iva DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN psp_fiscal_code DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN id_channel SET NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN channel_description SET NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN payment_method DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN fee DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN payment_date_time DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN application_date DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN transfer_date DROP NOT NULL;
ALTER TABLE public.mygov_receipt ALTER COLUMN office_name DROP NOT NULL;
