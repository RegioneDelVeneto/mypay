ALTER TABLE public.mygov_receipt ADD dt_last_export timestamp NULL;
ALTER TABLE public.mygov_receipt ADD num_try_export int2 NOT NULL DEFAULT 0;
ALTER TABLE public.mygov_receipt ADD status_export char NOT NULL DEFAULT 'N';

update mygov_receipt set status_export = 'S', num_try_export = 1, dt_last_export = now() where flg_exported = true;

