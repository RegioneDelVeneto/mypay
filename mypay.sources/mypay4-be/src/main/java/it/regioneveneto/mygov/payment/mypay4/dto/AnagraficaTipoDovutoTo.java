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

import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AnagraficaTipoDovutoTo extends BaseTo implements Serializable {

  public AnagraficaTipoDovutoTo(EnteTipoDovuto enteTipoDovuto) {
    super();
    this.setMygovEnteTipoDovutoId(enteTipoDovuto.getMygovEnteTipoDovutoId());
    this.setMygovEnteId(enteTipoDovuto.getMygovEnteId().getMygovEnteId());
    this.setCodIpaEnte(enteTipoDovuto.getMygovEnteId().getCodIpaEnte());
    this.setCodTipo(enteTipoDovuto.getCodTipo());
    this.setDeTipo(enteTipoDovuto.getDeTipo());
    this.setIbanAccreditoPi(enteTipoDovuto.getIbanAccreditoPi());
    this.setIbanAccreditoPsp(enteTipoDovuto.getIbanAccreditoPsp());
    this.setCodXsdCausale(enteTipoDovuto.getCodXsdCausale());
    this.setImporto(enteTipoDovuto.getImporto());
    this.setSpontaneo(enteTipoDovuto.isSpontaneo());
    this.setFlgCfAnonimo(enteTipoDovuto.isFlgCfAnonimo());
    this.setFlgScadenzaObbligatoria(enteTipoDovuto.isFlgScadenzaObbligatoria());
    this.setFlgStampaDataScadenza(enteTipoDovuto.isFlgStampaDataScadenza());
    this.setDeUrlPagamentoDovuto(enteTipoDovuto.getDeUrlPagamentoDovuto());
    this.setDeBilancioDefault(enteTipoDovuto.getDeBilancioDefault());
    this.setDeSettoreEnte(enteTipoDovuto.getDeSettoreEnte());
    this.setDeIntestatarioCcPostale(enteTipoDovuto.getDeIntestatarioCcPostale());
    this.setFlgNotificaIo(enteTipoDovuto.isFlgNotificaIo());
    this.setFlgDisabilitaStampaAvviso(enteTipoDovuto.isFlgDisabilitaStampaAvviso());
    this.setCodiceContestoPagamento(enteTipoDovuto.getCodiceContestoPagamento());
    this.setFlgNotificaEsitoPush(enteTipoDovuto.isFlgNotificaEsitoPush());
    this.setMaxTentativiInoltroEsito(enteTipoDovuto.getMaxTentativiInoltroEsito());
    this.setMacroArea(enteTipoDovuto.getMacroArea());
    this.setTipoServizio(enteTipoDovuto.getTipoServizio());
    this.setMotivoRiscossione(enteTipoDovuto.getMotivoRiscossione());
    this.setCodTassonomico(enteTipoDovuto.getCodTassonomico());
    this.setUrlNotificaPnd(enteTipoDovuto.getUrlNotificaPnd());
    this.setUserPnd(enteTipoDovuto.getUserPnd());
    this.setPswPnd(enteTipoDovuto.getPswPnd());
    this.setUrlNotificaAttualizzazionePnd(enteTipoDovuto.getUrlNotificaAttualizzazionePnd());
  }

  private Long mygovEnteTipoDovutoId;
  private Long mygovEnteId;
  private String codIpaEnte;
  private String codTipo;
  private String deTipo;
  private String ibanAccreditoPi;
  private String ibanAccreditoPsp;
  private String codXsdCausale;
  private BigDecimal importo;
  private boolean spontaneo;
  private boolean flgCfAnonimo;
  private boolean flgScadenzaObbligatoria;
  private boolean flgStampaDataScadenza;
  private String deUrlPagamentoDovuto;
  private String deBilancioDefault;
  private String deSettoreEnte;
  private String deIntestatarioCcPostale;
  private boolean flgNotificaIo;
  private boolean flgDisabilitaStampaAvviso;
  private String codiceContestoPagamento;
  private boolean flgNotificaEsitoPush;
  private Integer maxTentativiInoltroEsito;
  private String macroArea;
  private String tipoServizio;
  private String motivoRiscossione;
  private String codTassonomico;
  
  // Data in mygov_ente_sil
  private String nomeApplicativo;
  private String deUrlInoltroEsitoPagamentoPush;
  private String codServiceAccountJwtUscitaClientId;
  private String deServiceAccountJwtUscitaClientMail;
  private String codServiceAccountJwtUscitaSecretKeyId;
  private String codServiceAccountJwtUscitaSecretKey;
  private boolean flgJwtAttivo;

  private String urlNotificaPnd;
  private String userPnd;
  private String pswPnd;
  private String urlNotificaAttualizzazionePnd;
}
