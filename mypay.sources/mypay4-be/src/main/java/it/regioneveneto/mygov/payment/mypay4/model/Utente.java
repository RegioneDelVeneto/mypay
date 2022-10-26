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
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovUtenteId")
public class Utente extends BaseEntity {

  public final static char EMAIL_SOURCE_TYPE_BACKOFFICE = 'B';

  public enum EMAIL_SOURCE_TYPES {
    AUTH_SYSTEM('A'),
    BACKOFFICE(EMAIL_SOURCE_TYPE_BACKOFFICE),
    BACKOFFICE_CONFIRMED('C'),
    USER_VALIDATED('V');

    private final char asChar;
    public char asChar() {
      return asChar;
    }
    public static EMAIL_SOURCE_TYPES fromChar(char asChar){
      return Arrays.stream(EMAIL_SOURCE_TYPES.values())
          .filter(x -> x.asChar() == asChar)
          .findFirst()
          .orElse(null);
    }
    public static boolean isValid(char asChar){
      return Arrays.stream(EMAIL_SOURCE_TYPES.values())
          .anyMatch(x -> x.asChar() == asChar);
    }
    EMAIL_SOURCE_TYPES(char asChar) {
      this.asChar = asChar;
    }
  }

  public final static String ALIAS = "Utente";
  public final static String FIELDS = ""+ALIAS+".mygov_utente_id as Utente_mygovUtenteId,"+ALIAS+".version as Utente_version"+
      ","+ALIAS+".cod_fed_user_id as Utente_codFedUserId,"+ALIAS+".cod_codice_fiscale_utente as Utente_codCodiceFiscaleUtente"+
      ","+ALIAS+".flg_fed_authorized as Utente_flgFedAuthorized,"+ALIAS+".de_email_address as Utente_deEmailAddress"+
      ","+ALIAS+".de_firstname as Utente_deFirstname,"+ALIAS+".de_lastname as Utente_deLastname"+
      ","+ALIAS+".de_fed_legal_entity as Utente_deFedLegalEntity,"+ALIAS+".dt_ultimo_login as Utente_dtUltimoLogin"+
      ","+ALIAS+".indirizzo as Utente_indirizzo,"+ALIAS+".civico as Utente_civico,"+ALIAS+".cap as Utente_cap"+
      ","+ALIAS+".comune_id as Utente_comuneId,"+ALIAS+".provincia_id as Utente_provinciaId"+
      ","+ALIAS+".nazione_id as Utente_nazioneId,"+ALIAS+".dt_set_address as Utente_dtSetAddress"+
      ","+ALIAS+".email_source_type as Utente_emailSourceType,"+ALIAS+".de_email_address_new as Utente_deEmailAddressNew";

  private Long mygovUtenteId;
  private int version;
  private String codFedUserId;
  private String codCodiceFiscaleUtente;
  private boolean flgFedAuthorized;
  private String deEmailAddress;
  private String deFirstname;
  private String deLastname;
  private String deFedLegalEntity;
  private Date dtUltimoLogin;
  private String indirizzo;
  private String civico;
  private String cap;
  private Long comuneId;
  private Long provinciaId;
  private Long nazioneId;
  private Date dtSetAddress;
  private char emailSourceType;
  private String deEmailAddressNew;

  public boolean isChosenByUserEmail(){
    return StringUtils.isNotBlank(deEmailAddress) &&
        (emailSourceType==EMAIL_SOURCE_TYPES.USER_VALIDATED.asChar() ||
            emailSourceType==EMAIL_SOURCE_TYPES.BACKOFFICE.asChar() ||
            emailSourceType==EMAIL_SOURCE_TYPES.BACKOFFICE_CONFIRMED.asChar());
  }

  public boolean isEmailValidationOngoing(){
    return StringUtils.isNotBlank(deEmailAddressNew);
  }
}
