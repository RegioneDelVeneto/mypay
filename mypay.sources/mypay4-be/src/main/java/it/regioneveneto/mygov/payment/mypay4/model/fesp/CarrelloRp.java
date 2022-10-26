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

import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovCarrelloRpId")
public class CarrelloRp extends BaseEntity {

  public final static String ALIAS = "FESP_CarrelloRp";
  public final static String FIELDS = ""+ALIAS+".mygov_carrello_rp_id as FESP_CarrelloRp_mygovCarrelloRpId,"+ALIAS+".version as FESP_CarrelloRp_version"+
      ","+ALIAS+".cod_ack_carrello_rp as FESP_CarrelloRp_codAckCarrelloRp"+
      ","+ALIAS+".dt_creazione as FESP_CarrelloRp_dtCreazione,"+ALIAS+".dt_ultima_modifica as FESP_CarrelloRp_dtUltimaModifica"+
      ","+ALIAS+".id_session_carrello as FESP_CarrelloRp_idSessionCarrello"+
      ","+ALIAS+".de_rp_silinviacarrellorp_esito as FESP_CarrelloRp_deRpSilinviacarrellorpEsito"+
      ","+ALIAS+".cod_rp_silinviacarrellorp_redirect as FESP_CarrelloRp_codRpSilinviacarrellorpRedirect"+
      ","+ALIAS+".cod_rp_silinviacarrellorp_url as FESP_CarrelloRp_codRpSilinviacarrellorpUrl"+
      ","+ALIAS+".cod_rp_silinviacarrellorp_fault_code as FESP_CarrelloRp_codRpSilinviacarrellorpFaultCode"+
      ","+ALIAS+".de_rp_silinviacarrellorp_fault_string as FESP_CarrelloRp_deRpSilinviacarrellorpFaultString"+
      ","+ALIAS+".cod_rp_silinviacarrellorp_id as FESP_CarrelloRp_codRpSilinviacarrellorpId"+
      ","+ALIAS+".de_rp_silinviacarrellorp_description as FESP_CarrelloRp_deRpSilinviacarrellorpDescription"+
      ","+ALIAS+".codice_fiscale_ente as FESP_CarrelloRp_codiceFiscaleEnte"+
      ","+ALIAS+".cod_rp_silinviacarrellorp_serial as FESP_CarrelloRp_codRpSilinviacarrellorpSerial"+
      ","+ALIAS+".cod_rp_silinviacarrellorp_original_fault_code as FESP_CarrelloRp_codRpSilinviacarrellorpOriginalFaultCode"+
      ","+ALIAS+".de_rp_silinviacarrellorp_original_fault_string as FESP_CarrelloRp_deRpSilinviacarrellorpOriginalFaultString"+
      ","+ALIAS+".de_rp_silinviacarrellorp_original_fault_description as FESP_CarrelloRp_deRpSilinviacarrellorpOriginalFaultDescription";

  private Long mygovCarrelloRpId;
  private int version;
  private String codAckCarrelloRp;
  private Date dtCreazione;
  private Date dtUltimaModifica;
  private String idSessionCarrello;
  private String deRpSilinviacarrellorpEsito;
  private Integer codRpSilinviacarrellorpRedirect;
  private String codRpSilinviacarrellorpUrl;
  private String codRpSilinviacarrellorpFaultCode;
  private String deRpSilinviacarrellorpFaultString;
  private String codRpSilinviacarrellorpId;
  private String deRpSilinviacarrellorpDescription;
  private String codiceFiscaleEnte;
  private Integer codRpSilinviacarrellorpSerial;
  private String codRpSilinviacarrellorpOriginalFaultCode;
  private String deRpSilinviacarrellorpOriginalFaultString;
  private String deRpSilinviacarrellorpOriginalFaultDescription;
}
