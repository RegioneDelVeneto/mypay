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

import it.regioneveneto.mygov.payment.mypay4.model.Dovuto;
import it.regioneveneto.mygov.payment.mypay4.model.GpdErrDovuto;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface GpdErrDovutoDao extends BaseDao {

    @SqlUpdate(
            "insert into mygov_gpd_err_dovuto (" +
                    "   mygov_gpd_err_dovuto_id" +
                    " , gpd_iupd" +
                    " , cod_errore_http" +
                    " , descrizione" +
                    " , azione" +
                    " , dt_ultima_modifica" +
                    " , mygov_ente_id" +
                    ") values (" +
                    "   nextval('mygov_gpd_err_dovuto_mygov_gpd_err_dovuto_id_seq')" +
                    " , :d.gpdIupd" +
                    " , :d.codErroreHttp" +
                    " , :d.descrizione" +
                    " , :d.azione" +
                    " , :d.dtUltimaModifica" +
                    " , :d.mygovEnteId)"
    )
    @GetGeneratedKeys("mygov_gpd_err_dovuto_id")
    long insert(@BindBean("d") GpdErrDovuto d);


    @SqlQuery("select " + GpdErrDovuto.ALIAS + ALL_FIELDS +
            " from mygov_gpd_err_dovuto " + GpdErrDovuto.ALIAS +
            " where " + GpdErrDovuto.ALIAS + ".gpd_iupd = :gpdIupd "
    )
    @RegisterFieldMapper(GpdErrDovuto.class)
    GpdErrDovuto getByIupd(String gpdIupd);


    @SqlUpdate(
            " delete from mygov_gpd_err_dovuto " +
                    " where mygov_ente_id=:enteID " +
                    " and gpd_iupd in (<iupdList>)")
    int delete(Long enteID, @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> iupdList);

}
