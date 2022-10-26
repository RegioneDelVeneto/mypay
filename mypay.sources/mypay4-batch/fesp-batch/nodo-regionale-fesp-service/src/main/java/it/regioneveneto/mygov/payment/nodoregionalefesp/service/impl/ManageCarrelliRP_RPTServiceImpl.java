package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import gov.telematici.pagamenti.ws.nodospcpernodoregionale.FaultBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.CarrelloRpDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.CarrelloRptDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRp;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovCarrelloRpt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRptRt;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.utils.FespBean;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.ManageCarrelliRP_RPTService;
import it.regioneveneto.mygov.payment.utils.PropertiesUtil;
import it.regioneveneto.mygov.payment.utils.Utils;

public class ManageCarrelliRP_RPTServiceImpl implements ManageCarrelliRP_RPTService {

	private FespBean fespProperties;
	
	@Autowired
	private PropertiesUtil propertiesUtil;

	private CarrelloRpDao carrelloRpDao;
	private CarrelloRptDao carrelloRptDao;

	public void setCarrelloRptDao(CarrelloRptDao carrelloRptDao) {
		this.carrelloRptDao = carrelloRptDao;
	}



	private static final Log log = LogFactory.getLog(ManageCarrelliRP_RPTServiceImpl.class);


	public void setCarrelloRpDao(CarrelloRpDao carrelloRpDao) {
		this.carrelloRpDao = carrelloRpDao;
	}



	@Override
	public MygovCarrelloRp insertCarrelloRPWithRefresh(String id_session, 
			String dominioChiamante) {

		return carrelloRpDao.insertWithRefresh(id_session, dominioChiamante);
	}



	@Override
	public MygovCarrelloRpt insertCarrelloRPTWithRefresh(MygovCarrelloRp mygovCarrelloRp) {
		
		return carrelloRptDao.insertWithRefresh(mygovCarrelloRp,
				this.propertiesUtil.getProperty("nodoRegionaleFesp.identificativoIntermediarioPA"),
				this.propertiesUtil.getProperty("nodoRegionaleFesp.identificativoStazioneIntermediarioPA"),
				this.propertiesUtil.getProperty("nodoRegionaleFesp.password"),
				this.propertiesUtil.getProperty("nodoRegionaleFesp.pspFittizioIdentificativoCanale"),
				this.propertiesUtil.getProperty("nodoRegionaleFesp.pspFittizioIdentificativoIntermediarioPsp"),
				this.propertiesUtil.getProperty("nodoRegionaleFesp.pspFittizioIdentificativoPsp"));
	}



	@Override
	public void updateRispostaRptById(Long mygovCarrelloRptId, String esitoComplessivoOperazione, String url, FaultBean faultBean)
					throws UnsupportedEncodingException, MalformedURLException{

		String faultCode = faultBean.getFaultCode();
		String faultString = faultBean.getFaultString();
		String faultId = faultBean.getId();
		String faultDescription = faultBean.getDescription();
		Integer faultSerial = faultBean.getSerial();
		String originalFaultCode = faultBean.getOriginalFaultCode();
		String originalFaultString = faultBean.getOriginalFaultString();
		String originalFaultDescription = faultBean.getOriginalDescription();

		String idSessionSPC = null;
		if (StringUtils.isNotBlank(url)) {
			//estrarre idSessioSPC
			Map<String, String> parametersMap = Utils.splitQuery(new URL(url));
			idSessionSPC = parametersMap.get("idSession");
		}
		carrelloRptDao.updateRispostaRptById(mygovCarrelloRptId, esitoComplessivoOperazione,  url,
				faultCode, faultString, faultId, faultDescription, faultSerial, idSessionSPC, originalFaultCode,
				originalFaultString, originalFaultDescription);

	}



	@Override
	public MygovCarrelloRp updateRispostaRpById(Long mygovCarrelloRpId, String esito, String url, String faultCode,
			String faultString, String id, String description, Integer serial, String idSession, String originalFaultCode, 
			String originalFaultString, String originalFaultDescription) {

		log.debug("Invocato updateRispostaRpById con: mygovCarrelloRpId = [" + mygovCarrelloRpId + "] ");

		return carrelloRpDao.updateRispostaRpByRpId(mygovCarrelloRpId, esito,  url, faultCode,
				faultString,  id, serial, idSession,originalFaultCode, originalFaultString, originalFaultDescription);
	}



	@Override
	public MygovCarrelloRp getCarrelloRpByIdSession(String nodoRegionaleFespIdSession) {

		return carrelloRpDao.getCarrelloRpByIdSession(nodoRegionaleFespIdSession);
	}



	@Override
	public MygovCarrelloRpt getCarrelloRptByRpEId(Long mygovCarrelloRpId) {
		// TODO Auto-generated method stub
		return carrelloRptDao.getCarrelloRptByRpEId(mygovCarrelloRpId);
	}



	@Override
	public MygovCarrelloRpt getCarrelloRptByIdSession(String nodoSPCFespIdSession) {
		// TODO Auto-generated method stub
		return carrelloRptDao.getCarrelloRptByIdSession(nodoSPCFespIdSession);
	}



	@Override
	public MygovCarrelloRp getCarrelloRpById(Long mygovCarrelloRpId) {
		// TODO Auto-generated method stub
		return carrelloRpDao.getCarrelloRpById(mygovCarrelloRpId);
	}



	public FespBean getFespProperties() {
		return fespProperties;
	}



	public void setFespProperties(FespBean fespProperties) {
		this.fespProperties = fespProperties;
	}




}
