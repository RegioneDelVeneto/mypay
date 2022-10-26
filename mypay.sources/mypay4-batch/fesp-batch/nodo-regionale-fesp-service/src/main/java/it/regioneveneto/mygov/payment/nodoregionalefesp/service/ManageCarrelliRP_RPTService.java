package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRp;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRpt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public interface ManageCarrelliRP_RPTService {

@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	MygovCarrelloRp insertCarrelloRPWithRefresh(String id_session, String dominioChiamante);

@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
MygovCarrelloRpt insertCarrelloRPTWithRefresh(MygovCarrelloRp mygovCarrelloRp);

@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
void updateRispostaRptById(Long mygovCarrelloRptId, String esitoComplessivoOperazione, String url, FaultBean faultBean)
				throws UnsupportedEncodingException, MalformedURLException;

@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
MygovCarrelloRp updateRispostaRpById(Long mygovCarrelloRpId, String esito, String url, String faultCode, String faultString,
		String id, String description, Integer serial, String string, String originalFaultCode, String originalFaultString,
		String originalFaultDescription);


MygovCarrelloRp getCarrelloRpByIdSession(String nodoRegionaleFespIdSession);

MygovCarrelloRpt getCarrelloRptByRpEId(Long mygovCarrelloRpId);

MygovCarrelloRpt getCarrelloRptByIdSession(String nodoSPCFespIdSession);

MygovCarrelloRp getCarrelloRpById(Long mygovCarrelloRpId);

}
