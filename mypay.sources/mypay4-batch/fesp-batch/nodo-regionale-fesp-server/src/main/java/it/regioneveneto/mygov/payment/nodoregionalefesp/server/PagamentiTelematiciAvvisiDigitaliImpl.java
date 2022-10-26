package it.regioneveneto.mygov.payment.nodoregionalefesp.server;

import java.util.Date;

import javax.jws.WebService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.regioneveneto.mygov.payment.constants.Constants;
import it.regioneveneto.mygov.payment.constants.FaultCodeConstants;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.utils.FespBean;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.EsitoAvvisoDigitaleCompletoDto;
import it.regioneveneto.mygov.payment.nodoregionalefesp.dto.EsitoAvvisoDigitaleDto;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.GiornaleService;
import it.regioneveneto.mygov.payment.nodoregionalefesp.service.NodoInviaAvvisoDigitaleService;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.avvisidigitali.CtAvvisoDigitale;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.avvisidigitali.CtEsitoAvvisatura;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.avvisidigitali.CtEsitoAvvisoDigitale;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.avvisidigitali.CtFaultBean;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.avvisidigitali.CtNodoSILInviaAvvisoDigitale;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.avvisidigitali.CtNodoSILInviaAvvisoDigitaleRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.avvisidigitali.IntestazionePPT;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.avvisidigitali.StEsitoOperazione;
import it.veneto.regione.pagamenti.nodoregionalefesp.pagamentitelematiciavvisidigitali.PagamentiTelematiciAvvisiDigitali;

@WebService(serviceName = "PagamentiTelematiciAvvisiDigitaliService", portName = "PPTPort", targetNamespace = "http://_URL_ENTE_/pagamenti/nodoregionalefesp/PagamentiTelematiciAvvisiDigitali", wsdlLocation = "classpath:it/regioneveneto/mygov/payment/nodoregionalefesp/server/nodo-regionale-per-pa-avvisi-digitali.wsdl", endpointInterface = "it.veneto.regione.pagamenti.nodoregionalefesp.pagamentitelematiciavvisidigitali.PagamentiTelematiciAvvisiDigitali")
public class PagamentiTelematiciAvvisiDigitaliImpl implements PagamentiTelematiciAvvisiDigitali {

	private static final Log LOG = LogFactory.getLog(PagamentiTelematiciAvvisiDigitaliImpl.class);

	private GiornaleService giornaleService;

	private FespBean fespProperties;

	private NodoInviaAvvisoDigitaleService nodoInviaAvvisoDigitaleService;

	public void setGiornaleService(GiornaleService giornaleService) {
		this.giornaleService = giornaleService;
	}

	public void setFespProperties(FespBean fespProperties) {
		this.fespProperties = fespProperties;
	}

	public void setNodoInviaAvvisoDigitaleService(NodoInviaAvvisoDigitaleService nodoInviaAvvisoDigitaleService) {
		this.nodoInviaAvvisoDigitaleService = nodoInviaAvvisoDigitaleService;
	}

