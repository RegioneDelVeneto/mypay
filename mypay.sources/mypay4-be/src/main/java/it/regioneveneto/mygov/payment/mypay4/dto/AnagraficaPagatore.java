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
package it.regioneveneto.mygov.payment.mypay4.dto;

import it.regioneveneto.mygov.payment.mypay4.security.UserWithAdditionalInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnagraficaPagatore extends BaseTo implements Serializable {

  public enum TIPO {Pagatore, Versante}

  private char tipoIdentificativoUnivoco;
  private String codiceIdentificativoUnivoco;
  private String anagrafica;
  private String email;
  private String indirizzo;
  private String civico;
  private String cap;
  private String localita;
  private String provincia;
  private String nazione;
  private Long localitaId;
  private Long provinciaId;
  private Long nazioneId;

  public boolean checkSameCoreFields(UserWithAdditionalInfo user){
    return StringUtils.equalsIgnoreCase(user.getCodiceFiscale(), codiceIdentificativoUnivoco) &&
            StringUtils.equalsIgnoreCase(StringUtils.joinWith(" ",user.getFirstName(), user.getFamilyName()), anagrafica);
  }
}
