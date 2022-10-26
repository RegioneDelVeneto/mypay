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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovSpontaneoAnonimoId")
public class SpontaneoAnonimo {

  public final static String ALIAS = "SpontaneoAnonimo";
  public final static String FIELDS = ""+ALIAS+".mygov_spontaneo_anonimo_id as SpontaneoAnonimo_mygovSpontaneoAnonimoId"+
      ","+ALIAS+".cod_ipa_ente as SpontaneoAnonimo_codIpaEnte,"+ALIAS+".cod_tipo_dovuto as SpontaneoAnonimo_codTipoDovuto"+
      ","+ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento as SpontaneoAnonimo_deRpDatiVersDatiSingVersCausaleVersamento"+
      ","+ALIAS+".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento as SpontaneoAnonimo_numRpDatiVersDatiSingVersImportoSingoloVersamento"+
      ","+ALIAS+".id_session as SpontaneoAnonimo_idSession,"+ALIAS+".de_email_address as SpontaneoAnonimo_deEmailAddress"+
      ","+ALIAS+".email_verificata as SpontaneoAnonimo_emailVerificata"+
      ","+ALIAS+".mygov_carrello_id as SpontaneoAnonimo_mygovCarrelloId,"+ALIAS+".dt_creazione as SpontaneoAnonimo_dtCreazione"+
      ","+ALIAS+".version as SpontaneoAnonimo_version"+
      ","+ALIAS+".de_causale_visualizzata as SpontaneoAnonimo_deCausaleVisualizzata"+
      ","+ALIAS+".de_bilancio as SpontaneoAnonimo_deBilancio";

  private Long mygovSpontaneoAnonimoId;
  private String codIpaEnte;
  private String codTipoDovuto;
  private String deRpDatiVersDatiSingVersCausaleVersamento;
  private Long numRpDatiVersDatiSingVersImportoSingoloVersamento;
  private String idSession;
  private String deEmailAddress;
  private boolean emailVerificata;
  @Nested(Carrello.ALIAS)
  private Carrello mygovCarrelloId;
  private Date dtCreazione;
  private int version;
  private String deCausaleVisualizzata;
  private String deBilancio;
}
