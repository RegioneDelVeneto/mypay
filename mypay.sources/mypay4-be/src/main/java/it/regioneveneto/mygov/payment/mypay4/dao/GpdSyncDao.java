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

import it.regioneveneto.mygov.payment.mypay4.model.GpdSync;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Date;
import java.util.List;

public interface GpdSyncDao extends BaseDao {

    @SqlUpdate(
            "insert into mygov_gpd_sync (" +
                    "   mygov_gpd_sync_id" +
                    " , version" +
                    " , mygov_flusso_id" +
                    " , mygov_ente_id" +
                    " , filename" +
                    " , file_id" +
                    " , cod_stato" +
                    " , num_dovuti_inviati" +
                    " , num_dovuti_elaborati" +
                    " , dt_creazione" +
                    " , dt_ultima_modifica" +
                    ") values (" +
                    "   nextval('mygov_gpd_sync_mygov_gpd_sync_id_seq')" +
                    " , :d.version" +
                    " , :d.mygovFlussoId" +
                    " , :d.mygovEnteId" +
                    " , :d.filename" +
                    " , :d.fileId" +
                    " , :d.codStato" +
                    " , :d.numDovutiInviati" +
                    " , :d.numDovutiElaborati" +
                    " , :d.dtCreazione" +
                    " , :d.dtUltimaModifica)"
    )
    @GetGeneratedKeys("mygov_gpd_sync_id")
    long insert(@BindBean("d") GpdSync d);


    @SqlQuery("select " + GpdSync.ALIAS + ALL_FIELDS +
            " from mygov_gpd_sync " + GpdSync.ALIAS +
            " where " + GpdSync.ALIAS + ".cod_stato = :codStato " +
            " and " + GpdSync.ALIAS + ".num_rich_elaborazione < :maxRichiesteEsitoElaborazione " +
            " order by " + GpdSync.ALIAS + ".mygov_gpd_sync_id"
    )
    @RegisterFieldMapper(GpdSync.class)
    List<GpdSync> getByCodStato(String codStato, int maxRichiesteEsitoElaborazione);

    @SqlUpdate(
            " update mygov_gpd_sync " +
                    " set cod_stato = :codStato," +
                    "     num_dovuti_elaborati = :numDovutiElaborati, " +
                    "     dt_ultima_modifica = :dtUltimaModifica " +
                    " where mygov_gpd_sync_id = :gpdSyncId")
    int updateStatusAndNumDovutiElaborati(Long gpdSyncId, String codStato, int numDovutiElaborati, Date dtUltimaModifica);

    @SqlUpdate(
            " update mygov_gpd_sync " +
                    " set cod_stato = :codStato," +
                    "     dt_ultima_modifica = now() " +
                    " where mygov_flusso_id = :idFlusso")
    int updateStatusByFlussoId(String codStato, Long idFlusso);

    @SqlUpdate(
            " update mygov_gpd_sync " +
                    " set num_rich_elaborazione = num_rich_elaborazione + 1," +
                    " num_dovuti_elaborati = :numDovutiElaborati," +
                    " dt_ultima_modifica = now() " +
                    " where mygov_gpd_sync_id = :gpdSyncId")
    int updateElaborazioni(Long gpdSyncId, int numDovutiElaborati);
}
