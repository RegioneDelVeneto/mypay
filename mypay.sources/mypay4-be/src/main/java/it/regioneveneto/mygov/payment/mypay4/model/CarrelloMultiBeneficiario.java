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
package it.regioneveneto.mygov.payment.mypay4.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovCarrelloMultiBeneficiarioId")
public class CarrelloMultiBeneficiario extends BaseEntity{

  public static final String ALIAS = "CarrelloMultiBeneficiario";
  public static final String FIELDS = ""+ALIAS+".mygov_carrello_multi_beneficiario_id as CarrelloMultiBeneficiario_mygovCarrelloMultiBeneficiarioId"+
      ","+ALIAS+".version as CarrelloMultiBeneficiario_version"+
      ","+ALIAS+".mygov_anagrafica_stato_id as CarrelloMultiBeneficiario_mygovAnagraficaStatoId"+
      ","+ALIAS+".cod_ipa_ente as CarrelloMultiBeneficiario_codIpaEnte"+
      ","+ALIAS+".cod_ack_carrello_rp as CarrelloMultiBeneficiario_codAckCarrelloRp"+
      ","+ALIAS+".dt_creazione as CarrelloMultiBeneficiario_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica as CarrelloMultiBeneficiario_dtUltimaModifica"+
      ","+ALIAS+".id_session_carrello as CarrelloMultiBeneficiario_idSessionCarrello"+
      ","+ALIAS+".id_session_carrellofesp as CarrelloMultiBeneficiario_idSessionCarrellofesp"+
      ","+ALIAS+".risposta_pagamento_url as CarrelloMultiBeneficiario_rispostaPagamentoUrl"+
      ","+ALIAS+".de_rp_silinviacarrellorp_esito as CarrelloMultiBeneficiario_deRpSilinviacarrellorpEsito"+
      ","+ALIAS+".cod_rp_silinviacarrellorp_url as CarrelloMultiBeneficiario_codRpSilinviacarrellorpUrl"+
      ","+ALIAS+".cod_rp_silinviacarrellorp_fault_code as CarrelloMultiBeneficiario_codRpSilinviacarrellorpFaultCode"+
      ","+ALIAS+".de_rp_silinviacarrellorp_fault_string as CarrelloMultiBeneficiario_deRpSilinviacarrellorpFaultString"+
      ","+ALIAS+".cod_rp_silinviacarrellorp_id as CarrelloMultiBeneficiario_codRpSilinviacarrellorpId"+
      ","+ALIAS+".de_rp_silinviacarrellorp_description as CarrelloMultiBeneficiario_deRpSilinviacarrellorpDescription"+
      ","+ALIAS+".cod_rp_silinviacarrellorp_serial as CarrelloMultiBeneficiario_codRpSilinviacarrellorpSerial"+
      ","+ALIAS+".cod_rp_silinviacarrellorp_original_fault_code as CarrelloMultiBeneficiario_codRpSilinviacarrellorpOriginalFaultCode"+
      ","+ALIAS+".de_rp_silinviacarrellorp_original_fault_string as CarrelloMultiBeneficiario_deRpSilinviacarrellorpOriginalFaultString"+
      ","+ALIAS+".de_rp_silinviacarrellorp_original_fault_description as CarrelloMultiBeneficiario_deRpSilinviacarrellorpOriginalFaultDescription";

  private Long mygovCarrelloMultiBeneficiarioId;
  private int version;
  @Nested(AnagraficaStato.ALIAS)
  private AnagraficaStato mygovAnagraficaStatoId;
  private String codIpaEnte;
  private String codAckCarrelloRp;
  private Date dtCreazione;
  private Date dtUltimaModifica;
  private String idSessionCarrello;
  private String idSessionCarrellofesp;
  private String rispostaPagamentoUrl;
  private String deRpSilinviacarrellorpEsito;
  private String codRpSilinviacarrellorpUrl;
  private String codRpSilinviacarrellorpFaultCode;
  private String deRpSilinviacarrellorpFaultString;
  private String codRpSilinviacarrellorpId;
  private String deRpSilinviacarrellorpDescription;
  private Integer codRpSilinviacarrellorpSerial;
  private String codRpSilinviacarrellorpOriginalFaultCode;
  private String deRpSilinviacarrellorpOriginalFaultString;
  private String deRpSilinviacarrellorpOriginalFaultDescription;

}