	public CtNodoSILInviaAvvisoDigitaleRisposta nodoSILInviaAvvisoDigitale(IntestazionePPT header,
			CtNodoSILInviaAvvisoDigitale bodyrichiesta) {
		LOG.info("Executing operation nodoSILInviaAvvisoDigitale");

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
			idDominio = header.getIdentificativoDominio();
			identificativoUnivocoVersamento = "";
			codiceContestoPagamento = "n/a";
			identificativoPrestatoreServiziPagamento = "";
			tipoVers = null;
			componente = Constants.COMPONENTE.FESP.toString();
			categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
			tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoSILInviaAvvisoDigitale.toString();
			sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.REQUEST.toString();

			identificativoFruitore = header.getIdentificativoDominio();
			identificativoErogatore = fespProperties.getIdentificativoStazioneIntermediarioPa();
			identificativoStazioneIntermediarioPa = fespProperties.getIdentificativoStazioneIntermediarioPa();
			canalePagamento = "";

			parametriSpecificiInterfaccia = "Parametri di richiesta verso il Nodo SPC: " 
					+ "identificativoDominioHeader [ "+ header.getIdentificativoDominio() + " ], "
					+ "identificativoIntermediarioPAHeader [ "+ header.getIdentificativoIntermediarioPA() + " ], "
					+ "identificativoStazioneIntermediarioPAHeader [ "+ header.getIdentificativoStazioneIntermediarioPA() + " ], "
					+ "identificativoDominioAvviso [ "+ bodyrichiesta.getAvvisoDigitaleWS().getIdentificativoDominio() + " ], "
					+ "anagraficaBeneficiario [ "+ bodyrichiesta.getAvvisoDigitaleWS().getAnagraficaBeneficiario()+ " ], "
					+ "identificativoMessaggioRichiesta [ "+ bodyrichiesta.getAvvisoDigitaleWS().getIdentificativoMessaggioRichiesta()+ " ], "
					+ "tassonomiaAvviso [ " + bodyrichiesta.getAvvisoDigitaleWS().getTassonomiaAvviso()+ " ], "
					+ "codiceAvviso [ " + bodyrichiesta.getAvvisoDigitaleWS().getCodiceAvviso()+ " ], "
					+ "anagraficaPagatore [ "+ bodyrichiesta.getAvvisoDigitaleWS().getSoggettoPagatore().getAnagraficaPagatore()+ " ], "
					+ "codiceIdentificativoUnivocoPagatore [ "+ bodyrichiesta.getAvvisoDigitaleWS().getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco()+ " ], "
					+ "tipoIdentificativoUnivocoPagatore [ "+ bodyrichiesta.getAvvisoDigitaleWS().getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString()+ " ], "
					+ "dataScadenzaPagamento [ " + bodyrichiesta.getAvvisoDigitaleWS().getDataScadenzaPagamento()+ " ], "
					+ "dataScadenzaAvviso [ " + bodyrichiesta.getAvvisoDigitaleWS().getDataScadenzaAvviso()+ " ], "
					+ "importoAvviso [ " + bodyrichiesta.getAvvisoDigitaleWS().getImportoAvviso()+ " ], "
					+ "emailSoggetto [ " + bodyrichiesta.getAvvisoDigitaleWS().getEMailSoggetto()+ " ], "
					+ "cellulareSoggetto [ " + bodyrichiesta.getAvvisoDigitaleWS().getCellulareSoggetto()+ " ], "
					+ "descrizionePagamento [ " + bodyrichiesta.getAvvisoDigitaleWS().getDescrizionePagamento()+ " ], "
					+ "urlAvviso [ " + bodyrichiesta.getAvvisoDigitaleWS().getUrlAvviso() + " ], " 
					+ "datiSingoloVersamentoIbanAccredito [ " + bodyrichiesta.getAvvisoDigitaleWS().getDatiSingoloVersamento().get(0).getIbanAccredito() + " ], "
					+ "datiSingoloVersamentoIbanAppoggio [ " + bodyrichiesta.getAvvisoDigitaleWS().getDatiSingoloVersamento().get(0).getIbanAppoggio() + " ], "
					+ "tipoPagamento [ " + bodyrichiesta.getAvvisoDigitaleWS().getTipoPagamento() + " ], " 
					+ "tipoOperazione [ " + bodyrichiesta.getAvvisoDigitaleWS().getTipoOperazione().value() + " ]";
				
			esitoReq = Constants.GIORNALE_ESITO_EVENTO.OK.toString();

			giornaleService.registraEvento(dataOraEvento, idDominio, identificativoUnivocoVersamento,
					codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVers, componente,
					categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
					identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia, esitoReq);
		} catch (Exception e1) {
			LOG.warn("nodoSILInviaAvvisoDigitale REQUEST impossibile inserire nel giornale degli eventi", e1);
		}

