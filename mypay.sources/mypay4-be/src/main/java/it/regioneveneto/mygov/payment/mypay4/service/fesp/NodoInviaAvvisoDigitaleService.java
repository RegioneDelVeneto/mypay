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
package it.regioneveneto.mygov.payment.mypay4.service.fesp;

import gov.telematici.pagamenti.ws.*;
import gov.telematici.pagamenti.ws.sachead.IntestazionePPT;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.EsitoAvvisoDigitaleCompletoDto;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.EsitoAvvisoDigitaleDto;
import it.regioneveneto.mygov.payment.mypay4.ws.client.fesp.PagamentiTelematiciAvvisiDigitaliServiceClient;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
public class NodoInviaAvvisoDigitaleService {

  @Autowired
  private PagamentiTelematiciAvvisiDigitaliServiceClient pagamentiTelematiciAvvisiDigitaliServiceClient;

  public EsitoAvvisoDigitaleCompletoDto nodoInviaAvvisoDigitale(String identificativoDominioHeader,
                                                                String identificativoIntermediarioPAHeader, String identificativoStazioneIntermediarioPAHeader,
                                                                String identificativoDominio, String anagraficaBeneficiario, String identificativoMessaggioRichiesta,
                                                                String tassonomiaAvviso, String codiceAvviso, String anagraficaPagatore, String codiceIdentificativoUnivoco,
                                                                String tipoIdentificativoUnivoco, XMLGregorianCalendar dataScadenzaPagamento,
                                                                XMLGregorianCalendar dataScadenzaAvviso, BigDecimal importoAvviso, String eMailSoggetto,
                                                                String cellulareSoggetto, String descrizionePagamento, String urlAvviso, String ibanAccredito, String ibanAppoggio,
                                                                String tipoPagamento, String tipoOperazione) {
    /*
     * Creazione header
     */
    IntestazionePPT header = new IntestazionePPT();
    header.setIdentificativoDominio(identificativoDominioHeader);
    header.setIdentificativoIntermediarioPA(identificativoIntermediarioPAHeader);
    header.setIdentificativoStazioneIntermediarioPA(identificativoStazioneIntermediarioPAHeader);

    /*
     * Creazione request
     */
    NodoInviaAvvisoDigitale request = new NodoInviaAvvisoDigitale();
    CtAvvisoDigitale avvisoDigitale = new CtAvvisoDigitale();
    avvisoDigitale.setIdentificativoDominio(identificativoDominio);
    avvisoDigitale.setAnagraficaBeneficiario(anagraficaBeneficiario);
    avvisoDigitale.setIdentificativoMessaggioRichiesta(identificativoMessaggioRichiesta);
    avvisoDigitale.setTassonomiaAvviso(tassonomiaAvviso);
    avvisoDigitale.setCodiceAvviso(codiceAvviso);

    CtSoggettoPagatore soggPag = new CtSoggettoPagatore();
    soggPag.setAnagraficaPagatore(anagraficaPagatore);
    CtIdentificativoUnivocoPersonaFG identificativoUnivocoPersona = new CtIdentificativoUnivocoPersonaFG();
    identificativoUnivocoPersona.setCodiceIdentificativoUnivoco(codiceIdentificativoUnivoco);
    StTipoIdentificativoUnivocoPersFG tipo = StTipoIdentificativoUnivocoPersFG.fromValue(tipoIdentificativoUnivoco);
    identificativoUnivocoPersona.setTipoIdentificativoUnivoco(tipo);
    soggPag.setIdentificativoUnivocoPagatore(identificativoUnivocoPersona);
    avvisoDigitale.setSoggettoPagatore(soggPag);

    avvisoDigitale.setDataScadenzaPagamento(dataScadenzaPagamento);
    avvisoDigitale.setDataScadenzaAvviso(dataScadenzaAvviso);
    avvisoDigitale.setImportoAvviso(importoAvviso);
    avvisoDigitale.setEMailSoggetto(eMailSoggetto);
    avvisoDigitale.setCellulareSoggetto(cellulareSoggetto);
    avvisoDigitale.setDescrizionePagamento(descrizionePagamento);
    avvisoDigitale.setUrlAvviso(urlAvviso);

    CtDatiSingoloVersamento ctDtSngPag = new CtDatiSingoloVersamento();
    ctDtSngPag.setIbanAccredito(ibanAccredito);
    ctDtSngPag.setIbanAppoggio(ibanAppoggio);
    avvisoDigitale.getDatiSingoloVersamentos().add(ctDtSngPag);

    avvisoDigitale.setTipoPagamento(tipoPagamento);

    StTipoOperazione ope = StTipoOperazione.fromValue(tipoOperazione);
    avvisoDigitale.setTipoOperazione(ope);

    request.setAvvisoDigitaleWS(avvisoDigitale);

    request.setPassword("");

    try{
      NodoInviaAvvisoDigitaleRisposta response = pagamentiTelematiciAvvisiDigitaliServiceClient
          .nodoInviaAvvisoDigitale(request, header);
      return mapXMLResponseToDto(header, response);
    }catch(Exception ex) {
      log.error(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [" + ex.getMessage() + "]", ex);
      throw ex;
    }
  }

  private EsitoAvvisoDigitaleCompletoDto mapXMLResponseToDto(IntestazionePPT header, NodoInviaAvvisoDigitaleRisposta response) {
    EsitoAvvisoDigitaleCompletoDto dto = new EsitoAvvisoDigitaleCompletoDto();

    // HEADER
    dto.setIdentificativoDominioHeader(header.getIdentificativoDominio());
    dto.setIdentificativoIntermediarioPAHeader(header.getIdentificativoIntermediarioPA());
    dto.setIdentificativoStazioneIntermediarioPAHeader(header.getIdentificativoStazioneIntermediarioPA());

    // ESITO OPERAZIONE
    dto.setEsitoOperazione(response.getEsitoOperazione().value());

    // FAULT BEAN
    if (response.getFault() != null) {
      it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean faultDto = new it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean();
      faultDto.setId(response.getFault().getId());
      faultDto.setFaultCode(response.getFault().getFaultCode());
      faultDto.setFaultString(response.getFault().getFaultString());
      faultDto.setDescription(response.getFault().getDescription());
      faultDto.setSerial(response.getFault().getSerial());
      dto.setFaultBean(faultDto);
    }else{
      // ESITO AVVISO
      dto.setIdentificativoDominio(response.getEsitoAvvisoDigitaleWS().getIdentificativoDominio());
      dto.setIdentificativoMessaggioRichiesta(response.getEsitoAvvisoDigitaleWS().getIdentificativoMessaggioRichiesta());

      List<EsitoAvvisoDigitaleDto> listaEsiti = new ArrayList<>();

      for (CtEsitoAvvisatura esito : response.getEsitoAvvisoDigitaleWS().getEsitoAvvisaturas()) {
        EsitoAvvisoDigitaleDto esitoDto = new EsitoAvvisoDigitaleDto();

        esitoDto.setTipoCanaleEsito(esito.getTipoCanaleEsito());
        esitoDto.setIdentificativoCanale(esito.getIdentificativoCanale());
        esitoDto.setDataEsito(esito.getDataEsito());
        esitoDto.setCodiceEsito(esito.getCodiceEsito());
        esitoDto.setDescrizioneEsito(esito.getDescrizioneEsito());
        listaEsiti.add(esitoDto);
      }
      dto.setListaEsitiAvvisiDigitali(listaEsiti);
    }

    return dto;
  }

}
