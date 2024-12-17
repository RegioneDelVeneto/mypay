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

import it.regioneveneto.mygov.payment.mypay4.model.DovutoPreload;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface DovutoPreloadDao extends BaseDao {

    @SqlUpdate(
            "insert into mygov_dovuto_preload (" +
                    "   mygov_dovuto_preload_id" +
                    " , mygov_dovuto_id" +
                    " , gpd_iupd" +
                    " , gpd_status" +
                    " , nuovo_gpd_status" +
                    " , mygov_ente_id" +
                    " , dt_ultima_modifica" +
                    ") values (" +
                    "   nextval('mygov_dovuto_preload_mygov_dovuto_preload_id_seq')" +
                    " , :d.mygovDovutoId" +
                    " , :d.gpdIupd" +
                    " , :d.gpdStatus" +
                    " , :d.nuovoGpdStatus" +
                    " , :d.mygovEnteId" +
                    " , :d.dtUltimaModifica)"
    )
    @GetGeneratedKeys("mygov_dovuto_preload_id")
    long insert(@BindBean("d") DovutoPreload d);


    @SqlUpdate(
            " update mygov_dovuto_preload " +
                    " set gpd_status ='D', " +
                    " nuovo_gpd_status = null, " +
                    " dt_ultima_modifica = now() " +
                    " where gpd_iupd is not null and gpd_iupd in (<ids>)" +
                    " and mygov_ente_id = :enteId"
    )
    int deleteGpdStatusByIupds(Long enteId, @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> ids);


    @SqlUpdate(
            " update mygov_dovuto_preload " +
                    " set gpd_status ='D', " +
                    " nuovo_gpd_status = '-', " +
                    " dt_ultima_modifica = now() " +
                    " where gpd_iupd is not null and gpd_iupd in (<ids>)" +
                    " and mygov_ente_id = :enteId"
    )
    int deleteGpdStatusByIupdsAck(Long enteId, @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> ids);



    @SqlUpdate(
            " update mygov_dovuto_preload " +
                    " set gpd_status = :gpdStatus, " +
//                    " nuovo_gpd_status = null, " +
                    " dt_ultima_modifica = now() " +
                    " where mygov_ente_id = :enteId and mygov_dovuto_id in (" +
                    "           select d.mygov_dovuto_id from mygov_dovuto d " +
                    "                   join mygov_flusso f on d.mygov_flusso_id = f.mygov_flusso_id " +
                    "           where d.gpd_iupd in (<ids>) and d.gpd_iupd is not null and f.mygov_ente_id = :enteId " +
                    " )"
    )
    int updateGpdStatusByIupds(Long enteId, @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> ids, char gpdStatus);

    @SqlUpdate(
            " update mygov_dovuto_preload " +
                    " set gpd_status = :gpdStatus, " +
                    " nuovo_gpd_status = null, " +
                    " dt_ultima_modifica = now() " +
                    " where mygov_ente_id = :enteId and mygov_dovuto_id in (" +
                    "           select d.mygov_dovuto_id from mygov_dovuto d " +
                    "                   join mygov_flusso f on d.mygov_flusso_id = f.mygov_flusso_id " +
                    "           where d.gpd_iupd in (<ids>) and d.gpd_iupd is not null  and f.mygov_ente_id = :enteId " +
                    " )"
    )
    int updateGpdStatusWithResetNuovoStatusGpdByIupds(Long enteId, @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> ids, char gpdStatus);

    @SqlUpdate(
            " update mygov_dovuto_preload " +
                    " set gpd_status = :gpdStatus, " +
                    " nuovo_gpd_status = null, " +
                    " dt_ultima_modifica = now() " +
                    " where mygov_dovuto_id in (<ids>)")
    int updateGpdStatus(@BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<Long> ids, char gpdStatus);

    @SqlUpdate(
            " update mygov_dovuto_preload " +
                    " set nuovo_gpd_status = :nuovoGpdStatus, " +
                    " dt_ultima_modifica = now() " +
                    " where mygov_dovuto_id in (<ids>)")
    int updateNuovoGpdStatus(@BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<Long> ids, char nuovoGpdStatus);

    @SqlUpdate(
            " update mygov_dovuto_preload " +
                    " set mygov_dovuto_id = :newIdDovuto, " +
                    " dt_ultima_modifica = now() " +
                    " where mygov_dovuto_id = :oldIdDovuto")
    int updateIdDovuto(Long oldIdDovuto, Long newIdDovuto);

    @SqlUpdate(
            " update mygov_dovuto_preload " +
                    " set gpd_iupd = :iupd, " +
                    " dt_ultima_modifica = now() " +
                    " where mygov_dovuto_preload_id = :dovutoPreloadId")
    int updateIupd(Long dovutoPreloadId, String iupd);


    @SqlQuery(" select " + DovutoPreload.ALIAS + ALL_FIELDS +
            " from mygov_dovuto_preload " + DovutoPreload.ALIAS +
            " where " + DovutoPreload.ALIAS + ".mygov_dovuto_id = :idDovuto")
    @RegisterFieldMapper(DovutoPreload.class)
    DovutoPreload getByIdDovuto(Long idDovuto);

    @SqlQuery(" select " + DovutoPreload.ALIAS + ".gpd_iupd " +
            " from mygov_dovuto_preload " + DovutoPreload.ALIAS +
            " where " + DovutoPreload.ALIAS + ".nuovo_gpd_status = :gpdStatus" +
            " and " + DovutoPreload.ALIAS + ".mygov_ente_id = :enteId" +
            " and " + DovutoPreload.ALIAS + ".gpd_iupd is not null" +
            " limit :maxResults"
    )
    List<String> getIupdListToDeleteByEnte(Long enteId, char gpdStatus, int maxResults);
}
