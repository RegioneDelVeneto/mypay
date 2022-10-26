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
package it.regioneveneto.mygov.payment.mypay4.ws.impl;

import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.service.EnteService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.util.VerificationUtils;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.PagamentiTelematiciFlussiSPC;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciRP;
import it.veneto.regione.pagamenti.ente.FaultBean;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.*;
import it.veneto.regione.pagamenti.pa.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;
import java.util.Optional;

@Service("PagamentiTelematiciFlussiSPCImpl")
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class PagamentiTelematiciFlussiSPCImpl implements PagamentiTelematiciFlussiSPC {

  @Autowired
  private EnteService enteService;

  @Autowired
  private PagamentiTelematiciRP pagamentiTelematiciRP;

  @Override
  public PaaSILChiediFlussoSPCRisposta paaSILChiediFlussoSPC(PaaSILChiediFlussoSPC request) {
    PaaSILChiediFlussoSPCRisposta risposta = new PaaSILChiediFlussoSPCRisposta();
    String codIpaEnte = request.getCodIpaEnte(), password = request.getPassword();
    Ente ente = enteService.getEnteByCodIpa(codIpaEnte);

    Optional<FaultBean> faultValue = enteService.verificaEnte(codIpaEnte, password);
    if (faultValue.isPresent()) {
      log.error("paaSILChiediFlussoSPC: Ente non valido: " + codIpaEnte);
      risposta.setStato(Constants.ESITO.KO.getValue());
      risposta.setFault(VerificationUtils.convertToPaFaultBean(faultValue.get()));
      return risposta;
    }

    try {
      ChiediFlussoSPC chiediFlussoSPC = new ChiediFlussoSPC();
      chiediFlussoSPC.setFlgTipoFlusso(request.getTipoFlusso());
      chiediFlussoSPC.setIdentificativoDominio(ente.getCodiceFiscaleEnte());
      chiediFlussoSPC.setIdentificativoPSP(request.getIdentificativoPsp());
      chiediFlussoSPC.setCodIdentificativoFlusso(request.getIdentificativoFlusso());
      chiediFlussoSPC.setDtDataOraFlusso(request.getDataOraFlusso());

      ChiediFlussoSPCRisposta chiediFlussoSPCRisposta = pagamentiTelematiciRP.chiediFlussoSPC(chiediFlussoSPC);
      if (Constants.ESITO.OK.getValue().equals(chiediFlussoSPCRisposta.getStato())) {
        risposta.setDownloadUrl(chiediFlussoSPCRisposta.getDownloadURL());
      }
      risposta.setStato(chiediFlussoSPCRisposta.getStato());
    } catch (Exception ex) {
      log.error("Errore interno chiamata 'paaSILChiediFlussoSPC' :", ex);
      risposta.setStato(Constants.ESITO.KO.getValue());
      risposta.setFault(VerificationUtils.getPaFaultBean(codIpaEnte,"CODE_PAA_ERRORE_INTERNO","Errore interno a pa", ex.getMessage(), 1));
    }
    return risposta;
  }

  @Override
  public PaaSILChiediElencoFlussiSPCRisposta paaSILChiediElencoFlussiSPC(PaaSILChiediElencoFlussiSPC request) {
    PaaSILChiediElencoFlussiSPCRisposta risposta = new PaaSILChiediElencoFlussiSPCRisposta();
    String codIpaEnte = request.getCodIpaEnte(), password = request.getPassword();
    Ente ente = enteService.getEnteByCodIpa(codIpaEnte);

    Optional<FaultBean> faultValue = enteService.verificaEnte(codIpaEnte, password);
    if (faultValue.isPresent()) {
      log.error("paaSILChiediElencoFlussiSPC: Ente non valido: %s", codIpaEnte);
      risposta.setFault(VerificationUtils.convertToPaFaultBean(faultValue.get()));
      return risposta;
    }

    /*
     * **************************************
     * CONTROLLO SULLA CORRETTEZZA DELLE DATE
     * **************************************
     */
    var fb = VerificationUtils.checkDateExtraction(codIpaEnte, Utilities.toDate(request.getDateFrom()), Utilities.toDate(request.getDateTo()));
    if (fb != null) {
      log.error("paaSILChiediElencoFlussiSPC: %s", fb.getFaultString());
      risposta.setFault(VerificationUtils.convertToPaFaultBean(fb));
      return risposta;
    }

    try {
      ChiediListaFlussiSPC chiediListaFlussiSPC = new ChiediListaFlussiSPC();
      chiediListaFlussiSPC.setDateFrom(request.getDateFrom());
      chiediListaFlussiSPC.setDateTo(request.getDateTo());
      chiediListaFlussiSPC.setFlgTipoFlusso(request.getTipoFlusso());
      chiediListaFlussiSPC.setIdentificativoPSP(request.getIdentificativoPsp());
      chiediListaFlussiSPC.setIdentificativoDominio(ente.getCodiceFiscaleEnte());

      ChiediListaFlussiSPCRisposta chiediFlussoSPCPageRisposta = pagamentiTelematiciRP.chiediListaFlussiSPC(chiediListaFlussiSPC);

      TipoElencoFlussiSPC elencoFlussiSPCValue = new TipoElencoFlussiSPC();
      elencoFlussiSPCValue.setTotRestituiti(chiediFlussoSPCPageRisposta.getTotalRecords());
      List<TipoIdSPC> idSPC = elencoFlussiSPCValue.getIdSPCs();
      for (FlussoSPC flussoSPC : chiediFlussoSPCPageRisposta.getFlussoSPCs()) {
        TipoIdSPC tipoIdSPC = new TipoIdSPC();
        tipoIdSPC.setIdentificativoFlusso(flussoSPC.getCodIdentificativoFlusso());
        log.info("Output per CodIdentificativoFlusso:" + flussoSPC.getCodIdentificativoFlusso());

        XMLGregorianCalendar c = flussoSPC.getDtDataOraFlusso();
        XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c.getYear(), c.getMonth(), c.getDay(), c.getHour(),
            c.getMinute(), c.getSecond(), DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED);
        tipoIdSPC.setDataOraFlusso(xmlDate);

        idSPC.add(tipoIdSPC);
      }
      risposta.setElencoFlussiSPC(elencoFlussiSPCValue);

      if (chiediFlussoSPCPageRisposta.getFault() != null)
        risposta.setFault(VerificationUtils.convertToPaFaultBean(chiediFlussoSPCPageRisposta.getFault()));
    } catch (java.lang.Exception ex) {
      risposta.setFault(VerificationUtils.getPaFaultBean(codIpaEnte,"CODE_PAA_ERRORE_INTERNO", "Errore interno a pa", ex.getMessage(), 1));
      log.error("Errore interno chiamata 'paaSILChiediElencoFlussiSPC' :", ex);
    }
    return risposta;
  }
}
