-- SET SCRIPT NAME
-- use pattern ####-[pa|fesp]-[dml|ddl]-script_name.sql' (max 100 chars)
-- where #### is a sequence of integer numbers (without holes and duplicates) determining script execution order
set myvars.script_name to '0040-pa-ddl-procedure_is_dovuto_annullato.sql';
-- SET SCRIPT DESCRIPTION
-- free text, max 500 chars
set myvars.script_description to 'Improved check if dovuto annullato for specific case';

-- start transaction
BEGIN;

-- check that script has not already been executed
--  and store script execution info into table mygov_script
insert into mygov_script values ( current_setting('myvars.script_name'),current_setting('myvars.script_description') );

------------------- business logic -------------------


CREATE OR REPLACE FUNCTION public.is_dovuto_annullato(identificativo character varying, tipo_identificativo character varying, c_mygov_flusso_id bigint)
 RETURNS boolean
 LANGUAGE plpgsql
AS $function$

DECLARE
      num_dovuti int;
      v_ente_id int;
	  v_identificativo text;
begin
	v_identificativo :=  identificativo;

	IF (tipo_identificativo = 'IUD') 
      THEN
              SELECT  into v_ente_id  mygov_ente_id
				FROM mygov_identificativo_univoco  a
				WHERE a.identificativo = v_identificativo 
				  AND cod_tipo_identificativo = 'IUD'
				  AND mygov_flusso_id = c_mygov_flusso_id;

          SELECT 
            INTO num_dovuti count(1)      
            FROM mygov_dovuto_elaborato dov
                ,mygov_anagrafica_stato stato
            WHERE stato.mygov_anagrafica_stato_id = dov.mygov_anagrafica_stato_id
              AND stato.cod_stato = 'ANNULLATO'
              AND dov.mygov_flusso_id in (select mygov_flusso_id from mygov_flusso  where mygov_ente_id =v_ente_id )
              AND dov.cod_iud = identificativo;
      ELSE
              SELECT  into v_ente_id  mygov_ente_id
				FROM mygov_identificativo_univoco  a
				WHERE a.identificativo = v_identificativo 
				  AND cod_tipo_identificativo = 'IUV'
				  AND mygov_flusso_id = c_mygov_flusso_id;

          SELECT 
            INTO num_dovuti count(1)      
            FROM mygov_dovuto_elaborato dov
                ,mygov_anagrafica_stato stato
            WHERE stato.mygov_anagrafica_stato_id = dov.mygov_anagrafica_stato_id
              AND stato.cod_stato = 'ANNULLATO'
              AND dov.mygov_flusso_id in (select mygov_flusso_id from mygov_flusso  where mygov_ente_id =v_ente_id )
              AND dov.cod_iuv = identificativo;
      END IF;

    IF (num_dovuti = 0) 
      THEN RETURN false;
      ELSE 
        IF (tipo_identificativo = 'IUD') 
        THEN
          SELECT 
                  INTO num_dovuti count(1)      
                  FROM mygov_dovuto dov
                      ,mygov_anagrafica_stato stato
                  WHERE stato.mygov_anagrafica_stato_id = dov.mygov_anagrafica_stato_id
                    AND stato.cod_stato != 'ANNULLATO'
                    AND dov.mygov_flusso_id in (select mygov_flusso_id from mygov_flusso  where mygov_ente_id =v_ente_id )
                    AND dov.cod_iud = identificativo;

                    IF (num_dovuti = 0 ) THEN
						 SELECT 
						  INTO num_dovuti count(1)      
						  FROM mygov_dovuto_elaborato dov
							  ,mygov_anagrafica_stato stato
						  WHERE stato.mygov_anagrafica_stato_id = dov.mygov_anagrafica_stato_id
							AND stato.cod_stato != 'ANNULLATO'
							AND dov.mygov_flusso_id in (select mygov_flusso_id from mygov_flusso  where mygov_ente_id =v_ente_id )
							AND dov.cod_iud = identificativo;
					 END if;	

          ELSE
                  SELECT 
                  INTO num_dovuti count(1)      
                  FROM mygov_dovuto dov
                      ,mygov_anagrafica_stato stato
                  WHERE stato.mygov_anagrafica_stato_id = dov.mygov_anagrafica_stato_id
                    AND stato.cod_stato != 'ANNULLATO'
                    AND dov.mygov_flusso_id in (select mygov_flusso_id from mygov_flusso  where mygov_ente_id =v_ente_id )
                    AND dov.cod_iuv = identificativo;
					
                    IF (num_dovuti = 0 ) THEN
					  SELECT 
					  INTO num_dovuti count(1)      
					  FROM mygov_dovuto_elaborato dov
						  ,mygov_anagrafica_stato stato
					  WHERE stato.mygov_anagrafica_stato_id = dov.mygov_anagrafica_stato_id
						AND stato.cod_stato != 'ANNULLATO'
						AND dov.mygov_flusso_id in (select mygov_flusso_id from mygov_flusso  where mygov_ente_id =v_ente_id )
						AND dov.cod_iuv = identificativo;
                    end if;
           END IF;
    
          IF (num_dovuti = 0) 
            THEN RETURN true;
            ELSE RETURN false;
		  END IF;
		 
      END IF;   
   end;
$function$
;

------------------------------------------------------

-- final commit
COMMIT;