CREATE TABLE public.mygov_script (
	script_name varchar(100) NULL,
	script_description varchar(500) NULL,
	execution_date timestamp without time zone NOT NULL DEFAULT now(),
	CONSTRAINT mygov_script_pk PRIMARY KEY (script_name)
);
