package it.regioneveneto.mygov.payment.nodoregionalefesp.service;

import java.math.BigDecimal;

import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.EsitoAvvisoDigitaleCompletoDto;

public interface NodoInviaAvvisoDigitaleService {

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	EsitoAvvisoDigitaleCompletoDto nodoInviaAvvisoDigitale(String identificativoDominioHeader, String identificativoIntermediarioPAHeader,
			String identificativoStazioneIntermediarioPAHeader, String identificativoDominio,
			String anagraficaBeneficiario, String identificativoMessaggioRichiesta, String tassonomiaAvviso,
			String codiceAvviso, String anagraficaPagatore, String codiceIdentificativoUnivoco,
			String tipoIdentificativoUnivoco, XMLGregorianCalendar dataScadenzaPagamento,
			XMLGregorianCalendar dataScadenzaAvviso, BigDecimal importoAvviso, String eMailSoggetto,
			String cellulareSoggetto, String descrizionePagamento, String urlAvviso, String ibanAccredito, String ibanAppoggio,
			String tipoPagamento, String tipoOperazione);

}
