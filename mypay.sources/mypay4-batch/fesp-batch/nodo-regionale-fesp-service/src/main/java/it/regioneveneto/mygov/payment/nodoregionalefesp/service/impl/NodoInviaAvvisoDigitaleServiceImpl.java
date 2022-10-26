package it.regioneveneto.mygov.payment.nodoregionalefesp.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.telematici.pagamenti.ws.nodospcfesp.avvisaturadigitale.CtAvvisoDigitale;
import gov.telematici.pagamenti.ws.nodospcfesp.avvisaturadigitale.CtEsitoAvvisatura;
import gov.telematici.pagamenti.ws.nodospcfesp.avvisaturadigitale.CtIdentificativoUnivocoPersonaFG;
import gov.telematici.pagamenti.ws.nodospcfesp.avvisaturadigitale.CtNodoInviaAvvisoDigitale;
import gov.telematici.pagamenti.ws.nodospcfesp.avvisaturadigitale.CtNodoInviaAvvisoDigitaleRisposta;
import gov.telematici.pagamenti.ws.nodospcfesp.avvisaturadigitale.CtSoggettoPagatore;
import gov.telematici.pagamenti.ws.nodospcfesp.avvisaturadigitale.StTipoIdentificativoUnivocoPersFG;
import gov.telematici.pagamenti.ws.nodospcfesp.avvisaturadigitale.StTipoOperazione;
import gov.telematici.pagamenti.ws.nodospcfesp.avvisaturadigitale.CtDatiSingoloVersamento;
import gov.telematici.pagamenti.ws.sachead.IntestazionePPT;
import it.regioneveneto.mygov.payment.constants.Constants;
import it.regioneveneto.mygov.payment.constants.FaultCodeConstants;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.utils.FespBean;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.EsitoAvvisoDigitaleCompletoDto;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.EsitoAvvisoDigitaleDto;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.FaultBeanDto;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.GiornaleService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.NodoInviaAvvisoDigitaleService;
import it.regioneveneto.mygov.payment.nodospcfesp.avvisaturadigitale.client.PagamentiTelematiciAvvisiDigitaliServiceClient;

public class NodoInviaAvvisoDigitaleServiceImpl implements NodoInviaAvvisoDigitaleService {
	
	private static final Log LOG = LogFactory.getLog(NodoInviaAvvisoDigitaleServiceImpl.class);

	private PagamentiTelematiciAvvisiDigitaliServiceClient pagamentiTelematiciAvvisiDigitaliServiceClient;

	private FespBean fespProperties;

	private GiornaleService giornaleService;

	public void setPagamentiTelematiciAvvisiDigitaliServiceClient(
			PagamentiTelematiciAvvisiDigitaliServiceClient pagamentiTelematiciAvvisiDigitaliServiceClient) {
		this.pagamentiTelematiciAvvisiDigitaliServiceClient = pagamentiTelematiciAvvisiDigitaliServiceClient;
	}

	public void setFespProperties(FespBean fespProperties) {
		this.fespProperties = fespProperties;
	}

	public void setGiornaleService(GiornaleService giornaleService) {
		this.giornaleService = giornaleService;
	}

