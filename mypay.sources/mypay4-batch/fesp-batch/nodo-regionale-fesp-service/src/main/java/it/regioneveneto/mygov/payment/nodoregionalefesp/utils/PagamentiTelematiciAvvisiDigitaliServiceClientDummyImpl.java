package it.regioneveneto.mygov.payment.nodoregionalefesp.utils;

import gov.telematici.pagamenti.ws.nodospcfesp.avvisaturadigitale.CtNodoInviaAvvisoDigitale;
import gov.telematici.pagamenti.ws.nodospcfesp.avvisaturadigitale.CtNodoInviaAvvisoDigitaleRisposta;
import gov.telematici.pagamenti.ws.sachead.IntestazionePPT;
import it.regioneveneto.mygov.payment.nodospcfesp.avvisaturadigitale.client.PagamentiTelematiciAvvisiDigitaliServiceClient;

public class PagamentiTelematiciAvvisiDigitaliServiceClientDummyImpl implements PagamentiTelematiciAvvisiDigitaliServiceClient {

	@Override
	public CtNodoInviaAvvisoDigitaleRisposta nodoInviaAvvisoDigitale(IntestazionePPT header,
			CtNodoInviaAvvisoDigitale bodyrichiesta) {
		throw new RuntimeException("Method not implemented");
	}

}
