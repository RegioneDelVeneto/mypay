package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRPTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRispostaRevoca;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRispostaRevocaRisposta;
import gov.telematici.pagamenti.ws.ppthead.IntestazionePPT;
import it.gov.digitpa.schemas.x2011.pagamenti.CtRichiestaPagamentoTelematico;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovEnte;
import it.regioneveneto.mygov.payment.nodoregionalefesp.exceptions.NodoSILInviaRPRispostaException;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediCopiaEsito;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediCopiaEsitoRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediSceltaWISP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediSceltaWISPRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaCarrelloRP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaCarrelloRPRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaRP;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILInviaRPRisposta;

public interface NodoInviaRPTService {

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	NodoSILInviaRPRisposta nodoSILInviaRP(NodoSILInviaRP bodyrichiesta, it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT header)
			throws NodoSILInviaRPRispostaException;

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	IntestazionePPT buildHeaderRPT(it.veneto.regione.pagamenti.nodoregionalefesp.ppthead.IntestazionePPT header, MygovEnte enteProp);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	IntestazionePPT buildHeaderRPT(final String codRptIdMessaggioRichiesta);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	CtRichiestaPagamentoTelematico buildRPT(it.veneto.regione.schemas.x2012.pagamenti.CtRichiestaPagamento rp, MygovEnte enteProp);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	CtRichiestaPagamentoTelematico buildRPT(final String codRptIdMessaggioRichiesta);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	NodoInviaRPT buildBodyRPT(CtRichiestaPagamentoTelematico CtRPT, NodoSILInviaRP bodyrichiesta);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	NodoInviaRPT buildBodyRPT(CtRichiestaPagamentoTelematico CtRPT, final String codRptIdMessaggioRichiesta);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	NodoInviaRPTRisposta nodoInviaRPT(Long mygovRptRtId, IntestazionePPT _paaSILInviaRPT_header, NodoInviaRPT _paaSILInviaRPT_body)
			throws UnsupportedEncodingException, MalformedURLException;

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void saveRPTRisposta(NodoInviaRPTRisposta _rispostaRPT, Long mygovRptRtId) throws UnsupportedEncodingException, MalformedURLException;

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	NodoSILChiediSceltaWISPRisposta nodoChiediSceltaWISP(NodoSILChiediSceltaWISP nodoChiediSceltaWisp);
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	NodoSILChiediCopiaEsitoRisposta nodoSILChiediCopiaEsito(NodoSILChiediCopiaEsito nodoSilChiediCopiaEsito);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	NodoSILInviaCarrelloRPRisposta nodoSILInviaCarrelloRP(NodoSILInviaCarrelloRP nc);
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	NodoInviaRispostaRevocaRisposta nodoInviaRispostaRevoca(NodoInviaRispostaRevoca bodyrichiesta);

}