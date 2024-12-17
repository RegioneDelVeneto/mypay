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
import it.regioneveneto.mygov.payment.mypay4.model.ExportConservazione;
import it.regioneveneto.mygov.payment.mypay4.model.Utente;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.AllowUnusedBindings;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.time.LocalDate;
import java.util.List;

public interface ExportConservazioneDao extends BaseDao {

    @SqlUpdate(
            "insert into mygov_export_conservazione (" +
                    "  mygov_export_conservazione_id" +
                    ", version" +
                    ", mygov_ente_id" +
                    ", mygov_anagrafica_stato_id" +
                    ", de_nome_file_generato" +
                    ", num_dimensione_file_generato" +
                    ", dt_inizio_estrazione" +
                    ", dt_fine_estrazione" +
                    ", dt_creazione" +
                    ", dt_ultima_modifica" +
                    ", cod_request_token" +
                    ", mygov_utente_id" +
                    ", tipo_tracciato" +
                    ") values (" +
                    "  nextval('mygov_export_conservazione_mygov_export_conservazione_id_seq')" +
                    ", :c.version" +
                    ", :c.mygovEnteId.mygovEnteId" +
                    ", :c.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
                    ", :c.deNomeFileGenerato" +
                    ", :c.numDimensioneFileGenerato" +
                    ", :c.dtInizioEstrazione" +
                    ", :c.dtFineEstrazione" +
                    ", :c.dtCreazione" +
                    ", :c.dtUltimaModifica" +
                    ", :c.codRequestToken" +
                    ", :c.mygovUtenteId.mygovUtenteId" +
                    ", :c.tipoTracciato" +
                    ")"
    )
    @GetGeneratedKeys("mygov_export_conservazione_id")
    long insert(@BindBean("c") ExportConservazione c);

    @SqlQuery(
            " select count(1) " +
                    SQL_SEARCH_EXPORT
    )
    @RegisterFieldMapper(ExportConservazione.class)
    int getByEnteNomefileDtmodificaCount(Long mygovEnteId, String codFedUserId, String nomeFile,
                                         LocalDate dateFrom, LocalDate dateTo);

    String SQL_SEARCH_EXPORT =
            " from mygov_export_conservazione " + ExportConservazione.ALIAS +
                    " inner join mygov_ente " + Ente.ALIAS + " on " + Ente.ALIAS + ".mygov_ente_id = " + ExportConservazione.ALIAS + ".mygov_ente_id " +
                    " inner join mygov_anagrafica_stato " + AnagraficaStato.ALIAS + " on " + AnagraficaStato.ALIAS + ".mygov_anagrafica_stato_id = " + ExportConservazione.ALIAS + ".mygov_anagrafica_stato_id " +
                    " inner join mygov_utente " + Utente.ALIAS + " on " + Utente.ALIAS + ".mygov_utente_id = " + ExportConservazione.ALIAS + ".mygov_utente_id " +
                    " where " + Ente.ALIAS + ".mygov_ente_id = :mygovEnteId" +
                    " and (" + AnagraficaStato.ALIAS + ".cod_stato = 'EXPORT_ESEGUITO' or " + AnagraficaStato.ALIAS + ".cod_stato = 'EXPORT_CANCELLATO')" +
                    " and " + AnagraficaStato.ALIAS + ".de_tipo_stato = 'export'" +
                    //" and "+ExportDovuti.ALIAS+".de_nome_file_generato is not null " +
                    " and ( :nomeFile is null or " + ExportConservazione.ALIAS + ".de_nome_file_generato ilike '%' || :nomeFile || '%') " +
                    " and " + Utente.ALIAS + ".cod_fed_user_id = :codFedUserId" +
                    " and ( :dateFrom::DATE <= " + ExportConservazione.ALIAS + ".dt_ultima_modifica and " + ExportConservazione.ALIAS + ".dt_ultima_modifica < :dateTo::DATE ) ";

