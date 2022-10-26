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

import gov.telematici.pagamenti.ws.CtAvvisoDigitale;
import gov.telematici.pagamenti.ws.CtDatiSingoloVersamento;
import gov.telematici.pagamenti.ws.CtEsitoAvvisatura;
import gov.telematici.pagamenti.ws.CtIdentificativoUnivocoPersonaFG;
import gov.telematici.pagamenti.ws.CtSoggettoPagatore;
import gov.telematici.pagamenti.ws.NodoInviaAvvisoDigitale;
import gov.telematici.pagamenti.ws.NodoInviaAvvisoDigitaleRisposta;
import gov.telematici.pagamenti.ws.StTipoIdentificativoUnivocoPersFG;
import gov.telematici.pagamenti.ws.StTipoOperazione;
import gov.telematici.pagamenti.ws.sachead.IntestazionePPT;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.EsitoAvvisoDigitaleCompletoDto;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.EsitoAvvisoDigitaleDto;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
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
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
public class NodoInviaAvvisoDigitaleService {

  @Autowired
  private PagamentiTelematiciAvvisiDigitaliServiceClient pagamentiTelematiciAvvisiDigitaliServiceClient;

  @Autowired
  private GiornaleService giornaleService;

  private final String propIdStazioneIntermediarioPa = "";

  private final String propPassword = "";

