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

import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.service.AnagraficaStatoService;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AnagraficaEnteTo extends EnteTo implements Serializable {

  public AnagraficaEnteTo(Ente ente) {
    super();
    this.setMygovEnteId(ente.getMygovEnteId());
    this.setCodIpaEnte(ente.getCodIpaEnte());
    this.setCodiceFiscaleEnte(ente.getCodiceFiscaleEnte());
    this.setDeNomeEnte(ente.getDeNomeEnte());
    this.setEmailAmministratore(ente.getEmailAmministratore());
    this.setDtUltimaModifica(Utilities.toLocalDateTime(ente.getDtUltimaModifica()));
    this.setCodRpDatiVersDatiSingVersIbanAccredito(ente.getCodRpDatiVersDatiSingVersIbanAccredito());
    this.setEnteSilInviaRispostaPagamentoUrl(ente.getEnteSilInviaRispostaPagamentoUrl());
    this.setDePassword(ente.getDePassword());
    this.setCodRpDatiVersDatiSingVersBicAccreditoSeller(ente.getCodRpDatiVersDatiSingVersBicAccreditoSeller());
    this.setDeRpEnteBenefDenominazioneBeneficiario(ente.getDeRpEnteBenefDenominazioneBeneficiario());
    this.setDeRpEnteBenefIndirizzoBeneficiario(ente.getDeRpEnteBenefIndirizzoBeneficiario());
    this.setDeRpEnteBenefCivicoBeneficiario(ente.getDeRpEnteBenefCivicoBeneficiario());
    this.setCodRpEnteBenefCapBeneficiario(ente.getCodRpEnteBenefCapBeneficiario());
    this.setDeRpEnteBenefLocalitaBeneficiario(ente.getDeRpEnteBenefLocalitaBeneficiario());
    this.setDeRpEnteBenefProvinciaBeneficiario(ente.getDeRpEnteBenefProvinciaBeneficiario());
    this.setCodRpEnteBenefNazioneBeneficiario(ente.getCodRpEnteBenefNazioneBeneficiario());
    this.setDeRpEnteBenefTelefonoBeneficiario(ente.getDeRpEnteBenefTelefonoBeneficiario());
    this.setDeRpEnteBenefSitoWebBeneficiario(ente.getDeRpEnteBenefSitoWebBeneficiario());
    this.setDeRpEnteBenefEmailBeneficiario(ente.getDeRpEnteBenefEmailBeneficiario());
    this.setApplicationCode(ente.getApplicationCode());
    this.setCodCodiceInterbancarioCbill(ente.getCodCodiceInterbancarioCbill());
    this.setDeInformazioniEnte(ente.getDeInformazioniEnte());
    this.setCdStatoEnte(Optional.of(ente.getCdStatoEnte())
        .map(AnagraficaStatoService::mapToDto)
        .orElse(null));
    this.setDeAutorizzazione(ente.getDeAutorizzazione());
    this.setCodTipoEnte(ente.getCodTipoEnte());
    this.setDtAvvio(ente.getDtAvvio());
    this.setLinguaAggiuntiva(ente.getLinguaAggiuntiva());
  }

  private Long mygovEnteId;
  private String codIpaEnte;
  private String codiceFiscaleEnte;
  private String deNomeEnte;
  private String emailAmministratore;
  private LocalDateTime dtUltimaModifica;
  private String codRpDatiVersDatiSingVersIbanAccredito;
  private String enteSilInviaRispostaPagamentoUrl;
  private String dePassword;
  private String paaSILInviaCarrelloDovutiHash;
  private Boolean codRpDatiVersDatiSingVersBicAccreditoSeller;
  private String deRpEnteBenefDenominazioneBeneficiario;
  private String deRpEnteBenefIndirizzoBeneficiario;
  private String deRpEnteBenefCivicoBeneficiario;
  private String codRpEnteBenefCapBeneficiario;
  private String deRpEnteBenefLocalitaBeneficiario;
  private String deRpEnteBenefProvinciaBeneficiario;
  private String codRpEnteBenefNazioneBeneficiario;
  private String deRpEnteBenefTelefonoBeneficiario;
  private String deRpEnteBenefSitoWebBeneficiario;
  private String deRpEnteBenefEmailBeneficiario;
  private String applicationCode;
  private String codCodiceInterbancarioCbill;
  private String deInformazioniEnte;
  private AnagraficaStatoTo cdStatoEnte;
  private String deAutorizzazione;
  private String codTipoEnte;
  private LocalDate dtAvvio;
  private String linguaAggiuntiva;
  private boolean flagInsertDefaultSet;
}