	public EsitoAvvisoDigitaleCompletoDto nodoInviaAvvisoDigitale(String identificativoDominioHeader,
			String identificativoIntermediarioPAHeader, String identificativoStazioneIntermediarioPAHeader,
			String identificativoDominio, String anagraficaBeneficiario, String identificativoMessaggioRichiesta,
			String tassonomiaAvviso, String codiceAvviso, String anagraficaPagatore, String codiceIdentificativoUnivoco,
			String tipoIdentificativoUnivoco, XMLGregorianCalendar dataScadenzaPagamento,
			XMLGregorianCalendar dataScadenzaAvviso, BigDecimal importoAvviso, String eMailSoggetto,
			String cellulareSoggetto, String descrizionePagamento, String urlAvviso, String ibanAccredito, String ibanAppoggio,
			String tipoPagamento, String tipoOperazione) {

		Date dataOraEvento;
		String idDominio;
		String identificativoUnivocoVersamento;
		String codiceContestoPagamento;
		String identificativoPrestatoreServiziPagamento;
		String tipoVers;
		String componente;
		String categoriaEvento;
		String tipoEvento;
		String sottoTipoEvento;
		String identificativoFruitore;
		String identificativoErogatore;
		String identificativoStazioneIntermediarioPa;
		String canalePagamento;
		String parametriSpecificiInterfaccia;
		String esitoReq;
		/**
		 * LOG nel Giornale degli Eventi della richiesta
		 */
		try {
			dataOraEvento = new Date();
			idDominio = identificativoDominioHeader;
			identificativoUnivocoVersamento = "";
			codiceContestoPagamento = "n/a";
			identificativoPrestatoreServiziPagamento = "";
			tipoVers = null;
			componente = Constants.COMPONENTE.FESP.toString();
			categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
			tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoInviaAvvisoDigitale.toString();
			sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQUEST.toString();

			identificativoFruitore = identificativoDominioHeader;
			identificativoErogatore = fespProperties.getIdentificativoStazioneIntermediarioPa();
			identificativoStazioneIntermediarioPa = fespProperties.getIdentificativoStazioneIntermediarioPa();
			canalePagamento = "";

			parametriSpecificiInterfaccia = "Parametri di richiesta verso il Nodo SPC: identificativoDominioHeader [ "
					+ identificativoDominioHeader + " ], identificativoIntermediarioPAHeader [ "
					+ identificativoIntermediarioPAHeader + " ], identificativoStazioneIntermediarioPAHeader [ "
					+ identificativoStazioneIntermediarioPAHeader + " ], identificativoDominioAvviso [ "
					+ identificativoDominio + " ], anagraficaBeneficiario [ " + anagraficaBeneficiario
					+ " ], identificativoMessaggioRichiesta [ " + identificativoMessaggioRichiesta
					+ " ], tassonomiaAvviso [ " + tassonomiaAvviso + " ], codiceAvviso [ " + codiceAvviso
					+ " ], anagraficaPagatore [ " + anagraficaPagatore + " ], codiceIdentificativoUnivocoPagatore [ "
					+ codiceIdentificativoUnivoco + " ] tipoIdentificativoUnivocoPagatore [ "
					+ tipoIdentificativoUnivoco + " ], dataScadenzaPagamento [ " + dataScadenzaPagamento
					+ " ], dataScadenzaAvviso [ " + dataScadenzaAvviso + " ], importoAvviso [ " + importoAvviso
					+ " ], emailSoggetto [ " + eMailSoggetto + " ], cellulareSoggetto [ " + cellulareSoggetto
					+ " ], descrizionePagamento [ " + descrizionePagamento + " ], urlAvviso [ " + urlAvviso + " ], "
					+ "ibanAccredito [ " + ibanAccredito +" ], ibanAppoggio [ " + ibanAppoggio + " ], tipoPagamento [ "
					+ tipoPagamento + " ], tipoOperazione [ " + tipoOperazione + " ]";

			esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

			giornaleService.registraEvento(dataOraEvento, idDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVers, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
		} catch (Exception e1) {
			LOG.warn("nodoSILInviaAvvisoDigitale REQUEST impossibile inserire nel giornale degli eventi", e1);
		}

		/**
		 * Creazione header 
		 */
		IntestazionePPT header = new IntestazionePPT();
		header.setIdentificativoDominio(identificativoDominioHeader);
		header.setIdentificativoIntermediarioPA(identificativoIntermediarioPAHeader);
		header.setIdentificativoStazioneIntermediarioPA(identificativoStazioneIntermediarioPAHeader);

		/**
		 * Creazione request 
		 */
		CtNodoInviaAvvisoDigitale request = new CtNodoInviaAvvisoDigitale();
		CtAvvisoDigitale avvisoDigitale = new CtAvvisoDigitale();
		avvisoDigitale.setIdentificativoDominio(identificativoDominio);
		avvisoDigitale.setAnagraficaBeneficiario(anagraficaBeneficiario);
		avvisoDigitale.setIdentificativoMessaggioRichiesta(identificativoMessaggioRichiesta);
		avvisoDigitale.setTassonomiaAvviso(tassonomiaAvviso);
		avvisoDigitale.setCodiceAvviso(codiceAvviso);

		CtSoggettoPagatore soggPag = new CtSoggettoPagatore();
		soggPag.setAnagraficaPagatore(anagraficaPagatore);
		CtIdentificativoUnivocoPersonaFG identificativoUnivocoPersona = new CtIdentificativoUnivocoPersonaFG();
		identificativoUnivocoPersona.setCodiceIdentificativoUnivoco(codiceIdentificativoUnivoco);
		StTipoIdentificativoUnivocoPersFG tipo = StTipoIdentificativoUnivocoPersFG.fromValue(tipoIdentificativoUnivoco);
		identificativoUnivocoPersona.setTipoIdentificativoUnivoco(tipo);
		soggPag.setIdentificativoUnivocoPagatore(identificativoUnivocoPersona);
		avvisoDigitale.setSoggettoPagatore(soggPag);

		avvisoDigitale.setDataScadenzaPagamento(dataScadenzaPagamento);
		avvisoDigitale.setDataScadenzaAvviso(dataScadenzaAvviso);
		avvisoDigitale.setImportoAvviso(importoAvviso);
		avvisoDigitale.setEMailSoggetto(eMailSoggetto);
		avvisoDigitale.setCellulareSoggetto(cellulareSoggetto);
		avvisoDigitale.setDescrizionePagamento(descrizionePagamento);
		avvisoDigitale.setUrlAvviso(urlAvviso);
		
		CtDatiSingoloVersamento ctDtSngPag = new CtDatiSingoloVersamento();
		ctDtSngPag.setIbanAccredito(ibanAccredito);
		ctDtSngPag.setIbanAppoggio(ibanAppoggio);
		avvisoDigitale.getDatiSingoloVersamento().add(ctDtSngPag);
		
		avvisoDigitale.setTipoPagamento(tipoPagamento);
		
		StTipoOperazione ope = StTipoOperazione.fromValue(tipoOperazione);
		avvisoDigitale.setTipoOperazione(ope);

		request.setAvvisoDigitaleWS(avvisoDigitale);

		request.setPassword(fespProperties.getPassword());

		try{
			CtNodoInviaAvvisoDigitaleRisposta response = pagamentiTelematiciAvvisiDigitaliServiceClient
					.nodoInviaAvvisoDigitale(header, request);
			EsitoAvvisoDigitaleCompletoDto responseDto = mapXMLResponseToDto(header, response);
			
			if(responseDto.getFaultBeanDto() != null) {
				// RESPONSE FAULT BEAN
				try {
					dataOraEvento = new Date();
					idDominio = header.getIdentificativoDominio();
					identificativoUnivocoVersamento = "";
					codiceContestoPagamento = "n/a";
					identificativoPrestatoreServiziPagamento = "";
					tipoVers = null;
					componente = Constants.COMPONENTE.FESP.toString();
					categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
					tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoInviaAvvisoDigitale.toString();
					sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();

					identificativoFruitore = header.getIdentificativoDominio();
					identificativoErogatore = fespProperties.getIdentificativoStazioneIntermediarioPa();
					identificativoStazioneIntermediarioPa = fespProperties.getIdentificativoStazioneIntermediarioPa();
					canalePagamento = "";

					parametriSpecificiInterfaccia = "Parametri di risposta dal Nodo SPC: faultId [ "
							+ responseDto.getFaultBeanDto().getFaultId() + " ], faultCode [ "
							+ responseDto.getFaultBeanDto().getFaultCode() + " ], faultString[ "
							+ responseDto.getFaultBeanDto().getFaultString() + " ], faultDescription [ "
							+ responseDto.getFaultBeanDto().getFaultDescription() + " ], faultSerial [ "
							+ responseDto.getFaultBeanDto().getFaultSerial() + " ]";

					esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

					giornaleService.registraEvento(dataOraEvento, idDominio, identificativoUnivocoVersamento,
							codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVers, componente,
							categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
							identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
							parametriSpecificiInterfaccia, esitoReq);
				} catch (Exception e1) {
					LOG.warn("nodoSILInviaAvvisoDigitale RESPONSE impossibile inserire nel giornale degli eventi", e1);
				}
			} else {
				// RESPONSE
				try {
					dataOraEvento = new Date();
					idDominio = header.getIdentificativoDominio();
					identificativoUnivocoVersamento = "";
					codiceContestoPagamento = "n/a";
					identificativoPrestatoreServiziPagamento = "";
					tipoVers = null;
					componente = Constants.COMPONENTE.FESP.toString();
					categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
					tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoInviaAvvisoDigitale.toString();
					sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();

					identificativoFruitore = header.getIdentificativoDominio();
					identificativoErogatore = fespProperties.getIdentificativoStazioneIntermediarioPa();
					identificativoStazioneIntermediarioPa = fespProperties.getIdentificativoStazioneIntermediarioPa();
					canalePagamento = "";

					String esitoString = "";

					for (EsitoAvvisoDigitaleDto esito : responseDto.getListaEsitiAvvisiDigitali()) {
						esitoString += "{tipoCanaleEsito [ " + esito.getTipoCanaleEsito()
								+ " ], identificativoCanale [ " + esito.getIdentificativoCanale() + " ], dataEsito [ "
								+ esito.getDataEsito() + " ], codiceEsito [ " + esito.getCodiceEsito()
								+ " ], descrizioneEsito [ " + esito.getDescrizioneEsito() + " ]}, ";
					}
					esitoString = esitoString.trim();
					if (esitoString.endsWith(","))
						esitoString = esitoString.substring(0, esitoString.length() - 1);

					parametriSpecificiInterfaccia = "Parametri di risposta dal Nodo SPC: identificativoDominioHeader [ "
							+ responseDto.getIdentificativoDominioHeader()
							+ " ], identificativoIntermediarioPAHeader [ "
							+ responseDto.getIdentificativoIntermediarioPAHeader()
							+ " ], identificativoStazioneIntermediarioPAHeader [ "
							+ responseDto.getIdentificativoStazioneIntermediarioPAHeader() + " ], esitoOperazione [ "
							+ responseDto.getEsitoOperazione() + " ], identificativoDominioEsito [ "
							+ responseDto.getIdentificativoDominio() + " ], identificativoMessaggioRichiesta [ "
							+ responseDto.getIdentificativoMessaggioRichiesta() + " ], " + esitoString;

					parametriSpecificiInterfaccia = parametriSpecificiInterfaccia.trim();
					if (parametriSpecificiInterfaccia.endsWith(","))
						parametriSpecificiInterfaccia = parametriSpecificiInterfaccia.substring(0, parametriSpecificiInterfaccia.length() - 1);
					if (parametriSpecificiInterfaccia.length() > Constants.GIORNALE_PARAMETRI_SPECIFICI_INTERFACCIA_MAX_LENGTH)
						parametriSpecificiInterfaccia = parametriSpecificiInterfaccia.substring(0, Constants.GIORNALE_PARAMETRI_SPECIFICI_INTERFACCIA_MAX_LENGTH);

					esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

					giornaleService.registraEvento(dataOraEvento, idDominio, identificativoUnivocoVersamento,
							codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVers, componente,
							categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore,
							identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
							parametriSpecificiInterfaccia, esitoReq);
				} catch (Exception e1) {
					LOG.warn("nodoSILInviaAvvisoDigitale RESPONSE impossibile inserire nel giornale degli eventi", e1);
				}
			}
			
			return responseDto;
		}catch(Exception ex) {
			LOG.error(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [" + ex.getMessage() + "]", ex);

			try {
				dataOraEvento = new Date();
				idDominio = header.getIdentificativoDominio();
				identificativoUnivocoVersamento = "";
				codiceContestoPagamento = "n/a";
				identificativoPrestatoreServiziPagamento = "";
				tipoVers = null;
				componente = Constants.COMPONENTE.FESP.toString();
				categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
				tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoInviaAvvisoDigitale.toString();
				sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();

				identificativoFruitore = header.getIdentificativoDominio();
				identificativoErogatore = fespProperties.getIdentificativoStazioneIntermediarioPa();
				identificativoStazioneIntermediarioPa = fespProperties.getIdentificativoStazioneIntermediarioPa();
				canalePagamento = "";

				parametriSpecificiInterfaccia = ex.getMessage();

				esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

				giornaleService.registraEvento(dataOraEvento, idDominio, identificativoUnivocoVersamento,
						codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVers, componente,
						categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);
			} catch (Exception e1) {
				LOG.warn("nodoSILInviaAvvisoDigitale RESPONSE impossibile inserire nel giornale degli eventi", e1);
			}
			throw ex;
		}
	}

	private EsitoAvvisoDigitaleCompletoDto mapXMLResponseToDto(IntestazionePPT header, CtNodoInviaAvvisoDigitaleRisposta response) {
		EsitoAvvisoDigitaleCompletoDto dto = new EsitoAvvisoDigitaleCompletoDto();

		// HEADER
		dto.setIdentificativoDominioHeader(header.getIdentificativoDominio());
		dto.setIdentificativoIntermediarioPAHeader(header.getIdentificativoIntermediarioPA());
		dto.setIdentificativoStazioneIntermediarioPAHeader(header.getIdentificativoStazioneIntermediarioPA());

		// ESITO OPERAZIONE
		dto.setEsitoOperazione(response.getEsitoOperazione().value());
				
		// FAULT BEAN
		if (response.getFault() != null) {
			FaultBeanDto faultDto = new FaultBeanDto();
			faultDto.setFaultId(response.getFault().getId());
			faultDto.setFaultCode(response.getFault().getFaultCode());
			faultDto.setFaultString(response.getFault().getFaultString());
			faultDto.setFaultDescription(response.getFault().getDescription());
			faultDto.setFaultSerial(response.getFault().getSerial());
			dto.setFaultBeanDto(faultDto);
		}else{
			// ESITO AVVISO
			dto.setIdentificativoDominio(response.getEsitoAvvisoDigitaleWS().getIdentificativoDominio());
			dto.setIdentificativoMessaggioRichiesta(response.getEsitoAvvisoDigitaleWS().getIdentificativoMessaggioRichiesta());
			
			List<EsitoAvvisoDigitaleDto> listaEsiti = new ArrayList<EsitoAvvisoDigitaleDto>();
			
			for (CtEsitoAvvisatura esito : response.getEsitoAvvisoDigitaleWS().getEsitoAvvisatura()) {
				EsitoAvvisoDigitaleDto esitoDto = new EsitoAvvisoDigitaleDto();
	
				esitoDto.setTipoCanaleEsito(esito.getTipoCanaleEsito());
				esitoDto.setIdentificativoCanale(esito.getIdentificativoCanale());
				esitoDto.setDataEsito(esito.getDataEsito());
				esitoDto.setCodiceEsito(esito.getCodiceEsito());
				esitoDto.setDescrizioneEsito(esito.getDescrizioneEsito());
				listaEsiti.add(esitoDto);
			}
			dto.setListaEsitiAvvisiDigitali(listaEsiti);
		}
		
		return dto;
	}

}
