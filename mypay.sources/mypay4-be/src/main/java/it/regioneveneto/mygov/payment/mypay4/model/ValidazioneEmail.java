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

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "deEmailAddress")
public class ValidazioneEmail extends BaseEntity {

  public static final String ALIAS = "ValidazioneEmail";
  public static final String FIELDS = ""+ALIAS+".mygov_utente_id as ValidazioneEmail_mygovUtenteId,"+ALIAS+".codice as ValidazioneEmail_codice,"+
      ","+ALIAS+".dt_primo_invio as ValidazioneEmail_dtPrimoInvio,"+ALIAS+".dt_ultimo_invio as ValidazioneEmail_dtUltimoInvio"+
      ","+ALIAS+".num_invii as ValidazioneEmail_numInvii,"+ALIAS+".num_tentativi as ValidazioneEmail_numTentativi"+
      ","+ALIAS+".dt_ultimo_tentativo as ValidazioneEmail_dtUltimoTentativo";

  @Nested(Utente.ALIAS)
  private Utente mygovUtenteId;
  private String codice;
  private Timestamp dtPrimoInvio;
  private Timestamp dtUltimoInvio;
  private Integer numInvii;
  private Integer numTentativi;
  private Timestamp dtUltimoTentativo;
}
