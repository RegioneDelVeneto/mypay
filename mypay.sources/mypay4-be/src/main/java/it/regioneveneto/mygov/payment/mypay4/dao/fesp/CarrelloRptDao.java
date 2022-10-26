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
package it.regioneveneto.mygov.payment.mypay4.dao.fesp;

import it.regioneveneto.mygov.payment.mypay4.dao.BaseDao;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.CarrelloRp;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.CarrelloRpt;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface CarrelloRptDao extends BaseDao {

  @SqlQuery("select " + CarrelloRpt.ALIAS + ALL_FIELDS +", "+CarrelloRp.FIELDS+
      " from mygov_carrello_rpt " + CarrelloRpt.ALIAS +
      " inner join mygov_carrello_rp "+ CarrelloRp.ALIAS+" on "+
        CarrelloRp.ALIAS+".mygov_carrello_rp_id = "+ CarrelloRpt.ALIAS+".mygov_carrello_rp_id " +
      " where "+ CarrelloRpt.ALIAS+".mygov_carrello_rpt_id = :id " )
  @RegisterFieldMapper(CarrelloRpt.class)
  Optional<CarrelloRpt> getById(Long id);

  @SqlQuery("select " + CarrelloRpt.ALIAS + ALL_FIELDS +", "+CarrelloRp.FIELDS+
      "  from mygov_carrello_rpt " + CarrelloRpt.ALIAS +
      " inner join mygov_carrello_rp "+ CarrelloRp.ALIAS+" on "+
        CarrelloRp.ALIAS+".mygov_carrello_rp_id = "+ CarrelloRpt.ALIAS+".mygov_carrello_rp_id " +
      " where "+ CarrelloRpt.ALIAS+".cod_rpt_inviacarrellorpt_id_carrello = :idSession ")
  @RegisterFieldMapper(CarrelloRpt.class)
  Optional<CarrelloRpt> getByIdSession(String idSession);

  @SqlUpdate(" insert into mygov_carrello_rpt (" +
      "  mygov_carrello_rpt_id, version, cod_ack_carrello_rpt, mygov_carrello_rp_id, dt_creazione, dt_ultima_modifica"+
      ", cod_rpt_inviacarrellorpt_id_carrello, cod_rpt_inviacarrellorpt_id_intermediario_pa"+
      ", cod_rpt_inviacarrellorpt_id_stazione_intermediario_pa, de_rpt_inviacarrellorpt_password" +
      ", cod_rpt_inviacarrellorpt_id_psp"+
      ", cod_rpt_inviacarrellorpt_id_intermediario_psp, cod_rpt_inviacarrellorpt_id_canale"+
      ", de_rpt_inviacarrellorpt_esito_complessivo_operazione, cod_rpt_inviacarrellorpt_url" +
      ", cod_rpt_inviacarrellorpt_fault_code , cod_rpt_inviacarrellorpt_fault_string" +
      ", cod_rpt_inviacarrellorpt_id, de_rpt_inviacarrellorpt_description"+
      ", num_rpt_inviacarrellorpt_serial, cod_rpt_silinviacarrellorpt_original_fault_code"+
      ", de_rpt_silinviacarrellorpt_original_fault_string" +
      ", de_rpt_silinviacarrellorpt_original_fault_description ) values ( " +
      " nextval('mygov_carrello_rpt_id_seq') "+
      " , :c.version" +
      " , :c.codAckCarrelloRpt" +
      " , :c.mygovCarrelloRpId?.mygovCarrelloRpId" +
      " , :c.dtCreazione" +
      " , :c.dtUltimaModifica" +
      " , :c.codRptInviacarrellorptIdCarrello" +
      " , :c.codRptInviacarrellorptIdIntermediarioPa" +
      " , :c.codRptInviacarrellorptIdStazioneIntermediarioPa" +
      " , :c.deRptInviacarrellorptPassword" +
      " , :c.codRptInviacarrellorptIdPsp" +
      " , :c.codRptInviacarrellorptIdIntermediarioPsp" +
      " , :c.codRptInviacarrellorptIdCanale" +
      " , :c.deRptInviacarrellorptEsitoComplessivoOperazione" +
      " , :c.codRptInviacarrellorptUrl" +
      " , :c.codRptInviacarrellorptFaultCode" +
      " , :c.codRptInviacarrellorptFaultString" +
      " , :c.codRptInviacarrellorptId" +
      " , :c.deRptInviacarrellorptDescription" +
      " , :c.numRptInviacarrellorptSerial" +
      " , :c.codRptSilinviacarrellorptOriginalFaultCode" +
      " , :c.deRptSilinviacarrellorptOriginalFaultString" +
      " , :c.deRptSilinviacarrellorptOriginalFaultDescription );"
  )
  @GetGeneratedKeys("mygov_carrello_rpt_id")
  long insert(@BindBean("c") CarrelloRpt c);

  @SqlUpdate("update mygov_carrello_rpt set "+
      " dt_ultima_modifica = :c.dtUltimaModifica" +
      ", cod_rpt_inviacarrellorpt_id_carrello = :c.codRptInviacarrellorptIdCarrello" +
      ", cod_rpt_inviacarrellorpt_id_intermediario_pa = :c.codRptInviacarrellorptIdIntermediarioPa" +
      ", cod_rpt_inviacarrellorpt_id_stazione_intermediario_pa = :c.codRptInviacarrellorptIdStazioneIntermediarioPa" +
      ", de_rpt_inviacarrellorpt_password = :c.deRptInviacarrellorptPassword" +
      ", cod_rpt_inviacarrellorpt_id_psp = :c.codRptInviacarrellorptIdPsp" +
      ", cod_rpt_inviacarrellorpt_id_intermediario_psp = :c.codRptInviacarrellorptIdIntermediarioPsp" +
      ", cod_rpt_inviacarrellorpt_id_canale = :c.codRptInviacarrellorptIdCanale" +
      ", de_rpt_inviacarrellorpt_esito_complessivo_operazione = :c.deRptInviacarrellorptEsitoComplessivoOperazione" +
      ", cod_rpt_inviacarrellorpt_url = :c.codRptInviacarrellorptUrl" +
      ", cod_rpt_inviacarrellorpt_fault_code = :c.codRptInviacarrellorptFaultCode" +
      ", cod_rpt_inviacarrellorpt_fault_string = :c.codRptInviacarrellorptFaultString" +
      ", cod_rpt_inviacarrellorpt_id = :c.codRptInviacarrellorptId" +
      ", de_rpt_inviacarrellorpt_description = :c.deRptInviacarrellorptDescription" +
      ", num_rpt_inviacarrellorpt_serial = :c.numRptInviacarrellorptSerial" +
      ", cod_rpt_silinviacarrellorpt_original_fault_code = :c.codRptSilinviacarrellorptOriginalFaultCode" +
      ", de_rpt_silinviacarrellorpt_original_fault_string = :c.deRptSilinviacarrellorptOriginalFaultString" +
      ", de_rpt_silinviacarrellorpt_original_fault_description = :c.deRptSilinviacarrellorptOriginalFaultDescription "+
      " where mygov_carrello_rpt_id = :c.mygovCarrelloRptId ")
  int update(@BindBean("c") CarrelloRpt c);

  @SqlQuery("select " + CarrelloRpt.ALIAS + ALL_FIELDS +
      " from mygov_carrello_rpt " + CarrelloRpt.ALIAS +
      " inner join mygov_carrello_rp "+ CarrelloRp.ALIAS+" on "+
      CarrelloRp.ALIAS+".mygov_carrello_rp_id = "+ CarrelloRpt.ALIAS+".mygov_carrello_rp_id " +
      " where "+ CarrelloRpt.ALIAS+".mygov_carrello_rp_id = :id " )
  @RegisterFieldMapper(CarrelloRpt.class)
  Optional<CarrelloRpt> getByCarrelloRpId(Long id);
}
