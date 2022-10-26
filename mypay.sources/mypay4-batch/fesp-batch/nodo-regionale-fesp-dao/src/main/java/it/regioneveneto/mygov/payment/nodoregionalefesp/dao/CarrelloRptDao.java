package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRp;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRpt;


public interface CarrelloRptDao {
	
	MygovCarrelloRpt insertWithRefresh(MygovCarrelloRp carrelloRp, String identificativoIntermediarioPa, 
			String identificativoStazioneIntermediarioPa, String password, String identificativoCanale, String identificativoIntermediarioPsp, String identificativoPsp);

	void updateRispostaRptById(Long mygovCarrelloRptId, String esitoComplessivoOperazione, String url, String faultCode,
			String faultString, String faultId, String faultDescription, Integer faultSerial, String idSessionSPC,
			String originalFaultCode, String originalFaultString, String originalFaultDescription);

	MygovCarrelloRpt getCarrelloRptByRpEId(Long mygovCarrelloRpId);

	MygovCarrelloRpt getCarrelloRptByIdSession(String nodoSPCFespIdSession);

}
