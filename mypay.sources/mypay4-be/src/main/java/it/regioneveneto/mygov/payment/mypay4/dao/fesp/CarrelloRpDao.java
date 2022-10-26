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
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface CarrelloRpDao extends BaseDao {

  @SqlQuery("select " + CarrelloRp.ALIAS + ALL_FIELDS +
      " from mygov_carrello_rp " + CarrelloRp.ALIAS +
      " where "+ CarrelloRp.ALIAS+".mygov_carrello_rpt_id = :id " )
  @RegisterFieldMapper(CarrelloRp.class)
  Optional<CarrelloRp> getById(Long id);

  @SqlQuery("select " + CarrelloRp.ALIAS + ALL_FIELDS +
      "  from mygov_carrello_rp " + CarrelloRp.ALIAS +
      " where "+ CarrelloRp.ALIAS+".id_session_carrello = :idSession ")
  @RegisterFieldMapper(CarrelloRp.class)
  Optional<CarrelloRp> getByIdSession(String idSession);
  
  @SqlUpdate(" insert into mygov_carrello_rp (" +
      "  mygov_carrello_rp_id , version , cod_ack_carrello_rp , dt_creazione , dt_ultima_modifica "+
      ", id_session_carrello , de_rp_silinviacarrellorp_esito , cod_rp_silinviacarrellorp_redirect "+
      ", cod_rp_silinviacarrellorp_url , cod_rp_silinviacarrellorp_fault_code , de_rp_silinviacarrellorp_fault_string "+
      ", cod_rp_silinviacarrellorp_id , de_rp_silinviacarrellorp_description , codice_fiscale_ente "+
      ", cod_rp_silinviacarrellorp_serial , cod_rp_silinviacarrellorp_original_fault_code "+
      ", de_rp_silinviacarrellorp_original_fault_string " +
      ", de_rp_silinviacarrellorp_original_fault_description ) values ( " +
      " nextval('mygov_carrello_rp_id_seq') "+
      " , :c.version" +
      " , :c.codAckCarrelloRp" +
      " , :c.dtCreazione" +
      " , :c.dtUltimaModifica" +
      " , :c.idSessionCarrello" +
      " , :c.deRpSilinviacarrellorpEsito" +
      " , :c.codRpSilinviacarrellorpRedirect" +
      " , :c.codRpSilinviacarrellorpUrl" +
      " , :c.codRpSilinviacarrellorpFaultCode" +
      " , :c.deRpSilinviacarrellorpFaultString" +
      " , :c.codRpSilinviacarrellorpId" +
      " , :c.deRpSilinviacarrellorpDescription" +
      " , :c.codiceFiscaleEnte" +
      " , :c.codRpSilinviacarrellorpSerial" +
      " , :c.codRpSilinviacarrellorpOriginalFaultCode" +
      " , :c.deRpSilinviacarrellorpOriginalFaultString" +
      " , :c.deRpSilinviacarrellorpOriginalFaultDescription );"
  )
  @GetGeneratedKeys("mygov_carrello_rp_id")
  long insert(@BindBean("c") CarrelloRp c);

  @SqlUpdate("update mygov_carrello_rp set "+
      " dt_ultima_modifica = :c.dtUltimaModifica"+
      ", id_session_carrello = :c.idSessionCarrello"+
      ", de_rp_silinviacarrellorp_esito = :c.deRpSilinviacarrellorpEsito"+
      ", cod_rp_silinviacarrellorp_url = :c.codRpSilinviacarrellorpUrl"+
      ", cod_rp_silinviacarrellorp_fault_code = :c.codRpSilinviacarrellorpFaultCode"+
      ", de_rp_silinviacarrellorp_fault_string = :c.deRpSilinviacarrellorpFaultString"+
      ", cod_rp_silinviacarrellorp_id = :c.codRpSilinviacarrellorpId"+
      ", cod_rp_silinviacarrellorp_serial = :c.codRpSilinviacarrellorpSerial"+
      ", cod_rp_silinviacarrellorp_original_fault_code = :c.codRpSilinviacarrellorpOriginalFaultCode"+
      ", de_rp_silinviacarrellorp_original_fault_string = :c.deRpSilinviacarrellorpOriginalFaultString"+
      ", de_rp_silinviacarrellorp_original_fault_description = :c.deRpSilinviacarrellorpOriginalFaultDescription"+
          " where mygov_carrello_rp_id = :c.mygovCarrelloRpId ")
  int update(@BindBean("c") CarrelloRp c);
}
