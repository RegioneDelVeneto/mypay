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

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovRegistroOperazioneId")
public class RegistroOperazione extends BaseEntity {

  public enum TipoOperazione {
    ENTE_FUNZ, ENTE_TIP_DOV, OPER_TIP_DOV
  }

  public final static String ALIAS = "RegistroOperazione";
  public final static String FIELDS = ""+ALIAS+".mygov_registro_operazione_id as RegistroOperazione_mygovRegistroOperazioneId"+
      ","+ALIAS+".dt_operazione as RegistroOperazione_dtOperazione"+
      ","+ALIAS+".cod_fed_user_id_operatore as RegistroOperazione_codFedUserIdOperatore"+
      ","+ALIAS+".cod_tipo_operazione as RegistroOperazione_codTipoOperazione"+
      ","+ALIAS+".de_oggetto_operazione as RegistroOperazione_deOggettoOperazione"+
      ","+ALIAS+".cod_stato_bool as RegistroOperazione_codStatoBool";

  private Long mygovRegistroOperazioneId;
  private Timestamp dtOperazione;
  private String codFedUserIdOperatore;
  private TipoOperazione codTipoOperazione;
  private String deOggettoOperazione;
  private Boolean codStatoBool;
}