    @SqlQuery(
            " select " + ExportConservazione.ALIAS + ALL_FIELDS + ", " + Ente.FIELDS + ", " + AnagraficaStato.FIELDS + ", " + Utente.FIELDS +
                    SQL_SEARCH_EXPORT +
                    " limit <queryLimit>"
    )
    @RegisterFieldMapper(ExportConservazione.class)
    List<ExportConservazione> getByEnteNomefileDtmodifica(Long mygovEnteId, String codFedUserId, String nomeFile,
                                                          LocalDate dateFrom, LocalDate dateTo, @Define int queryLimit);

    @SqlQuery(
            " select " + ExportConservazione.ALIAS + ALL_FIELDS +
                    " from mygov_export_conservazione " + ExportConservazione.ALIAS +
                    " where " + ExportConservazione.ALIAS+".mygov_export_conservazione_id = :mygovExportConservazioneId"
    )
    @RegisterFieldMapper(ExportConservazione.class)
    List<ExportConservazione> getExportConservazioneByID (Long mygovExportConservazioneId);

   /* @SqlUpdate(
            " update mygov_export_conservazione " +
                    " set mygov_anagrafica_stato_id = :c.mygovAnagraficaStatoId.mygovAnagraficaStatoId"+
                    ", de_nome_file_generato = :c.deNomeFileGenerato"+
                    ", num_dimensione_file_generato = :c.numDimensioneFileGenerato"+
                    ", dt_ultima_modifica = :c.dtUltimaModifica"+
                    " where mygov_export_conservazione_id = :c.mygovExportConservazioneId"
    )
   int update (@BindBean("c") ExportConservazione c);
*/

    @SqlQuery(
            "select " + ExportConservazione.ALIAS + ALL_FIELDS + ", "+Ente.FIELDS +
                    " from mygov_export_conservazione " + ExportConservazione.ALIAS +
                    "  join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+
                    "    on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+ExportConservazione.ALIAS+".mygov_anagrafica_stato_id " +
                    "  join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+ExportConservazione.ALIAS+".mygov_ente_id " +
                    " where " + AnagraficaStato.ALIAS + ".cod_stato = '"+ Constants.STATO_EXPORT_LOAD + "'" +
                    " and "+ AnagraficaStato.ALIAS + ".de_tipo_stato = '"+ Constants.STATO_TIPO_EXPORT + "'"
    )
    @RegisterFieldMapper(ExportConservazione.class)
    List<ExportConservazione> getExportConservazioneDaEffettuare();

    @SqlUpdate(" update mygov_export_conservazione " +
            " set de_nome_file_generato = :d.deNomeFileGenerato ,"+
            " num_dimensione_file_generato = :d.numDimensioneFileGenerato, " +
            " mygov_anagrafica_stato_id = :statoId" +

            " where mygov_export_conservazione_id = :d.mygovExportConservazioneId")
    int update(@BindBean("d") ExportConservazione d, Long statoId);

    @AllowUnusedBindings
    @SqlQuery(
            "select " + ExportConservazione.ALIAS + ALL_FIELDS + ", "+Ente.FIELDS +
                    " from mygov_export_conservazione " + ExportConservazione.ALIAS +
                    "  join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+
                    "    on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+ExportConservazione.ALIAS+".mygov_anagrafica_stato_id " +
                    "  join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+ExportConservazione.ALIAS+".mygov_ente_id " +
                    " where " + AnagraficaStato.ALIAS + ".cod_stato = '"+ Constants.STATO_EXPORT_ESEGUITO + "'" +
                    " and "+ AnagraficaStato.ALIAS + ".de_tipo_stato = '"+ Constants.STATO_TIPO_EXPORT + "'" +
                    " and "+ExportConservazione.ALIAS + ".dt_ultima_modifica < now() - make_interval(hours => :orePerCancellazione)"
    )
    @RegisterFieldMapper(ExportConservazione.class)
    List<ExportConservazione> getExportDaCancellare(final Integer orePerCancellazione);

}