		try {

			String identificativoDominioHeader = header.getIdentificativoDominio();
			String identificativoIntermediarioPAHeader = header.getIdentificativoIntermediarioPA();
			String identificativoStazioneIntermediarioPAHeader = header.getIdentificativoStazioneIntermediarioPA();

			CtAvvisoDigitale avvisoDigitale = bodyrichiesta.getAvvisoDigitaleWS();

			EsitoAvvisoDigitaleCompletoDto responseDto = nodoInviaAvvisoDigitaleService.nodoInviaAvvisoDigitale(
					identificativoDominioHeader, identificativoIntermediarioPAHeader,
					identificativoStazioneIntermediarioPAHeader, avvisoDigitale.getIdentificativoDominio(),
					avvisoDigitale.getAnagraficaBeneficiario(), avvisoDigitale.getIdentificativoMessaggioRichiesta(),
					avvisoDigitale.getTassonomiaAvviso(), avvisoDigitale.getCodiceAvviso(),
					avvisoDigitale.getSoggettoPagatore().getAnagraficaPagatore(),
					avvisoDigitale.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco(),
					avvisoDigitale.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString(),
					avvisoDigitale.getDataScadenzaPagamento(), avvisoDigitale.getDataScadenzaAvviso(),
					avvisoDigitale.getImportoAvviso(), avvisoDigitale.getEMailSoggetto(),
					avvisoDigitale.getCellulareSoggetto(), avvisoDigitale.getDescrizionePagamento(),
					avvisoDigitale.getUrlAvviso(), avvisoDigitale.getDatiSingoloVersamento().get(0).getIbanAccredito(), 
					avvisoDigitale.getDatiSingoloVersamento().get(0).getIbanAppoggio(), avvisoDigitale.getTipoPagamento(),
					avvisoDigitale.getTipoOperazione().value());

			if (responseDto.getFaultBeanDto() != null) {
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
					tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoSILInviaAvvisoDigitale.toString();
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

				CtNodoSILInviaAvvisoDigitaleRisposta response = new CtNodoSILInviaAvvisoDigitaleRisposta();
				CtFaultBean faultBean = new CtFaultBean();
				faultBean.setId(responseDto.getFaultBeanDto().getFaultId());
				faultBean.setFaultCode(responseDto.getFaultBeanDto().getFaultCode());
				faultBean.setFaultString(responseDto.getFaultBeanDto().getFaultString());
				faultBean.setDescription(responseDto.getFaultBeanDto().getFaultDescription());
				faultBean.setSerial(responseDto.getFaultBeanDto().getFaultSerial());
				response.setFault(faultBean);
				return response;
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
					tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoSILInviaAvvisoDigitale.toString();
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
				
				CtNodoSILInviaAvvisoDigitaleRisposta response = new CtNodoSILInviaAvvisoDigitaleRisposta();
				
				StEsitoOperazione stEsitoOperazione = StEsitoOperazione.fromValue(responseDto.getEsitoOperazione());
				
				CtEsitoAvvisoDigitale ctEsitoAvvisoDigitale = new CtEsitoAvvisoDigitale();
				ctEsitoAvvisoDigitale.setIdentificativoDominio(responseDto.getIdentificativoDominio());
				ctEsitoAvvisoDigitale.setIdentificativoMessaggioRichiesta(responseDto.getIdentificativoMessaggioRichiesta());
				
				for(EsitoAvvisoDigitaleDto esitoDto : responseDto.getListaEsitiAvvisiDigitali()) {
					CtEsitoAvvisatura esitoAvvisatura = new CtEsitoAvvisatura();
					esitoAvvisatura.setTipoCanaleEsito(esitoDto.getTipoCanaleEsito());
					esitoAvvisatura.setIdentificativoCanale(esitoDto.getIdentificativoCanale());
					esitoAvvisatura.setDataEsito(esitoDto.getDataEsito());
					esitoAvvisatura.setCodiceEsito(esitoDto.getCodiceEsito());
					esitoAvvisatura.setDescrizioneEsito(esitoDto.getDescrizioneEsito());
					ctEsitoAvvisoDigitale.getEsitoAvvisatura().add(esitoAvvisatura);
				}
				
				response.setEsitoOperazione(stEsitoOperazione);
				response.setEsitoAvvisoDigitaleWS(ctEsitoAvvisoDigitale);
				return response;
			}
		} catch (Exception ex) {
			LOG.error(FaultCodeConstants.PAA_SYSTEM_ERROR + ": [" + ex.getMessage() + "]", ex);

			CtFaultBean fault = new CtFaultBean();
			fault.setFaultCode(FaultCodeConstants.PAA_SYSTEM_ERROR);
			fault.setSerial(1);
			fault.setId(FaultCodeConstants.PAA_NODO_SIL_INVIA_AVVISO_DIGITALE);
			if (ex.getMessage().length() > 256) {
				fault.setDescription(ex.getMessage().substring(0, 256));
				fault.setFaultString(ex.getMessage().substring(0, 256));
			} else {
				fault.setDescription(ex.getMessage());
				fault.setFaultString(ex.getMessage());
			}

			CtNodoSILInviaAvvisoDigitaleRisposta response = new CtNodoSILInviaAvvisoDigitaleRisposta();
			response.setFault(fault);

			try {
				dataOraEvento = new Date();
				idDominio = header.getIdentificativoDominio();
				identificativoUnivocoVersamento = "";
				codiceContestoPagamento = "n/a";
				identificativoPrestatoreServiziPagamento = "";
				tipoVers = null;
				componente = Constants.COMPONENTE.FESP.toString();
				categoriaEvento = Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString();
				tipoEvento = Constants.GIORNALE_TIPO_EVENTO.nodoSILInviaAvvisoDigitale.toString();
				sottoTipoEvento = Constants.GIORNALE_SOTTOTIPO_EVENTO.RESPONSE.toString();

				identificativoFruitore = header.getIdentificativoDominio();
				identificativoErogatore = fespProperties.getIdentificativoStazioneIntermediarioPa();
				identificativoStazioneIntermediarioPa = fespProperties.getIdentificativoStazioneIntermediarioPa();
				canalePagamento = "";

				parametriSpecificiInterfaccia = "Fault Bean: faultCode [ " + fault.getFaultCode() + " ], faultString [ "
						+ fault.getFaultString() + " ], faultDescription [ " + fault.getDescription() + " ]";

				esitoReq = Constants.GIORNALE_ESITO_EVENTO.KO.toString();

				giornaleService.registraEvento(dataOraEvento, idDominio, identificativoUnivocoVersamento,
						codiceContestoPagamento, identificativoPrestatoreServiziPagamento, tipoVers, componente,
						categoriaEvento, tipoEvento, sottoTipoEvento, identificativoFruitore, identificativoErogatore,
						identificativoStazioneIntermediarioPa, canalePagamento, parametriSpecificiInterfaccia,
						esitoReq);
			} catch (Exception e1) {
				LOG.warn("nodoSILInviaAvvisoDigitale RESPONSE impossibile inserire nel giornale degli eventi", e1);
			}

			return response;
		}
	}

}
