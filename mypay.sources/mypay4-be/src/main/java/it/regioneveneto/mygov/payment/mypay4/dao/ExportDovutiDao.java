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

import it.regioneveneto.mygov.payment.mypay4.model.*;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface ExportDovutiDao extends BaseDao {

  @SqlUpdate(
      "insert into mygov_export_dovuti (" +
          "  mygov_export_dovuti_id" +
          ", version" +
          ", mygov_ente_id" +
          ", mygov_anagrafica_stato_id" +
          ", de_nome_file_generato" +
          ", num_dimensione_file_generato" +
          ", dt_inizio_estrazione" +
          ", dt_fine_estrazione" +
          ", dt_creazione" +
          ", dt_ultima_modifica" +
          ", cod_tipo_dovuto" +
          ", cod_request_token" +
          ", mygov_utente_id" +
          ", flg_ricevuta" +
          ", flg_incrementale" +
          ", versione_tracciato" +
          ", flg_mypivot" +
          ") values (" +
          "  nextval('mygov_export_dovuti_mygov_export_dovuti_id_seq')" +
          ", :d.version" +
          ", :d.mygovEnteId.mygovEnteId" +
          ", :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
          ", :d.deNomeFileGenerato" +
          ", :d.numDimensioneFileGenerato" +
          ", :d.dtInizioEstrazione" +
          ", :d.dtFineEstrazione" +
          ", :d.dtCreazione" +
          ", :d.dtUltimaModifica" +
          ", :d.codTipoDovuto" +
          ", :d.codRequestToken" +
          ", :d.mygovUtenteId.mygovUtenteId" +
          ", :d.flgRicevuta" +
          ", :d.flgIncrementale" +
          ", :d.versioneTracciato" +
          ", :d.flgMypivot" +
          ")"
  )
  @GetGeneratedKeys("mygov_export_dovuti_id")
  long insert(@BindBean("d") ExportDovuti d);

  @SqlUpdate(
      "update mygov_export_dovuti set " +
          " version = :d.version" +
          ", mygov_ente_id = :d.mygovEnteId.mygovEnteId" +
          ", mygov_anagrafica_stato_id = :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
          ", de_nome_file_generato = :d.deNomeFileGenerato" +
          ", num_dimensione_file_generato = :d.numDimensioneFileGenerato" +
          ", dt_inizio_estrazione = :d.dtInizioEstrazione" +
          ", dt_fine_estrazione = :d.dtFineEstrazione" +
          ", dt_creazione = :d.dtCreazione" +
          ", dt_ultima_modifica = :d.dtUltimaModifica" +
          ", cod_tipo_dovuto = :d.codTipoDovuto" +
          ", cod_request_token = :d.codRequestToken" +
          ", mygov_utente_id = :d.mygovUtenteId.mygovUtenteId" +
          ", flg_ricevuta = :d.flgRicevuta" +
          ", flg_incrementale = :d.flgIncrementale" +
          ", versione_tracciato = :d.versioneTracciato" +
          ", mygov_anagrafica_stato_multibeneficiario_id = :d.mygovAnagraficaStatoMultibeneficiarioId.mygovAnagraficaStatoId" +
          " where mygov_export_dovuti_id = :d.mygovExportDovutiId")
  int update(@BindBean("d") ExportDovuti d);

  String SQL_SEARCH_EXPORT =
      " from mygov_export_dovuti " + ExportDovuti.ALIAS +
          " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + ExportDovuti.ALIAS + ".mygov_ente_id " +
          " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + ExportDovuti.ALIAS + ".mygov_anagrafica_stato_id " +
          " inner join mygov_utente " + Utente.ALIAS + " on " + Utente.ALIAS + ".mygov_utente_id = " + ExportDovuti.ALIAS + ".mygov_utente_id " +
          " where " + Ente.ALIAS + ".mygov_ente_id = :mygovEnteId" +
          " and " + AnagraficaStato.ALIAS + ".cod_stato = 'EXPORT_ESEGUITO'" +
          " and " + AnagraficaStato.ALIAS + ".de_tipo_stato = 'export'" +
          //" and "+ExportDovuti.ALIAS+".de_nome_file_generato is not null " +
          " and ( :nomeFile is null or " + ExportDovuti.ALIAS + ".de_nome_file_generato ilike '%' || :nomeFile || '%') " +
          " and ( :codFedUserId is null or " + Utente.ALIAS + ".cod_fed_user_id = :codFedUserId)" +
          " and ( :dateFrom::DATE <= " + ExportDovuti.ALIAS + ".dt_ultima_modifica and " + ExportDovuti.ALIAS + ".dt_ultima_modifica < :dateTo::DATE ) ";

  @SqlQuery(
      " select " + ExportDovuti.ALIAS + ALL_FIELDS + ", " + Ente.FIELDS + ", " + AnagraficaStato.FIELDS + ", " + Utente.FIELDS +
          SQL_SEARCH_EXPORT +
          " order by " + ExportDovuti.ALIAS + ".dt_creazione DESC " +
          " limit <queryLimit>"
  )
  @RegisterFieldMapper(ExportDovuti.class)
  List<ExportDovuti> getByEnteNomefileDtmodifica(Long mygovEnteId, String codFedUserId, String nomeFile,
                                                 LocalDate dateFrom, LocalDate dateTo, @Define int queryLimit);

  @SqlQuery(
      " select count(1) " +
          SQL_SEARCH_EXPORT
  )
  @RegisterFieldMapper(ExportDovuti.class)
  int getByEnteNomefileDtmodificaCount(Long mygovEnteId, String codFedUserId, String nomeFile,
                                       LocalDate dateFrom, LocalDate dateTo);

  @SqlQuery(
      " select " + ExportDovuti.ALIAS + ALL_FIELDS + ", " + Ente.FIELDS + ", " + AnagraficaStato.FIELDS + ", " + Utente.FIELDS +
          " from mygov_export_dovuti " + ExportDovuti.ALIAS +
          " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + ExportDovuti.ALIAS + ".mygov_ente_id " +
          " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + ExportDovuti.ALIAS + ".mygov_anagrafica_stato_id " +
          " inner join mygov_utente " + Utente.ALIAS + " on " + Utente.ALIAS + ".mygov_utente_id = " + ExportDovuti.ALIAS + ".mygov_utente_id " +
          " where " + ExportDovuti.ALIAS + ".cod_request_token = :codRequestToken "
  )
  @RegisterFieldMapper(ExportDovuti.class)
  List<ExportDovuti> getExportByRequestToken(String codRequestToken);


  @SqlQuery(
      " select " + ExportDovuti.ALIAS + ALL_FIELDS + ", " +
          Ente.FIELDS + ", " +
          AnagraficaStato.FIELDS + ", " +
          Utente.FIELDS +
          " from mygov_export_dovuti " + ExportDovuti.ALIAS +
          " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + ExportDovuti.ALIAS + ".mygov_ente_id " +
          " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + ExportDovuti.ALIAS + ".mygov_anagrafica_stato_id " +
          " inner join mygov_utente " + Utente.ALIAS + " on " + Utente.ALIAS + ".mygov_utente_id = " + ExportDovuti.ALIAS + ".mygov_utente_id " +
          " where " + ExportDovuti.ALIAS + ".mygov_export_dovuti_id = :idExportDovuti"
  )
  @RegisterFieldMapper(ExportDovuti.class)
  ExportDovuti getExportDovutoById(Long idExportDovuti);

  @SqlQuery(
      "select " + DovutoElaborato.ALIAS + ALL_FIELDS + ","
          + "case when " + DovutoElaborato.ALIAS + ".modello_pagamento ='4' then case when " + DovutoMultibeneficiarioElaborato.ALIAS + ".mygov_dovuto_elaborato_id is not null then " + DovutoElaborato.ALIAS + ".num_e_dati_pag_dati_sing_pag_singolo_importo_pagato else  " + DovutoElaborato.ALIAS + ".num_e_dati_pag_importo_totale_pagato end else " + DovutoElaborato.ALIAS + ".num_e_dati_pag_importo_totale_pagato end as  mygovIdDovutoElaboratoMultibeneficiario,"
          + Flusso.FIELDS + ","
          + Ente.FIELDS_WITHOUT_LOGO + "," +
          AnagraficaStato.FIELDS +
          " from mygov_flusso " + Flusso.ALIAS +
          ",mygov_ente " + Ente.ALIAS +
          ",mygov_anagrafica_stato anag_stato_flusso" +
          ",mygov_anagrafica_stato " + AnagraficaStato.ALIAS +
          ",mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
          " left outer join mygov_dovuto_multibeneficiario_elaborato " + DovutoMultibeneficiarioElaborato.ALIAS + " on (" + DovutoMultibeneficiarioElaborato.ALIAS + ".mygov_dovuto_elaborato_id = " + DovutoElaborato.ALIAS + ".mygov_dovuto_elaborato_id) " +
          "where " + Flusso.ALIAS + ".mygov_anagrafica_stato_id = anag_stato_flusso.mygov_anagrafica_stato_id " +
          "and " + DovutoElaborato.ALIAS + ".mygov_anagrafica_stato_id = " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id " +
          "and " + DovutoElaborato.ALIAS + ".mygov_flusso_id = " + Flusso.ALIAS + ".mygov_flusso_id " +
          "and " + Flusso.ALIAS + ".mygov_ente_id = " + Ente.ALIAS + ".mygov_ente_id " +
          "and " + Flusso.ALIAS + ".mygov_ente_id = :idEnte " +
          "and " + Flusso.ALIAS + ".flg_attivo = true " +
          "and " + DovutoElaborato.ALIAS + ".flg_dovuto_attuale = true " +
          "and (" + AnagraficaStato.ALIAS + ".cod_stato = 'COMPLETATO' " +
          "and " + AnagraficaStato.ALIAS + ".de_tipo_stato = 'dovuto' " +
          "and " + DovutoElaborato.ALIAS + ".num_e_dati_pag_dati_sing_pag_singolo_importo_pagato > 0 " +
          "AND ((:flgIncrementale = true AND " + DovutoElaborato.ALIAS + ".dt_ultima_modifica_e >= :dataOraInizioEstrazione and  " +
          DovutoElaborato.ALIAS + ".dt_ultima_modifica_e < :dataOraFineEstrazione) " +
          "OR (:flgIncrementale = false AND " + DovutoElaborato.ALIAS + ".dt_e_dati_pag_dati_sing_pag_data_esito_singolo_pagamento >= :dataInizioEstrazione " +
          "and " + DovutoElaborato.ALIAS + ".dt_e_dati_pag_dati_sing_pag_data_esito_singolo_pagamento <= :dataFineEstrazione) " +
          "))" +
          "and anag_stato_flusso.cod_stato = 'CARICATO' " +
          "and anag_stato_flusso.de_tipo_stato = 'flusso' " +
          "and  " + DovutoElaborato.ALIAS + ".cod_tipo_dovuto <> 'MARCA_BOLLO_DIGITALE' " +
          "and case" +
          " when :codTipoDovuto is not null then " + DovutoElaborato.ALIAS + ".cod_tipo_dovuto = :codTipoDovuto " +
          " when :codTipoDovuto is null and :codFedUserId not like '%' || 'WS_USER' || '%' then " +
          DovutoElaborato.ALIAS + ".cod_tipo_dovuto in (" +
          "select " +
          EnteTipoDovuto.ALIAS + ".cod_tipo " +
          "from " +
          "mygov_ente_tipo_dovuto " + EnteTipoDovuto.ALIAS + "," +
          "mygov_operatore_ente_tipo_dovuto " + OperatoreEnteTipoDovuto.ALIAS + "," +
          "mygov_operatore " + Operatore.ALIAS +
          " where " +
          Operatore.ALIAS + ".cod_fed_user_id = :codFedUserId " +
          "and " + Operatore.ALIAS + ".cod_ipa_ente = :codIpaEnte " +
          "and " + Operatore.ALIAS + ".mygov_operatore_id = " + OperatoreEnteTipoDovuto.ALIAS + ".mygov_operatore_id " +
          "and " + OperatoreEnteTipoDovuto.ALIAS + ".flg_attivo = true " +
          "and " + OperatoreEnteTipoDovuto.ALIAS + ".mygov_ente_tipo_dovuto_id = " + EnteTipoDovuto.ALIAS + ".mygov_ente_tipo_dovuto_id) " +
          "else 1 = 1 end"
  )
  @RegisterFieldMapper(DovutoElaboratoWithAdditionalInfo.class)
  List<DovutoElaboratoWithAdditionalInfo> getRowForExportDovuto(Long idEnte, boolean flgIncrementale, String codTipoDovuto, String codFedUserId, Date dataOraInizioEstrazione, Date dataOraFineEstrazione, Date dataInizioEstrazione, Date dataFineEstrazione, String codIpaEnte);


  @SqlQuery(
      "select update_mygov_export_dovuti(:mygovExportDovutiId, :dtUltimaModifica) as result"
  )
  int updateExportDovuti(Long mygovExportDovutiId, Date dtUltimaModifica);

  @SqlUpdate(
      "update" +
          " mygov_export_dovuti " +
          "set" +
          " mygov_anagrafica_stato_id = :idStatoExportError," +
          " version = version + 1," +
          " dt_ultima_modifica = current_timestamp " +
          "where" +
          " dt_ultima_modifica + interval '12 hours' < current_timestamp" +
          " and mygov_anagrafica_stato_id = :idStatoExportElab"
  )
  int updateObsoleteRowsExportDovuti(Long idStatoExportError, Long idStatoExportElab);


}
