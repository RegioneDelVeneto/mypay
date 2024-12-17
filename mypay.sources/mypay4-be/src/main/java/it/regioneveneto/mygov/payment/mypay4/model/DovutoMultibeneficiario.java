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

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovDovutoMultibeneficiarioId")
public class DovutoMultibeneficiario extends BaseEntity {

  // Special handling for field località(=comune): truncate if > 35 char (i.e. max length on DB)
  //   This special handling is due to località selected by select list nazione/provincia/comune and on list of comuni
  //   there are some values whose length > 35. The choice of 35 chars is due to max length of corresponding field
  //   on PagoPA api.
  public static final int MAX_LENGTH_LOCALITA = 35;

  public static final String ALIAS = "DovutoMultibeneficiario";
  public static final String FIELDS =
    "" + ALIAS + ".mygov_DovutoMultibeneficiario_id as DovutoMultibeneficiario_mygovDovutoMultibeneficiarioId" +
      "," + ALIAS + ".cod_iud as DovutoMultibeneficiario_codIud" +
      "," + ALIAS + ".cod_iuv as DovutoMultibeneficiario_codIuv" +
      "," + ALIAS + ".codice_fiscale_ente as DovutoMultibeneficiario_codiceFiscaleEnte" +
      "," + ALIAS + ".de_rp_DovutoMultibeneficiario_benef_denominazione_beneficiario as DovutoMultibeneficiario_deRpEnteBenefDenominazioneBeneficiario" +
      "," + ALIAS + ".cod_rp_dati_vers_dati_sing_vers_iban_accredito as DovutoMultibeneficiario_codRpDatiVersDatiSingVersIbanAccredito" +
      "," + ALIAS + ".de_rp_DovutoMultibeneficiario_benef_indirizzo_beneficiario as DovutoMultibeneficiario_deRpEnteBenefIndirizzoBeneficiario" +
      "," + ALIAS + ".de_rp_DovutoMultibeneficiario_benef_civico_beneficiario as DovutoMultibeneficiario_deRpEnteBenefCivicoBeneficiario" +
      "," + ALIAS + ".cod_rp_DovutoMultibeneficiario_benef_cap_beneficiario as DovutoMultibeneficiario_codRpEnteBenefCapBeneficiario" +
      "," + ALIAS + ".de_rp_DovutoMultibeneficiario_benef_localita_beneficiario as DovutoMultibeneficiario_deRpEnteBenefLocalitaBeneficiario" +
      "," + ALIAS + ".de_rp_DovutoMultibeneficiario_benef_provincia_beneficiario as DovutoMultibeneficiario_deRpEnteBenefProvinciaBeneficiario" +
      "," + ALIAS + ".cod_rp_DovutoMultibeneficiario_benef_nazione_beneficiario as DovutoMultibeneficiario_codRpEnteBenefNazioneBeneficiario" +
      "," + ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento as DovutoMultibeneficiario_numRpDatiVersDatiSingVersImportoSingoloVersamento" +
      "," + ALIAS + ".mygov_Dovuto_id as Dovuto_mygovDovutoId" +
      "," + ALIAS + ".dt_creazione as DovutoMultibeneficiario_dtCreazione" +
      "," + ALIAS + ".dt_ultima_modifica as DovutoMultibeneficiario_dtUltimaModifica" +
      "," + ALIAS + ".de_rp_dati_vers_dati_sing_vers_causale_versamento as DovutoMultibeneficiario_deRpDatiVersDatiSingVersCausaleVersamento" +
      "," + ALIAS + ".de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione as DovutoMultibeneficiario_deRpDatiVersDatiSingVersDatiSpecificiRiscossione";


  private Long mygovDovutoMultibeneficiarioId;
  private String codIud;
  private String codIuv;
  private String codiceFiscaleEnte;
  private String deRpEnteBenefDenominazioneBeneficiario;
  private String codRpDatiVersDatiSingVersIbanAccredito;
  private String deRpEnteBenefIndirizzoBeneficiario;
  private String deRpEnteBenefCivicoBeneficiario;
  private String codRpEnteBenefCapBeneficiario;
  private String deRpEnteBenefLocalitaBeneficiario;
  private String deRpEnteBenefProvinciaBeneficiario;
  private String codRpEnteBenefNazioneBeneficiario;
  private BigDecimal numRpDatiVersDatiSingVersImportoSingoloVersamento;
  @Nested(Dovuto.ALIAS)
  private Dovuto mygovDovutoId;
  private Date dtCreazione;
  private Date dtUltimaModifica;
  private String deRpDatiVersDatiSingVersCausaleVersamento;
  private String deRpDatiVersDatiSingVersDatiSpecificiRiscossione;
}