  public EsitoAvvisoDigitaleCompletoDto nodoInviaAvvisoDigitale(String identificativoDominioHeader,
                                                                String identificativoIntermediarioPAHeader, String identificativoStazioneIntermediarioPAHeader,
                                                                String identificativoDominio, String anagraficaBeneficiario, String identificativoMessaggioRichiesta,
                                                                String tassonomiaAvviso, String codiceAvviso, String anagraficaPagatore, String codiceIdentificativoUnivoco,
                                                                String tipoIdentificativoUnivoco, XMLGregorianCalendar dataScadenzaPagamento,
                                                                XMLGregorianCalendar dataScadenzaAvviso, BigDecimal importoAvviso, String eMailSoggetto,
                                                                String cellulareSoggetto, String descrizionePagamento, String urlAvviso, String ibanAccredito, String ibanAppoggio,
                                                                String tipoPagamento, String tipoOperazione) {

    Date dataOraEvento;
    String idDominio;
    String identificativoUnivocoVersamento;
    String codiceContestoPagamento;
    String identificativoPrestatoreServiziPagamento;
    String tipoVers;
    String componente;
    String categoriaEvento;
    String tipoEvento;
    String sottoTipoEvento;
    String identificativoFruitore;
    String identificativoErogatore;
    String identificativoStazioneIntermediarioPa;
    String canalePagamento;
    String parametriSpecificiInterfaccia;
    String esitoReq;
    /**
     * LOG nel Giornale degli Eventi della richiesta
     */
    try {
      dataOraEvento = new Date();
      idDominio = identificativoDominioHeader;
      identificativoUnivocoVersamento = "";
      codiceContestoPagamento = "n/a";
      identificativoPrestatoreServiziPagamento = "";
      tipoVers = null;
      componente = Constants.COMPONENTE.FESP.toString();
      categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
      tipoEvento = Constants.GIORNALE_TIPO_EVENTO_FESP.nodoInviaAvvisoDigitale.toString();
      sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString();

      identificativoFruitore = identificativoDominioHeader;
      identificativoErogatore = propIdStazioneIntermediarioPa;
      identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
      canalePagamento = "";

      parametriSpecificiInterfaccia = "Parametri di richiesta verso il Nodo SPC: identificativoDominioHeader [ "
          + identificativoDominioHeader + " ], identificativoIntermediarioPAHeader [ "
          + identificativoIntermediarioPAHeader + " ], identificativoStazioneIntermediarioPAHeader [ "
          + identificativoStazioneIntermediarioPAHeader + " ], identificativoDominioAvviso [ "
          + identificativoDominio + " ], anagraficaBeneficiario [ " + anagraficaBeneficiario
          + " ], identificativoMessaggioRichiesta [ " + identificativoMessaggioRichiesta
          + " ], tassonomiaAvviso [ " + tassonomiaAvviso + " ], codiceAvviso [ " + codiceAvviso
          + " ], anagraficaPagatore [ " + anagraficaPagatore + " ], codiceIdentificativoUnivocoPagatore [ "
          + codiceIdentificativoUnivoco + " ] tipoIdentificativoUnivocoPagatore [ "
          + tipoIdentificativoUnivoco + " ], dataScadenzaPagamento [ " + dataScadenzaPagamento
          + " ], dataScadenzaAvviso [ " + dataScadenzaAvviso + " ], importoAvviso [ " + importoAvviso
          + " ], emailSoggetto [ " + eMailSoggetto + " ], cellulareSoggetto [ " + cellulareSoggetto
          + " ], descrizionePagamento [ " + descrizionePagamento + " ], urlAvviso [ " + urlAvviso + " ], "
          + "ibanAccredito [ " + ibanAccredito +" ], ibanAppoggio [ " + ibanAppoggio + " ], tipoPagamento [ "
          + tipoPagamento + " ], tipoOperazione [ " + tipoOperazione + " ]";

      esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

      giornaleService.registraEvento(dataOraEvento, idDominio, identificativoUnivocoVersamento,
          codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVers, componente,
          categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
          identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
    } catch (Exception e1) {
      log.warn("nodoSILInviaAvvisoDigitale REQUEST impossibile inserire nel giornale degli eventi", e1);
    }

    /**
     * Creazione header
     */
    IntestazionePPT header = new IntestazionePPT();
    header.setIdentificativoDominio(identificativoDominioHeader);
    header.setIdentificativoIntermediarioPA(identificativoIntermediarioPAHeader);
    header.setIdentificativoStazioneIntermediarioPA(identificativoStazioneIntermediarioPAHeader);

    /**
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

    request.setPassword(propPassword);

    try{
      NodoInviaAvvisoDigitaleRisposta response = pagamentiTelematiciAvvisiDigitaliServiceClient
          .nodoInviaAvvisoDigitale(request, header);
      EsitoAvvisoDigitaleCompletoDto responseDto = mapXMLResponseToDto(header, response);

      if(responseDto.getFaultBean() != null) {
        // RESPONSE FAULT BEAN
        try {
          dataOraEvento = new Date();
          idDominio = header.getIdentificativoDominio();
          identificativoUnivocoVersamento = "";
          codiceContestoPagamento = "n/a";
          identificativoPrestatoreServiziPagamento = "";
          tipoVers = null;
          componente = Constants.COMPONENTE.FESP.toString();
          categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
          tipoEvento = Constants.GIORNALE_TIPO_EVENTO_FESP.nodoInviaAvvisoDigitale.toString();
          sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();

          identificativoFruitore = header.getIdentificativoDominio();
          identificativoErogatore = propIdStazioneIntermediarioPa;
          identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
          canalePagamento = "";

          parametriSpecificiInterfaccia = "Parametri di risposta dal Nodo SPC: faultId [ "
              + responseDto.getFaultBean().getId() + " ], faultCode [ "
              + responseDto.getFaultBean().getFaultCode() + " ], faultString[ "
              + responseDto.getFaultBean().getFaultString() + " ], faultDescription [ "
              + responseDto.getFaultBean().getDescription() + " ], faultSerial [ "
              + responseDto.getFaultBean().getSerial() + " ]";

          esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

          giornaleService.registraEvento(dataOraEvento, idDominio, identificativoUnivocoVersamento,
              codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVers, componente,
              categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
              identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
              parametriSpecificiInterfaccia, esitoReq);
        } catch (Exception e1) {
          log.warn("nodoSILInviaAvvisoDigitale RESPONSE impossibile inserire nel giornale degli eventi", e1);
        }
      } else {
        // RESPONSE
        try {
          dataOraEvento = new Date();
          idDominio = header.getIdentificativoDominio();
          identificativoUnivocoVersamento = "";
          codiceContestoPagamento = "n/a";
          identificativoPrestatoreServiziPagamento = "";
          tipoVers = null;
          componente = Constants.COMPONENTE.FESP.toString();
          categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
          tipoEvento = Constants.GIORNALE_TIPO_EVENTO_FESP.nodoInviaAvvisoDigitale.toString();
          sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();

          identificativoFruitore = header.getIdentificativoDominio();
          identificativoErogatore = propIdStazioneIntermediarioPa;
          identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
          canalePagamento = "";

          String esitoString = "";

          for (EsitoAvvisoDigitaleDto esito : responseDto.getListaEsitiAvvisiDigitali()) {
            esitoString += "{tipoCanaleEsito [ " + esito.getTipoCanaleEsito()
                + " ], identificativoCanale [ " + esito.getIdentificativoCanale() + " ], dataEsito [ "
                + esito.getDataEsito() + " ], codiceEsito [ " + esito.getCodiceEsito()
                + " ], descrizioneEsito [ " + esito.getDescrizioneEsito() + " ]}, ";
          }
          esitoString = esitoString.trim();
          if (esitoString.endsWith(","))
            esitoString = esitoString.substring(0, esitoString.length() - 1);

          parametriSpecificiInterfaccia = "Parametri di risposta dal Nodo SPC: identificativoDominioHeader [ "
              + responseDto.getIdentificativoDominioHeader()
              + " ], identificativoIntermediarioPAHeader [ "
              + responseDto.getIdentificativoIntermediarioPAHeader()
              + " ], identificativoStazioneIntermediarioPAHeader [ "
              + responseDto.getIdentificativoStazioneIntermediarioPAHeader() + " ], esitoOperazione [ "
              + responseDto.getEsitoOperazione() + " ], identificativoDominioEsito [ "
              + responseDto.getIdentificativoDominio() + " ], identificativoMessaggioRichiesta [ "
              + responseDto.getIdentificativoMessaggioRichiesta() + " ], " + esitoString;

          parametriSpecificiInterfaccia = parametriSpecificiInterfaccia.trim();
          if (parametriSpecificiInterfaccia.endsWith(","))
            parametriSpecificiInterfaccia = parametriSpecificiInterfaccia.substring(0, parametriSpecificiInterfaccia.length() - 1);
          if (parametriSpecificiInterfaccia.length() > Constants.GIORNALE_PARAMETRI_SPECIFICI_INTERFACCIA_MAX_LENGTH)
            parametriSpecificiInterfaccia = parametriSpecificiInterfaccia.substring(0, Constants.GIORNALE_PARAMETRI_SPECIFICI_INTERFACCIA_MAX_LENGTH);

          esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

          giornaleService.registraEvento(dataOraEvento, idDominio, identificativoUnivocoVersamento,
              codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVers, componente,
              categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
              identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
              parametriSpecificiInterfaccia, esitoReq);
        } catch (Exception e1) {
          log.warn("nodoSILInviaAvvisoDigitale RESPONSE impossibile inserire nel giornale degli eventi", e1);
        }
      }

      return responseDto;
    }catch(Exception ex) {
      log.error(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [" + ex.getMessage() + "]", ex);

      try {
        dataOraEvento = new Date();
        idDominio = header.getIdentificativoDominio();
        identificativoUnivocoVersamento = "";
        codiceContestoPagamento = "n/a";
        identificativoPrestatoreServiziPagamento = "";
        tipoVers = null;
        componente = Constants.COMPONENTE.FESP.toString();
        categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
        tipoEvento = Constants.GIORNALE_TIPO_EVENTO_FESP.nodoInviaAvvisoDigitale.toString();
        sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();

        identificativoFruitore = header.getIdentificativoDominio();
        identificativoErogatore = propIdStazioneIntermediarioPa;
        identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
        canalePagamento = "";

        parametriSpecificiInterfaccia = ex.getMessage();

        esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

        giornaleService.registraEvento(dataOraEvento, idDominio, identificativoUnivocoVersamento,
            codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVers, componente,
            categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
            identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
            esitoReq);
      } catch (Exception e1) {
        log.warn("nodoSILInviaAvvisoDigitale RESPONSE impossibile inserire nel giornale degli eventi", e1);
      }
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

      List<EsitoAvvisoDigitaleDto> listaEsiti = new ArrayList<EsitoAvvisoDigitaleDto>();

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
