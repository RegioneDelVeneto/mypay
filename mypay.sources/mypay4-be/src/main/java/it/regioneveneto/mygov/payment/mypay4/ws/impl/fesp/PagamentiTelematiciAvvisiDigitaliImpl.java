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
import it.regioneveneto.mygov.payment.mypay4.service.fesp.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.service.fesp.NodoInviaAvvisoDigitaleService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciAvvisiDigitali;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.veneto.regione.pagamenti.nodoregionalefesp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service("PagamentiTelematiciAvvisiDigitaliImpl")
@Slf4j
@ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class PagamentiTelematiciAvvisiDigitaliImpl implements PagamentiTelematiciAvvisiDigitali {

  @Autowired
  private GiornaleService giornaleService;

  @Value("${pa.identificativoStazioneIntermediarioPA}")
  private String propIdStazioneIntermediarioPa;

  @Autowired
  private NodoInviaAvvisoDigitaleService nodoInviaAvvisoDigitaleService;

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public NodoSILInviaAvvisoDigitaleRisposta nodoSILInviaAvvisoDigitale(NodoSILInviaAvvisoDigitale bodyrichiesta, IntestazionePPT header) {
    log.info("Executing operation nodoSILInviaAvvisoDigitale");

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
    /*
     * LOG nel Giornale degli Eventi della richiesta
     */
    try {
      dataOraEvento = new Date();
      idDominio = header.getIdentificativoDominio();
      identificativoUnivocoVersamento = "";
      codiceContestoPagamento = "n/a";
      identificativoPrestatoreServiziPagamento = "";
      tipoVers = null;
      componente = Constants.COMPONENTE.FESP.toString();
      categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
      tipoEvento = Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILInviaAvvisoDigitale.toString();
      sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString();

      identificativoFruitore = header.getIdentificativoDominio();
      identificativoErogatore = propIdStazioneIntermediarioPa;
      identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
      canalePagamento = "";

      parametriSpecificiInterfaccia = "Parametri di richiesta verso il Nodo SPC: "
          + "identificativoDominioHeader [ "+ header.getIdentificativoDominio() + " ], "
          + "identificativoIntermediarioPAHeader [ "+ header.getIdentificativoIntermediarioPA() + " ], "
          + "identificativoStazioneIntermediarioPAHeader [ "+ header.getIdentificativoStazioneIntermediarioPA() + " ], "
          + "identificativoDominioAvviso [ "+ bodyrichiesta.getAvvisoDigitaleWS().getIdentificativoDominio() + " ], "
          + "anagraficaBeneficiario [ "+ bodyrichiesta.getAvvisoDigitaleWS().getAnagraficaBeneficiario()+ " ], "
          + "identificativoMessaggioRichiesta [ "+ bodyrichiesta.getAvvisoDigitaleWS().getIdentificativoMessaggioRichiesta()+ " ], "
          + "tassonomiaAvviso [ " + bodyrichiesta.getAvvisoDigitaleWS().getTassonomiaAvviso()+ " ], "
          + "codiceAvviso [ " + bodyrichiesta.getAvvisoDigitaleWS().getCodiceAvviso()+ " ], "
          + "anagraficaPagatore [ "+ bodyrichiesta.getAvvisoDigitaleWS().getSoggettoPagatore().getAnagraficaPagatore()+ " ], "
          + "codiceIdentificativoUnivocoPagatore [ "+ bodyrichiesta.getAvvisoDigitaleWS().getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco()+ " ], "
          + "tipoIdentificativoUnivocoPagatore [ "+ bodyrichiesta.getAvvisoDigitaleWS().getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString()+ " ], "
          + "dataScadenzaPagamento [ " + bodyrichiesta.getAvvisoDigitaleWS().getDataScadenzaPagamento()+ " ], "
          + "dataScadenzaAvviso [ " + bodyrichiesta.getAvvisoDigitaleWS().getDataScadenzaAvviso()+ " ], "
          + "importoAvviso [ " + bodyrichiesta.getAvvisoDigitaleWS().getImportoAvviso()+ " ], "
          + "emailSoggetto [ " + bodyrichiesta.getAvvisoDigitaleWS().getEMailSoggetto()+ " ], "
          + "cellulareSoggetto [ " + bodyrichiesta.getAvvisoDigitaleWS().getCellulareSoggetto()+ " ], "
          + "descrizionePagamento [ " + bodyrichiesta.getAvvisoDigitaleWS().getDescrizionePagamento()+ " ], "
          + "urlAvviso [ " + bodyrichiesta.getAvvisoDigitaleWS().getUrlAvviso() + " ], "
          + "datiSingoloVersamentoIbanAccredito [ " + bodyrichiesta.getAvvisoDigitaleWS().getDatiSingoloVersamentos().get(0).getIbanAccredito() + " ], "
          + "datiSingoloVersamentoIbanAppoggio [ " + bodyrichiesta.getAvvisoDigitaleWS().getDatiSingoloVersamentos().get(0).getIbanAppoggio() + " ], "
          + "tipoPagamento [ " + bodyrichiesta.getAvvisoDigitaleWS().getTipoPagamento() + " ], "
          + "tipoOperazione [ " + bodyrichiesta.getAvvisoDigitaleWS().getTipoOperazione().value() + " ]";

      esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

      giornaleService.registraEvento(dataOraEvento, idDominio, identificativoUnivocoVersamento,
          codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVers, componente,
          categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
          identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
    } catch (Exception e1) {
      log.warn("nodoSILInviaAvvisoDigitale REQUEST impossibile inserire nel giornale degli eventi", e1);
    }

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
        try {
          dataOraEvento = new Date();
          idDominio = header.getIdentificativoDominio();
          identificativoUnivocoVersamento = "";
          codiceContestoPagamento = "n/a";
          identificativoPrestatoreServiziPagamento = "";
          tipoVers = null;
          componente = Constants.COMPONENTE.FESP.toString();
          categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
          tipoEvento = Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILInviaAvvisoDigitale.toString();
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
        try {
          dataOraEvento = new Date();
          idDominio = header.getIdentificativoDominio();
          identificativoUnivocoVersamento = "";
          codiceContestoPagamento = "n/a";
          identificativoPrestatoreServiziPagamento = "";
          tipoVers = null;
          componente = Constants.COMPONENTE.FESP.toString();
          categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
          tipoEvento = Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILInviaAvvisoDigitale.toString();
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

      try {
        dataOraEvento = new Date();
        idDominio = header.getIdentificativoDominio();
        identificativoUnivocoVersamento = "";
        codiceContestoPagamento = "n/a";
        identificativoPrestatoreServiziPagamento = "";
        tipoVers = null;
        componente = Constants.COMPONENTE.FESP.toString();
        categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
        tipoEvento = Constants.GIORNALE_TIPO_EVENTO_FESP.nodoSILInviaAvvisoDigitale.toString();
        sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString();

        identificativoFruitore = header.getIdentificativoDominio();
        identificativoErogatore = propIdStazioneIntermediarioPa;
        identificativoStazioneIntermediarioPa = propIdStazioneIntermediarioPa;
        canalePagamento = "";

        parametriSpecificiInterfaccia = "Fault Bean: faultCode [ " + fault.getFaultCode() + " ], faultString [ "
            + fault.getFaultString() + " ], faultDescription [ " + fault.getDescription() + " ]";

        esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

        giornaleService.registraEvento(dataOraEvento, idDominio, identificativoUnivocoVersamento,
            codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVers, componente,
            categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
            identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
            esitoReq);
      } catch (Exception e1) {
        log.warn("nodoSILInviaAvvisoDigitale RESPONSE impossibile inserire nel giornale degli eventi", e1);
      }

      return response;
    }
  }
}
