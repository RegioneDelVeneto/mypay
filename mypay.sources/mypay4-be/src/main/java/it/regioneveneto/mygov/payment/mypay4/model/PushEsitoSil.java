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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovPushEsitoSilId")
public class PushEsitoSil extends BaseEntity {

  public final static String ALIAS = "PushEsitoSil";
  public final static String FIELDS = ""+ALIAS+".mygov_push_esito_sil_id as PushEsitoSil_mygovPushEsitoSilId"+
      ","+ALIAS+".mygov_dovuto_elaborato_id as PushEsitoSil_mygovDovutoElaboratoId"+
      ","+ALIAS+".dt_creazione as PushEsitoSil_dtCreazione,"+ALIAS+".dt_ultimo_tentativo as PushEsitoSil_dtUltimoTentativo"+
      ","+ALIAS+".num_tentativi_effettuati as PushEsitoSil_numTentativiEffettuati"+
      ","+ALIAS+".flg_esito_invio_push as PushEsitoSil_flgEsitoInvioPush"+
      ","+ALIAS+".cod_esito_invio_fault_code as PushEsitoSil_codEsitoInvioFaultCode"+
      ","+ALIAS+".de_esito_invio_fault_description as PushEsitoSil_deEsitoInvioFaultDescription";

  private Long mygovPushEsitoSilId;
  @Nested(DovutoElaborato.ALIAS)
  private DovutoElaborato mygovDovutoElaboratoId;
  private Date dtCreazione;
  private Date dtUltimoTentativo;
  private Integer numTentativiEffettuati;
  private boolean flgEsitoInvioPush;
  private String codEsitoInvioFaultCode;
  private String deEsitoInvioFaultDescription;
}
