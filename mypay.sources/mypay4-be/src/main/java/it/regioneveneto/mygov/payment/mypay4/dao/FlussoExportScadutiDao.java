/**
 *     MyPay - Payment portal of Regione Veneto.
 *     Copyright (C) 2022  Regione Veneto
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.regioneveneto.mygov.payment.mypay4.dao;

import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.Flusso;
import it.regioneveneto.mygov.payment.mypay4.model.FlussoExportScaduti;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Date;
import java.util.List;

public interface FlussoExportScadutiDao extends BaseDao {

  /**
   *    public FlussoExportScaduti getByRequestToken(final String requestToken) throws DataAccessException {
   * 		DetachedCriteria criteria = DetachedCriteria.forClass(FlussoExportScaduti.class);
   * 		criteria.add(Restrictions.eq("codRequestToken", requestToken));
   *
   * 		List<FlussoExportScaduti> flusso = getHibernateTemplate().findByCriteria(criteria);
   * 		if (flusso.size() > 1) {
   * 			throw new DataIntegrityViolationException("pa.flusso.flussoDuplicato");
   *        }
   * 		if (flusso.size() == 0) {
   * 			return null;
   *        }
   *
   * 		return flusso.get(0);
   *    }
   */
  @SqlQuery(
          " select " + FlussoExportScaduti.ALIAS + ALL_FIELDS +
            ", "+AnagraficaStato.FIELDS+
            ","+Ente.FIELDS+
          " from mygov_flusso_export_scaduti " + FlussoExportScaduti.ALIAS +
            " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+FlussoExportScaduti.ALIAS+".mygov_anagrafica_stato_id = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
            " inner join mygov_ente " + Ente.ALIAS + " on "+FlussoExportScaduti.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where "+FlussoExportScaduti.ALIAS+".cod_request_token = :codRequestToken ")
  @RegisterFieldMapper(FlussoExportScaduti.class)
  List<FlussoExportScaduti> getByRequestToken(String codRequestToken);

   @SqlUpdate(
           "    update mygov_flusso_export_scaduti set " +
                   "version = :fes.version" +
                   ", mygov_ente_id = :fes.mygovEnteId.mygovEnteId" +
                   ", mygov_anagrafica_stato_id = :fes.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
                   ", iuf = :fes.iuf" +
                   ", num_pagamenti_scaduti = :fes.numPagamentiScaduti" +
                   ", dt_creazione = :fes.dtCreazione" +
                   ", dt_ultima_modifica = :fes.dtUltimaModifica" +
                   ", dt_scadenza = :fes.dtScadenza" +
                   ", de_percorso_file = :fes.dePercorsoFile" +
                   ", de_nome_file = :fes.deNomeFile" +
                   ", cod_request_token = :fes.codRequestToken" +
                   ", tipi_dovuto = :fes.tipiDovuto" +
                   ", cod_errore = :fes.codErrore"+
                   " where mygov_flusso_export_scaduti_id = :fes.mygovFlussoExportScadutiId"
   )
   int updateFlusso(@BindBean("fes") FlussoExportScaduti fes);

  @SqlUpdate(
          " INSERT INTO public.mygov_flusso_export_scaduti(" +
                  "mygov_flusso_export_scaduti_id" +
                  ", version" +
                  ", mygov_ente_id" +
                  ", mygov_anagrafica_stato_id" +
                  ", iuf" +
                  ", num_pagamenti_scaduti" +
                  ", dt_creazione" +
                  ", dt_ultima_modifica" +
                  ", dt_scadenza" +
                  ", de_percorso_file" +
                  ", de_nome_file" +
                  ", cod_request_token" +
                  ", tipi_dovuto" +
                  ", cod_errore"+
                  " ) values (" +
                  "   nextval('mygov_flusso_export_scaduti_mygov_flusso_export_scaduti_id_seq')" +
                  " , :fes.version" +
                  " , :fes.mygovEnteId.mygovEnteId" +
                  " , :fes.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
                  " , :fes.iuf" +
                  " , :fes.numPagamentiScaduti" +
                  " , coalesce(:fes.dtCreazione, now())" +
                  " , coalesce(:fes.dtUltimaModifica, now())" +
                  " , coalesce(:fes.dtScadenza, now())" +
                  " , :fes.dePercorsoFile" +
                  " , :fes.deNomeFile" +
                  " , :fes.codRequestToken" +
                  " , :fes.tipiDovuto" +
                  " , :fes.codErrore)"
  )
  @GetGeneratedKeys("mygov_flusso_export_scaduti_id")
  long insert(@BindBean("fes") FlussoExportScaduti fes);

  @SqlQuery(
          " select " + FlussoExportScaduti.ALIAS + ALL_FIELDS +
                  ", "+AnagraficaStato.FIELDS+
                  ", "+Ente.FIELDS+
          " from mygov_flusso_export_scaduti " + FlussoExportScaduti.ALIAS +
            " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+FlussoExportScaduti.ALIAS+".mygov_anagrafica_stato_id = "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id " +
            " inner join mygov_ente " + Ente.ALIAS + " on "+FlussoExportScaduti.ALIAS+".mygov_ente_id = "+Ente.ALIAS+".mygov_ente_id " +
          " where " +
            ""+FlussoExportScaduti.ALIAS+".de_nome_file is null " +
            " and "+FlussoExportScaduti.ALIAS+".de_percorso_file is null " +
            " and ("+
                AnagraficaStato.ALIAS+".cod_stato='LOAD_EXPORT' and "+AnagraficaStato.ALIAS+".de_tipo_stato='export' " +
            ")" +
          " order by " + FlussoExportScaduti.ALIAS + ".dt_Creazione asc")
  @RegisterFieldMapper(FlussoExportScaduti.class)
  List<FlussoExportScaduti> getListaFlussiRichiesti();



}
