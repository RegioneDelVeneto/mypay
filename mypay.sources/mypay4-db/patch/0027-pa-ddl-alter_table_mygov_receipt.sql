ALTER TABLE public.mygov_receipt ALTER COLUMN psp_company_name TYPE varchar(70) USING psp_company_name::varchar;
