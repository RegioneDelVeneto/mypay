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
package it.regioneveneto.mygov.payment.mypay4.ws.impl.fesp;

import gov.telematici.pagamenti.ws.CtFaultBean;
import gov.telematici.pagamenti.ws.StEsitoOperazione;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.EsitoAvvisoDigitaleCompletoDto;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.EsitoAvvisoDigitaleDto;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.NodoInviaAvvisoDigitaleService;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciAvvisiDigitali;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.veneto.regione.pagamenti.nodoregionalefesp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("PagamentiTelematiciAvvisiDigitaliImpl")
@Slf4j
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class PagamentiTelematiciAvvisiDigitaliImpl implements PagamentiTelematiciAvvisiDigitali {

  @Autowired
  private NodoInviaAvvisoDigitaleService nodoInviaAvvisoDigitaleService;

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public NodoSILInviaAvvisoDigitaleRisposta nodoSILInviaAvvisoDigitale(NodoSILInviaAvvisoDigitale bodyrichiesta, IntestazionePPT header) {
    log.info("Executing operation nodoSILInviaAvvisoDigitale");

    try {

      String identificativoDominioHeader = header.getIdentificativoDominio();
      String identificativoIntermediarioPAHeader = header.getIdentificativoIntermediarioPA();
      String identificativoStazioneIntermediarioPAHeader = header.getIdentificativoStazioneIntermediarioPA();

      CtAvvisoDigitale avvisoDigitale = bodyrichiesta.getAvvisoDigitaleWS();

      EsitoAvvisoDigitaleCompletoDto responseDto = nodoInviaAvvisoDigitaleService.nodoInviaAvvisoDigitale(
          identificativoDominioHeader, identificativoIntermediarioPAHeader,
          identificativoStazioneIntermediarioPAHeader, avvisoDigitale.getIdentificativoDominio(),
          avvisoDigitale.getAnagraficaBeneficiario(), avvisoDigitale.getIdentificativoMessaggioRichiesta(),
          avvisoDigitale.getTassonomiaAvviso(), avvisoDigitale.getCodiceAvviso(),
          avvisoDigitale.getSoggettoPagatore().getAnagraficaPagatore(),
          avvisoDigitale.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco(),
          avvisoDigitale.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString(),
          avvisoDigitale.getDataScadenzaPagamento(), avvisoDigitale.getDataScadenzaAvviso(),
          avvisoDigitale.getImportoAvviso(), avvisoDigitale.getEMailSoggetto(),
          avvisoDigitale.getCellulareSoggetto(), avvisoDigitale.getDescrizionePagamento(),
          avvisoDigitale.getUrlAvviso(), avvisoDigitale.getDatiSingoloVersamentos().get(0).getIbanAccredito(),
          avvisoDigitale.getDatiSingoloVersamentos().get(0).getIbanAppoggio(), avvisoDigitale.getTipoPagamento(),
          avvisoDigitale.getTipoOperazione().value());

      if (responseDto.getFaultBean() != null) {
        // RESPONSE FAULT BEAN
        NodoSILInviaAvvisoDigitaleRisposta response = new NodoSILInviaAvvisoDigitaleRisposta();
        CtFaultBean faultBean = new CtFaultBean();
        faultBean.setId(responseDto.getFaultBean().getId());
        faultBean.setFaultCode(responseDto.getFaultBean().getFaultCode());
        faultBean.setFaultString(responseDto.getFaultBean().getFaultString());
        faultBean.setDescription(responseDto.getFaultBean().getDescription());
        faultBean.setSerial(responseDto.getFaultBean().getSerial());
        response.setFault(faultBean);
        return response;
      } else {
        // RESPONSE
        NodoSILInviaAvvisoDigitaleRisposta response = new NodoSILInviaAvvisoDigitaleRisposta();

        StEsitoOperazione stEsitoOperazione = StEsitoOperazione.fromValue(responseDto.getEsitoOperazione());

        CtEsitoAvvisoDigitale ctEsitoAvvisoDigitale = new CtEsitoAvvisoDigitale();
        ctEsitoAvvisoDigitale.setIdentificativoDominio(responseDto.getIdentificativoDominio());
        ctEsitoAvvisoDigitale.setIdentificativoMessaggioRichiesta(responseDto.getIdentificativoMessaggioRichiesta());

        for(EsitoAvvisoDigitaleDto esitoDto : responseDto.getListaEsitiAvvisiDigitali()) {
          CtEsitoAvvisatura esitoAvvisatura = new CtEsitoAvvisatura();
          esitoAvvisatura.setTipoCanaleEsito(esitoDto.getTipoCanaleEsito());
          esitoAvvisatura.setIdentificativoCanale(esitoDto.getIdentificativoCanale());
          esitoAvvisatura.setDataEsito(esitoDto.getDataEsito());
          esitoAvvisatura.setCodiceEsito(esitoDto.getCodiceEsito());
          esitoAvvisatura.setDescrizioneEsito(esitoDto.getDescrizioneEsito());
          ctEsitoAvvisoDigitale.getEsitoAvvisaturas().add(esitoAvvisatura);
        }

        response.setEsitoOperazione(stEsitoOperazione);
        response.setEsitoAvvisoDigitaleWS(ctEsitoAvvisoDigitale);
        return response;
      }
    } catch (Exception ex) {
      log.error(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [" + ex.getMessage() + "]", ex);

      CtFaultBean fault = new CtFaultBean();
      fault.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
      fault.setSerial(1);
      fault.setId(FaultCodeConstants.PAA_NODO_SIL_INVIA_AVVISO_DIGITALE);
      fault.setDescription(StringUtils.abbreviate(ex.getMessage(), 255));
      fault.setFaultString(fault.getDescription());

      NodoSILInviaAvvisoDigitaleRisposta response = new NodoSILInviaAvvisoDigitaleRisposta();
      response.setFault(fault);

      return response;
    }
  }
}
