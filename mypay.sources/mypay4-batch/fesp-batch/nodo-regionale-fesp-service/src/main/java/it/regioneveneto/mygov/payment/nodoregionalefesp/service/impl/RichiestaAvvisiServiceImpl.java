package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RichiestaAvvisiDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRichiestaAvvisi;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.RichiestaAvvisiService;

import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtDatiPagamentoPA;
import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtFaultBean;
import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtNumeroAvviso;
import it.veneto.regione.schemas._2012.pagamenti.CtEnteBeneficiario;

public class RichiestaAvvisiServiceImpl implements RichiestaAvvisiService {

	@Autowired
	private RichiestaAvvisiDao richiestaAvvisiDao;

	@Override
	public MygovRichiestaAvvisi creaRichiestaAvvisi(String identificativoIntermediarioPa, String identificativoStazioneIntermediarioPA, String identificativoDominio, String identificativoPSP, String idServizio, String datiSpecificiServizio) {
		MygovRichiestaAvvisi richiestaAvvisi = richiestaAvvisiDao.insertWithRefresh(identificativoIntermediarioPa, identificativoStazioneIntermediarioPA, identificativoDominio, identificativoPSP, idServizio, datiSpecificiServizio);
		return richiestaAvvisi;
	}

	@Override
	public void updateRispostaPA(Long richiestaAvvisiId, String codEsito, CtDatiPagamentoPA datiPagamentoPa, CtNumeroAvviso numeroAvviso, CtFaultBean faultBean) {
		richiestaAvvisiDao.updateEsito(richiestaAvvisiId, codEsito);
		if (datiPagamentoPa != null){
			richiestaAvvisiDao.updateDatiPagamentoPa(richiestaAvvisiId, datiPagamentoPa);
			CtEnteBeneficiario enteBeneficiario = datiPagamentoPa.getEnteBeneficiario();
			if (enteBeneficiario != null)
				richiestaAvvisiDao.updateEnteBeneficiario(richiestaAvvisiId, enteBeneficiario);
		}
		if (numeroAvviso != null)
			richiestaAvvisiDao.updateNumeroAvviso(richiestaAvvisiId, numeroAvviso);
		if (faultBean != null)
			richiestaAvvisiDao.updateFaultBean(richiestaAvvisiId, faultBean);
	}
}
