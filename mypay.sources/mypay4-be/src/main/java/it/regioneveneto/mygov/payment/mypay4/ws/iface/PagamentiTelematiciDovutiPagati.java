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
package it.regioneveneto.mygov.payment.mypay4.ws.iface;

import it.veneto.regione.pagamenti.ente.FaultBean;
import it.veneto.regione.pagamenti.ente.ListaCarrelli;
import it.veneto.regione.pagamenti.ente.PaaSILAutorizzaImportFlusso;
import it.veneto.regione.pagamenti.ente.PaaSILAutorizzaImportFlussoRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILChiediPosizioniAperte;
import it.veneto.regione.pagamenti.ente.PaaSILChiediPosizioniAperteRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILChiediStatoExportFlusso;
import it.veneto.regione.pagamenti.ente.PaaSILChiediStatoExportFlussoRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILChiediStatoImportFlusso;
import it.veneto.regione.pagamenti.ente.PaaSILChiediStatoImportFlussoRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILChiediStoricoPagamenti;
import it.veneto.regione.pagamenti.ente.PaaSILChiediStoricoPagamentiRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILImportaDovuto;
import it.veneto.regione.pagamenti.ente.PaaSILImportaDovutoRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILInviaCarrelloDovuti;
import it.veneto.regione.pagamenti.ente.PaaSILInviaCarrelloDovutiRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILInviaDovuti;
import it.veneto.regione.pagamenti.ente.PaaSILInviaDovutiRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILPrenotaExportFlusso;
import it.veneto.regione.pagamenti.ente.PaaSILPrenotaExportFlussoIncrementaleConRicevuta;
import it.veneto.regione.pagamenti.ente.PaaSILPrenotaExportFlussoIncrementaleConRicevutaRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILPrenotaExportFlussoRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILVerificaAvviso;
import it.veneto.regione.pagamenti.ente.PaaSILVerificaAvvisoRisposta;
import it.veneto.regione.pagamenti.ente.ppthead.IntestazionePPT;

import javax.activation.DataHandler;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import java.math.BigDecimal;

public interface PagamentiTelematiciDovutiPagati {
    PaaSILAutorizzaImportFlussoRisposta paaSILAutorizzaImportFlusso(PaaSILAutorizzaImportFlusso bodyrichiesta, IntestazionePPT header);

    void paaSILChiediEsitoCarrelloDovuti(String codIpaEnte, String password, String idSessionCarrello, Holder<FaultBean> fault, Holder<ListaCarrelli> listaCarrelli);

    void paaSILChiediPagati(String codIpaEnte, String password, String idSession, Holder<FaultBean> fault, Holder<DataHandler> pagati);

    void paaSILChiediPagatiConRicevuta(String codIpaEnte, String password, String idSession,
                                        String identificativoUnivocoVersamento, String identificativoUnivocoDovuto, Holder<FaultBean> fault,
                                        Holder<DataHandler> pagati, Holder<String> tipoFirma, Holder<DataHandler> rt);

    PaaSILChiediPosizioniAperteRisposta paaSILChiediPosizioniAperte(PaaSILChiediPosizioniAperte bodyrichiesta);

    PaaSILChiediStatoExportFlussoRisposta paaSILChiediStatoExportFlusso(PaaSILChiediStatoExportFlusso bodyrichiesta, IntestazionePPT header);

    PaaSILChiediStatoImportFlussoRisposta paaSILChiediStatoImportFlusso(PaaSILChiediStatoImportFlusso bodyrichiesta, IntestazionePPT header);

    PaaSILChiediStoricoPagamentiRisposta paaSILChiediStoricoPagamenti(PaaSILChiediStoricoPagamenti bodyrichiesta);

    PaaSILImportaDovutoRisposta paaSILImportaDovuto(PaaSILImportaDovuto bodyrichiesta, IntestazionePPT header);

    PaaSILInviaCarrelloDovutiRisposta paaSILInviaCarrelloDovuti(PaaSILInviaCarrelloDovuti bodyrichiesta, IntestazionePPT header);

    PaaSILInviaDovutiRisposta paaSILInviaDovuti(PaaSILInviaDovuti bodyrichiesta, IntestazionePPT header);

    PaaSILPrenotaExportFlussoRisposta paaSILPrenotaExportFlusso(PaaSILPrenotaExportFlusso bodyrichiesta, IntestazionePPT header);

    PaaSILPrenotaExportFlussoIncrementaleConRicevutaRisposta paaSILPrenotaExportFlussoIncrementaleConRicevuta(PaaSILPrenotaExportFlussoIncrementaleConRicevuta bodyrichiesta, IntestazionePPT header);

    void paaSILRegistraPagamento(String codIpaEnte,
                                 String password,
                                 String identificativoUnivocoVersamento,
                                 String codiceContestoPagamento,
                                 BigDecimal singoloImportoPagato,
                                 XMLGregorianCalendar dataEsitoSingoloPagamento,
                                 Integer indiceDatiSingoloPagamento,
                                 String identificativoUnivocoRiscossione,
                                 String tipoIstitutoAttestante,
                                 String codiceIstitutiAttestante,
                                 String denominazioneAttestante,
                                 Holder<FaultBean> fault, Holder<String> esito);

    PaaSILVerificaAvvisoRisposta paaSILVerificaAvviso(PaaSILVerificaAvviso bodyrichiesta, IntestazionePPT header);

}
