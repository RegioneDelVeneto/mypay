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

import it.regioneveneto.mygov.payment.mypay4.model.DovutoCarrello;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface DovutoCarrelloDao extends BaseDao {

  @SqlQuery(" insert into mygov_dovuto_carrello (" +
      "  mygov_dovuto_carrello_id "+
      " , mygov_dovuto_id "+
      " , mygov_carrello_id "+
      " , num_rp_dati_vers_dati_sing_vers_commissione_carico_pa "+
      " , cod_rp_dati_vers_dati_sing_vers_iban_accredito "+
      " , cod_rp_dati_vers_dati_sing_vers_bic_accredito "+
      " , cod_rp_dati_vers_dati_sing_vers_iban_appoggio "+
      " , cod_rp_dati_vers_dati_sing_vers_bic_appoggio "+
      " , cod_rp_dati_vers_dati_sing_vers_credenziali_pagatore "+
      " , version "+
      " , de_rp_dati_vers_dati_sing_vers_causale_versamento_agid "+
      ") values ("+
      "  nextval('mygov_dovuto_carrello_mygov_dovuto_carrello_id_seq')"+
      " , :d.mygovDovutoId?.mygovDovutoId"+
      " , :d.mygovCarrelloId?.mygovCarrelloId"+
      " , :d.numRpDatiVersDatiSingVersCommissioneCaricoPa"+
      " , :d.codRpDatiVersDatiSingVersIbanAccredito"+
      " , :d.codRpDatiVersDatiSingVersBicAccredito"+
      " , :d.codRpDatiVersDatiSingVersIbanAppoggio"+
      " , :d.codRpDatiVersDatiSingVersBicAppoggio"+
      " , :d.codRpDatiVersDatiSingVersCredenzialiPagatore"+
      " , :d.version"+
      " , :d.deRpDatiVersDatiSingVersCausaleVersamentoAgid ) "+
      " returning mygov_dovuto_carrello_id"
  )
  @GetGeneratedKeys("mygov_dovuto_carrello_id")
  Long insert(@BindBean("d") DovutoCarrello d);

  @SqlUpdate(
      "delete from mygov_dovuto_carrello " +
          " where mygov_dovuto_id = :mygovDovutoId" +
          "   and mygov_carrello_id  = :mygovCarrelloId"
  )
  int delete(Long mygovDovutoId, Long mygovCarrelloId);

  @SqlUpdate(
    "delete from mygov_dovuto_carrello " +
      " where mygov_dovuto_id = :mygovDovutoId"
  )
  int deleteByIdDovuto(Long mygovDovutoId);

  @SqlQuery(" select from mygov_dovuto_carrello " +
          " where mygov_dovuto_id = :mygovDovutoId" +
          "   and mygov_carrello_id  = :mygovCarrelloId"
  )
  @RegisterFieldMapper(DovutoCarrello.class)
  List<DovutoCarrello> getByDovutoECarrello(Long mygovDovutoId, Long mygovCarrelloId);
}
