package it.regioneveneto.mygov.payment.nodoregionalefesp.dao;

import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRp;

public interface CarrelloRpDao {

	MygovCarrelloRp insertWithRefresh(String id_session, String dominioChiamante);

	MygovCarrelloRp updateRispostaRpByRpId(Long mygovCarrelloRpId, String esito, String url, String faultCode,
			String faultString, String id, Integer serial, String idSession, String originalFaultCode, 
			String originalFaultString, String originalFaultDescription);

	MygovCarrelloRp getCarrelloRpByIdSession(String nodoRegionaleFespIdSession);

	MygovCarrelloRp getCarrelloRpById(Long mygovCarrelloRpId);

}
