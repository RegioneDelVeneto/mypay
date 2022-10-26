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
package it.regioneveneto.mygov.payment.mypay4.model.fesp;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import it.regioneveneto.mygov.payment.mypay4.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.Nested;

import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovCarrelloRptId")
public class CarrelloRpt extends BaseEntity {

  public final static String ALIAS = "FESP_CarrelloRpt";
  public final static String FIELDS = ""+ALIAS+".mygov_carrello_rpt_id as FESP_CarrelloRpt_mygovCarrelloRptId,"+ALIAS+".version as FESP_CarrelloRpt_version"+
      ","+ALIAS+".cod_ack_carrello_rpt as FESP_CarrelloRpt_codAckCarrelloRpt"+
      ","+ALIAS+".mygov_carrello_rp_id as FESP_CarrelloRpt_mygovCarrelloRpId"+
      ","+ALIAS+".dt_creazione as FESP_CarrelloRpt_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica as FESP_CarrelloRpt_dtUltimaModifica"+
      ","+ALIAS+".cod_rpt_inviacarrellorpt_id_carrello as FESP_CarrelloRpt_codRptInviacarrellorptIdCarrello"+
      ","+ALIAS+".cod_rpt_inviacarrellorpt_id_intermediario_pa as FESP_CarrelloRpt_codRptInviacarrellorptIdIntermediarioPa"+
      ","+ALIAS+".cod_rpt_inviacarrellorpt_id_stazione_intermediario_pa as FESP_CarrelloRpt_codRptInviacarrellorptIdStazioneIntermediarioPa"+
      ","+ALIAS+".de_rpt_inviacarrellorpt_password as FESP_CarrelloRpt_deRptInviacarrellorptPassword"+
      ","+ALIAS+".cod_rpt_inviacarrellorpt_id_psp as FESP_CarrelloRpt_codRptInviacarrellorptIdPsp"+
      ","+ALIAS+".cod_rpt_inviacarrellorpt_id_intermediario_psp as FESP_CarrelloRpt_codRptInviacarrellorptIdIntermediarioPsp"+
      ","+ALIAS+".cod_rpt_inviacarrellorpt_id_canale as FESP_CarrelloRpt_codRptInviacarrellorptIdCanale"+
      ","+ALIAS+".de_rpt_inviacarrellorpt_esito_complessivo_operazione as FESP_CarrelloRpt_deRptInviacarrellorptEsitoComplessivoOperazione"+
      ","+ALIAS+".cod_rpt_inviacarrellorpt_url as FESP_CarrelloRpt_codRptInviacarrellorptUrl"+
      ","+ALIAS+".cod_rpt_inviacarrellorpt_fault_code as FESP_CarrelloRpt_codRptInviacarrellorptFaultCode"+
      ","+ALIAS+".cod_rpt_inviacarrellorpt_fault_string as FESP_CarrelloRpt_codRptInviacarrellorptFaultString"+
      ","+ALIAS+".cod_rpt_inviacarrellorpt_id as FESP_CarrelloRpt_codRptInviacarrellorptId"+
      ","+ALIAS+".de_rpt_inviacarrellorpt_description as FESP_CarrelloRpt_deRptInviacarrellorptDescription"+
      ","+ALIAS+".num_rpt_inviacarrellorpt_serial as FESP_CarrelloRpt_numRptInviacarrellorptSerial"+
      ","+ALIAS+".cod_rpt_silinviacarrellorpt_original_fault_code as FESP_CarrelloRpt_codRptSilinviacarrellorptOriginalFaultCode"+
      ","+ALIAS+".de_rpt_silinviacarrellorpt_original_fault_string as FESP_CarrelloRpt_deRptSilinviacarrellorptOriginalFaultString"+
      ","+ALIAS+".de_rpt_silinviacarrellorpt_original_fault_description as FESP_CarrelloRpt_deRptSilinviacarrellorptOriginalFaultDescription";

  private Long mygovCarrelloRptId;
  private int version;
  private String codAckCarrelloRpt;
  @Nested(CarrelloRp.ALIAS)
  private CarrelloRp mygovCarrelloRpId;
  private Date dtCreazione;
  private Date dtUltimaModifica;
  private String codRptInviacarrellorptIdCarrello;
  private String codRptInviacarrellorptIdIntermediarioPa;
  private String codRptInviacarrellorptIdStazioneIntermediarioPa;
  private String deRptInviacarrellorptPassword;
  private String codRptInviacarrellorptIdPsp;
  private String codRptInviacarrellorptIdIntermediarioPsp;
  private String codRptInviacarrellorptIdCanale;
  private String deRptInviacarrellorptEsitoComplessivoOperazione;
  private String codRptInviacarrellorptUrl;
  private String codRptInviacarrellorptFaultCode;
  private String codRptInviacarrellorptFaultString;
  private String codRptInviacarrellorptId;
  private String deRptInviacarrellorptDescription;
  private Integer numRptInviacarrellorptSerial;
  private String codRptSilinviacarrellorptOriginalFaultCode;
  private String deRptSilinviacarrellorptOriginalFaultString;
  private String deRptSilinviacarrellorptOriginalFaultDescription;
}
