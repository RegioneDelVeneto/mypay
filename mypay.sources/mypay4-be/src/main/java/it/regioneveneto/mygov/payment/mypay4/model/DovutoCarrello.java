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

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovDovutoCarrelloId")
public class DovutoCarrello extends BaseEntity {

  public final static String ALIAS = "DovutoCarrello";
  public final static String FIELDS = ""+ALIAS+".mygov_dovuto_carrello_id as DovutoCarrello_mygovDovutoCarrelloId"+
      ","+ALIAS+".mygov_dovuto_id as DovutoCarrello_mygovDovutoId"+
      ","+ALIAS+".mygov_carrello_id as DovutoCarrello_mygovCarrelloId"+
      ","+ALIAS+".num_rp_dati_vers_dati_sing_vers_commissione_carico_pa as DovutoCarrello_numRpDatiVersDatiSingVersCommissioneCaricoPa"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_iban_accredito as DovutoCarrello_codRpDatiVersDatiSingVersIbanAccredito"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_bic_accredito as DovutoCarrello_codRpDatiVersDatiSingVersBicAccredito"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_iban_appoggio as DovutoCarrello_codRpDatiVersDatiSingVersIbanAppoggio"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_bic_appoggio as DovutoCarrello_codRpDatiVersDatiSingVersBicAppoggio"+
      ","+ALIAS+".cod_rp_dati_vers_dati_sing_vers_credenziali_pagatore as DovutoCarrello_codRpDatiVersDatiSingVersCredenzialiPagatore"+
      ","+ALIAS+".version as DovutoCarrello_version"+
      ","+ALIAS+".de_rp_dati_vers_dati_sing_vers_causale_versamento_agid as DovutoCarrello_deRpDatiVersDatiSingVersCausaleVersamentoAgid";

  private Long mygovDovutoCarrelloId;
  @Nested(Dovuto.ALIAS)
  private Dovuto mygovDovutoId;
  @Nested(Carrello.ALIAS)
  private Carrello mygovCarrelloId;
  private BigDecimal numRpDatiVersDatiSingVersCommissioneCaricoPa;
  private String codRpDatiVersDatiSingVersIbanAccredito;
  private String codRpDatiVersDatiSingVersBicAccredito;
  private String codRpDatiVersDatiSingVersIbanAppoggio;
  private String codRpDatiVersDatiSingVersBicAppoggio;
  private String codRpDatiVersDatiSingVersCredenzialiPagatore;
  private int version;
  private String deRpDatiVersDatiSingVersCausaleVersamentoAgid;
}
