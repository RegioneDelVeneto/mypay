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
package it.regioneveneto.mygov.payment.mypay4.ws.helper;

import gov.telematici.pagamenti.ws.NodoInviaAvvisoDigitaleRisposta;
import gov.telematici.pagamenti.ws.nodoregionaleperspc.EsitoPaaInviaRT;
import gov.telematici.pagamenti.ws.nodoregionaleperspc.PaaInviaRTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.Risposta;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtResponse;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.veneto.regione.pagamenti.ente.*;
import it.veneto.regione.pagamenti.nodoregionalefesp.NodoSILInviaAvvisoDigitaleRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaRPRisposta;
import it.veneto.regione.pagamenti.pa.*;
import org.springframework.util.Assert;

import java.util.function.Function;

public class OutcomeHelper {

  public static String getOutcome(CtResponse response){
    return getOutcomeFromFaultStatus(response, CtResponse::getFault);
  }

//  public static String getOutcome(it.gov.pagopa.pagopa_api.pa.pafornode.CtResponse response){
//    return getOutcomeFromFaultStatus(response, it.gov.pagopa.pagopa_api.pa.pafornode.CtResponse::getFault);
//  }

  public static String getOutcome(Risposta response){
    return getOutcomeFromFaultStatus(response, Risposta::getFault);
  }

  public static String getOutcome(it.veneto.regione.pagamenti.pa.Risposta response){
    return getOutcomeFromFaultStatus(response, it.veneto.regione.pagamenti.pa.Risposta::getFault);
  }

  public static String getOutcome(it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.Risposta response){
    return getOutcomeFromFaultStatus(response, it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.Risposta::getFault);
  }


  public static String getOutcome(NodoSILInviaRPRisposta response){
    return getOutcomeFromFaultStatus(response, NodoSILInviaRPRisposta::getFault);
  }

  public static String getOutcome(PaaInviaRTRisposta response){
    return getOutcomeFromFaultStatus(response, PaaInviaRTRisposta::getPaaInviaRTRisposta, EsitoPaaInviaRT::getFault);
  }

  public static String getOutcome(PaaSILInviaEsitoRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILInviaEsitoRisposta::getPaaSILInviaEsitoRisposta, EsitoPaaSILInviaEsito::getFault);
  }

  public static String getOutcome(NodoSILInviaAvvisoDigitaleRisposta response){
    return getOutcomeFromFaultStatus(response, NodoSILInviaAvvisoDigitaleRisposta::getFault);
  }

  public static String getOutcome(NodoInviaAvvisoDigitaleRisposta response){
    return getOutcomeFromFaultStatus(response, NodoInviaAvvisoDigitaleRisposta::getFault);
  }

  public static String getOutcome(PaaSILChiediPagatiRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILChiediPagatiRisposta::getFault);
  }

  public static String getOutcome(PaaSILInviaCarrelloDovutiRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILInviaCarrelloDovutiRisposta::getFault);
  }

  public static String getOutcome(PaaSILInviaDovutiRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILInviaDovutiRisposta::getFault);
  }

  public static String getOutcome(PaaSILRegistraPagamentoRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILRegistraPagamentoRisposta::getFault);
  }

  public static String getOutcome(PaaSILChiediPagatiConRicevutaRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILChiediPagatiConRicevutaRisposta::getFault);
  }

  public static String getOutcome(PaaSILImportaDovutoRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILImportaDovutoRisposta::getFault);
  }

  public static String getOutcome(PaaSILVerificaEsternaRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILVerificaEsternaRisposta::getPaaSILVerificaEsternaRisposta, EsitoSILVerificaEsterna::getFault);
  }

  public static String getOutcome(PaaSILAttivaEsternaRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILAttivaEsternaRisposta::getPaaSILAttivaEsternaRisposta, EsitoSILAttivaEsterna ::getFault);
  }

  public static String getOutcome(PaaSILAutorizzaImportFlussoRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILAutorizzaImportFlussoRisposta::getFault);
  }

  public static String getOutcome(PaaSILChiediEsitoCarrelloDovutiRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILChiediEsitoCarrelloDovutiRisposta::getFault);
  }

  public static String getOutcome(PaaSILChiediPosizioniAperteRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILChiediPosizioniAperteRisposta::getFault);
  }

  public static String getOutcome(PaaSILChiediStatoExportFlussoRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILChiediStatoExportFlussoRisposta::getFault);
  }

  public static String getOutcome(PaaSILChiediStatoImportFlussoRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILChiediStatoImportFlussoRisposta::getFault);
  }

  public static String getOutcome(PaaSILChiediStoricoPagamentiRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILChiediStoricoPagamentiRisposta::getFault);
  }

  public static String getOutcome(PaaSILPrenotaExportFlussoRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILPrenotaExportFlussoRisposta::getFault);
  }

  public static String getOutcome(PaaSILPrenotaExportFlussoIncrementaleConRicevutaRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILPrenotaExportFlussoIncrementaleConRicevutaRisposta::getFault);
  }

  public static String getOutcome(PaaSILVerificaAvvisoRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILVerificaAvvisoRisposta::getFault);
  }

  public static String getOutcome(PaaMCSAllineamentoRisposta response){
    return getOutcomeFromFaultStatus(response, PaaMCSAllineamentoRisposta::getFault);
  }
  public static String getOutcome(PaaSILPrenotaExportFlussoScadutiRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILPrenotaExportFlussoScadutiRisposta::getFault);
  }

  public static String getOutcome(PaaSILChiediStatoExportFlussoScadutiRisposta response){
    return getOutcomeFromFaultStatus(response, PaaSILChiediStatoExportFlussoScadutiRisposta::getFault);
  }

  private static <T> String getOutcomeFromFaultStatus(T response, Function<T, Object> responseToFaultFun){
    if(response==null || responseToFaultFun.apply(response)!=null)
      return Constants.GIORNALE_ESITO_EVENTO.KO.toString();
    else
      return Constants.GIORNALE_ESITO_EVENTO.OK.toString();
  }

  private static <W, T> String getOutcomeFromFaultStatus(W responseWrapper, Function<W, T> wrapperToResponseFun, Function<T, Object> responseToFaultFun){
    Assert.notNull(wrapperToResponseFun, "wrapperToResponseFun cannot be null");
    if(responseWrapper==null)
      return getOutcomeFromFaultStatus(null, responseToFaultFun);
    else
      return getOutcomeFromFaultStatus(wrapperToResponseFun.apply(responseWrapper), responseToFaultFun);
  }

}
