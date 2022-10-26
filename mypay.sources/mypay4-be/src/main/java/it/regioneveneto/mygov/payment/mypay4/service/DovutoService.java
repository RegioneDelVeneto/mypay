/**
 *     MyPay - Payment portal of Regione Veneto.
 *     Copyright (C) 2022  Regione Veneto
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.regioneveneto.mygov.payment.mypay4.service;

import it.regioneveneto.mygov.payment.mypay4.dao.DovutoDao;
import it.regioneveneto.mygov.payment.mypay4.dto.*;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.NotFoundException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.exception.WSFaultException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.security.JwtTokenUtil;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.MaxResultsHelper;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciRP;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.veneto.regione.pagamenti.ente.FaultBean;
import it.veneto.regione.pagamenti.ente.PaaSILImportaDovutoRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediIUV;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILChiediIUVRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILRichiediRT;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.NodoSILRichiediRTRisposta;
import it.veneto.regione.schemas._2012.pagamenti.ente.Bilancio;
import it.veneto.regione.schemas._2012.pagamenti.ente.CtDatiVersamento;
import it.veneto.regione.schemas._2012.pagamenti.ente.CtSoggettoPagatore;
import it.veneto.regione.schemas._2012.pagamenti.ente.Versamento;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jdbi.v3.core.statement.OutParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato.*;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class DovutoService {

	@Autowired
	private EnteTipoDovutoService enteTipoDovutoService;

	@Autowired
	private EnteService enteService;

	@Autowired
	private DovutoDao dovutoDao;

	@Autowired
	private DefinitionDovutoService defDovutoService;

	@Autowired
	private AvvisoDigitaleService avvisoDigitaleService;

	@Autowired
	private EnteFunzionalitaService enteFunzionalitaService;

	@Autowired
	private DovutoElaboratoService dovutoElaboratoService;

	@Autowired
	private AnagraficaStatoService anagraficaStatoService;

	@Autowired
	private FlussoService flussoService;

	@Autowired
	private AvvisoService avvisoService;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private PagamentiTelematiciRP pagamentiTelematiciRPClient;

	@Autowired
	private MaxResultsHelper maxResultsHelper;

	@Autowired
	private JAXBTransformService jaxbTransformService;

	@Autowired
	private GiornaleService giornalePaService;

	@Autowired
	private UtenteService utenteService;

	@Autowired
	private AsynchAvvisiDigitaliService asynchAvvisiDigitaliService;

	@Autowired
	private JasperService jasperService;

	@Autowired
	private DatiMarcaBolloDigitaleService marcaBolloDigitaleService;

	@Autowired
	private IdentificativoUnivocoService identificativoUnivocoService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private LandingService landingService;

	@Autowired
	private LocationService locationService;
	@Value("${pa.identificativoStazioneIntermediarioPA}")
	private String identificativoStazioneIntermediariaPa;


	private AnagraficaStato mygovAnagraficaStatoDovutoDaPagare = null;

	@PostConstruct
	private void initialize(){
		mygovAnagraficaStatoDovutoDaPagare = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_DA_PAGARE, Constants.STATO_TIPO_DOVUTO);
	}


	public boolean checkAppStatusDovuto() {
		int retries = 3;
		boolean found = false;
		boolean error = false;
		while(!found && retries-->0){
			try{
				found = dovutoDao.getRandomCodiceFiscaleOnDovutoTable()
					.map(cf -> this.searchLastDovuto(cf, 3))
					.map(List::size)
					.map(i -> i>0)
					.orElse(false);
				error = false;
			}catch(Exception e){
				log.warn("checkAppStatusDovuto error [{}]", retries, e);
				found = false;
				error = true;
			}
			log.info("checkAppStatusDovuto retries[{}] error[{}] found[{}]", retries, error, found);
		}
		return !error;
	}


	@Transactional(propagation = Propagation.REQUIRED)
	public Dovuto upsert(Dovuto dovuto) {
		if (dovuto.getMygovDovutoId() == null || this.getById(dovuto.getMygovDovutoId()) == null) {
			long mygovDovutoId = dovutoDao.insert(dovuto);
			dovuto.setMygovDovutoId(mygovDovutoId);
		} else {
			dovutoDao.update(dovuto);
		}
		return dovuto;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(Dovuto dovuto) {
		dovutoDao.delete(dovuto);
	}

	public Dovuto getById(Long id) {
		return dovutoDao.getById(id);
	}

	public DovutoTo getToById(Long id) {
		DovutoTo dovutoTo = dovutoDao.getToById(id);
		return dovutoTo;
	}

	public long count(String codIpaEnte, String codTipoDovuto) {
	  return dovutoDao.count(codIpaEnte, codTipoDovuto);
	}

	public List<Dovuto> getByFlussoId(Long flussoId) {
		return dovutoDao.getByFlussoId(flussoId);
	}

	public List<Dovuto> getUnpaidByEnteIdUnivocoPersona(String codRpSoggPagIdUnivPagTipoIdUnivoco, String codRpSoggPagIdUnivPagCodiceIdUnivoco, String codIpaEnte) {
		Long idStatoDovutoDaPagare = anagraficaStatoService.getIdByTipoAndCode(STATO_TIPO_DOVUTO,"INSERIMENTO_DOVUTO");
		Long idStatoFlussoCaricato = anagraficaStatoService.getIdByTipoAndCode(STATO_TIPO_FLUSSO, "CARICATO");
		return dovutoDao.getUnpaidByEnteIdUnivocoPersona(codRpSoggPagIdUnivPagTipoIdUnivoco, codRpSoggPagIdUnivPagCodiceIdUnivoco, codIpaEnte, idStatoDovutoDaPagare, idStatoFlussoCaricato);
	}

	public Optional<Dovuto> getByIdSession(String idSession) {
		return dovutoDao.getByIdSession(idSession);
	}

	public List<Dovuto> getByIuvEnte(String iuv, String codIpa) {
	  return dovutoDao.getByIuvEnte(iuv, codIpa);
	}

	public Dovuto getByIudEnte(String iud, String codIpa) {
		List<Dovuto> dovuti = dovutoDao.getByIudEnte(iud, codIpa);
		if (CollectionUtils.isEmpty(dovuti)) {
			return null;
		} else if (dovuti.size() > 1) {
			throw new MyPayException(messageSource.getMessage("pa.dovuto.dovutoDuplicato", null, Locale.ITALY));
		}
		return dovuti.get(0);
	}

	public List<DovutoTo> searchDovuto(String codIpaEnte, String idUnivocoPagatoreVersante,
																	 String codTipoDovuto, String causale,
																	 LocalDate dataFrom, LocalDate dataTo) {
		Long idStatoDovutoPredisposto = anagraficaStatoService.getIdByTipoAndCode(STATO_TIPO_DOVUTO,"PREDISPOSTO");
		Long idStatoFlussoCaricato = anagraficaStatoService.getIdByTipoAndCode(STATO_TIPO_FLUSSO, "CARICATO");
		Long idStatoEnteEsercizio = anagraficaStatoService.getIdByTipoAndCode(STATO_ENTE, STATO_ENTE_ESERCIZIO);

		return maxResultsHelper.manageMaxResults(
				maxResults -> dovutoDao.searchDovuto(codIpaEnte, idStatoEnteEsercizio, idUnivocoPagatoreVersante,
						codTipoDovuto, causale, dataFrom, dataTo.plusDays(1), idStatoDovutoPredisposto, idStatoFlussoCaricato, maxResults),
				() -> dovutoDao.searchDovutoCount(codIpaEnte, idStatoEnteEsercizio, idUnivocoPagatoreVersante,
						codTipoDovuto, causale, dataFrom, dataTo.plusDays(1), idStatoDovutoPredisposto, idStatoFlussoCaricato) );
	}

	public Optional<Boolean> hasReplicaDovuto(String codIpaEnte, char tipoIdUnivocoPagatoreVersante,
																						String idUnivocoPagatoreVersante, String causale, String codTipoDovuto) {
		if(StringUtils.isBlank(codIpaEnte) || StringUtils.isBlank(idUnivocoPagatoreVersante)
				|| StringUtils.isBlank(causale) || StringUtils.isBlank(codTipoDovuto))
			return Optional.empty();
		else
			return dovutoDao.hasReplicaDovuto(codIpaEnte, tipoIdUnivocoPagatoreVersante, idUnivocoPagatoreVersante, causale, codTipoDovuto);
	}

	public List<DovutoTo> searchLastDovuto(String idUnivocoPagatoreVersante, Integer num) {
		Long idStatoDovutoPredisposto = anagraficaStatoService.getIdByTipoAndCode(STATO_TIPO_DOVUTO,"PREDISPOSTO");
		Long idStatoFlussoCaricato = anagraficaStatoService.getIdByTipoAndCode(STATO_TIPO_FLUSSO, "CARICATO");
		Long idStatoEnteEsercizio = anagraficaStatoService.getIdByTipoAndCode(STATO_ENTE, STATO_ENTE_ESERCIZIO);

		return dovutoDao.searchLastDovuto(idUnivocoPagatoreVersante, idStatoEnteEsercizio, idStatoDovutoPredisposto, idStatoFlussoCaricato, num);
	}

	public List<DovutoTo> searchDovutoOnTipoAttivo(Long mygovEnteId, String codTipoDovuto, String codIuv,
																								 String codRpSoggPagIdUnivPagCodiceIdUnivoco, String deRpSoggPagAnagraficaPagatore,
																								 Boolean searchOnlyOnTipoAttivo) {
		return dovutoDao.searchDovutoOnTipoAttivo(mygovEnteId, codTipoDovuto, codIuv,
				codRpSoggPagIdUnivPagCodiceIdUnivoco, deRpSoggPagAnagraficaPagatore, searchOnlyOnTipoAttivo);
	}

	public List<DovutoOperatoreTo> searchDovutoForOperatore(String username,
																													Long mygovEnteId, LocalDate dataFrom, LocalDate dataTo, String codStato,
																													Long myGovEnteTipoDovutoId, String nomeFlusso, String causale,
																													String codFiscale, String iud, String iuv) {
		List<EnteTipoDovuto> enteTipoDovutoOfOperator = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(mygovEnteId, username);
		List<EnteTipoDovuto> enteTipoDovutoFiltered;
		if (myGovEnteTipoDovutoId != null) {
			//check if operatore is authorized for tipoDovuto
			enteTipoDovutoFiltered = enteTipoDovutoOfOperator.stream().filter(etd -> etd.getMygovEnteTipoDovutoId().equals(myGovEnteTipoDovutoId)).collect(Collectors.toList());
		} else {
			enteTipoDovutoFiltered = enteTipoDovutoOfOperator;
		}
		//if user is not authorized on any tipoDovuto, return empty list
		if (enteTipoDovutoFiltered.isEmpty())
			return Collections.emptyList();

		List<String> listCodTipoDovuto = enteTipoDovutoFiltered.stream().map(enteTipoDovuto -> enteTipoDovuto.getCodTipo()).collect(Collectors.toList());
		List<String> listCodTipoDovutoDataNonObbl = enteTipoDovutoService.getAttiviByMygovEnteIdAndFlags(mygovEnteId, null, false)
				.stream().distinct().filter(enteTipoDovutoFiltered::contains)
				.map(enteTipoDovuto -> enteTipoDovuto.getCodTipo()).collect(Collectors.toList());
		String codStatoPagamento;
		final boolean notEnforceDataScadenza;
		if("daPagare".equals(codStato)){
			codStatoPagamento = AnagraficaStato.STATO_DOVUTO_DA_PAGARE;
			if(dataFrom.isBefore(LocalDate.now()))
				dataFrom = LocalDate.now();
			notEnforceDataScadenza = true;
		} else if ("pagamentoIniziato".equals(codStato)){
			codStatoPagamento = AnagraficaStato.STATO_DOVUTO_PAGAMENTO_INIZIATO;
			notEnforceDataScadenza = true;
		} else if ("scaduto".equals(codStato)){
			codStatoPagamento = AnagraficaStato.STATO_DOVUTO_DA_PAGARE;
			if(dataTo.isAfter(LocalDate.now().minus(1, ChronoUnit.DAYS)))
				dataTo = LocalDate.now().minus(1, ChronoUnit.DAYS);
			notEnforceDataScadenza = false;
		} else {
			codStatoPagamento = null;
			notEnforceDataScadenza = true;
		}
		final LocalDate dataToAdjusted = dataTo.plusDays(1);
		final LocalDate dataFromFinal = dataFrom;

    final Long idStatoDovuto = codStatoPagamento!=null ? anagraficaStatoService.getIdByTipoAndCode(STATO_TIPO_DOVUTO, codStatoPagamento) : null;

		return maxResultsHelper.manageMaxResults(
				maxResults -> dovutoDao.searchDovutoForOperatore(mygovEnteId, dataFromFinal, dataToAdjusted, idStatoDovuto,
						notEnforceDataScadenza, nomeFlusso, causale, codFiscale,
						iud, iuv, listCodTipoDovuto, listCodTipoDovutoDataNonObbl, maxResults),
				() -> dovutoDao.searchDovutoForOperatoreCount(mygovEnteId, dataFromFinal, dataToAdjusted, idStatoDovuto,
						notEnforceDataScadenza, nomeFlusso, causale, codFiscale,
						iud, iuv, listCodTipoDovuto, listCodTipoDovutoDataNonObbl) );
	}

	public DovutoOperatoreTo getDetailsForOperatore(String username, Long mygovEnteId, Long mygovDovutoId) {
		List<EnteTipoDovuto> enteTipoDovutoOfOperator = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(mygovEnteId, username);
		List<String> listCodTipoDovuto = enteTipoDovutoOfOperator.stream().map(enteTipoDovuto -> enteTipoDovuto.getCodTipo()).collect(Collectors.toList());
		return dovutoDao.getDovutoDetailsOperatore(listCodTipoDovuto, mygovDovutoId, "true");
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public DovutoOperatoreTo updateDovuto(String username, Long mygovEnteId, Long mygovDovutoId, DovutoOperatoreTo newDovuto) {

		List<EnteTipoDovuto> enteTipoDovutoOfOperator = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(mygovEnteId, username);
		List<String> listCodTipoDovuto = enteTipoDovutoOfOperator.stream().map(enteTipoDovuto -> enteTipoDovuto.getCodTipo()).collect(Collectors.toList());
		DovutoOperatoreTo authorizedDovuto = dovutoDao.getDovutoDetailsOperatore(listCodTipoDovuto, mygovDovutoId, "true");
		if (authorizedDovuto == null || !authorizedDovuto.getId().equals(newDovuto.getId())) {
			throw new ValidatorException("EnteTipoDovuto non e' attivo per l'operatore.");
		}

		EnteTipoDovuto enteTipoDovuto = enteTipoDovutoOfOperator.stream().filter(etd -> etd.getCodTipo()
				.equals(newDovuto.getTipoDovuto().getCodTipo())).collect(Collectors.toList()).get(0);

		Ente ente = enteService.getEnteById(mygovEnteId);
		Dovuto dovutoInDB = dovutoDao.getById(authorizedDovuto.getId());

		/**
		 * Caso in cui il dovuto è stato pagato
		 */
		if (!dovutoInDB.getCodIud().equals(newDovuto.getIud())) {
			throw new ValidatorException(messageSource.getMessage("pa.gestioneDovuto.dovuto.errore.dovuto.pagato", null, Locale.ITALY));
		}

		/**
		 * Verifico che non sia iniziata la transazione di pagamento del dovuto.
		 */
		if ( dovutoInDB.getMygovAnagraficaStatoId() == null ||
				!Constants.STATO_TIPO_DOVUTO.equals(dovutoInDB.getMygovAnagraficaStatoId().getDeTipoStato()) ||
				!Constants.STATO_DOVUTO_DA_PAGARE.equals(dovutoInDB.getMygovAnagraficaStatoId().getCodStato()) ||
				dovutoInDB.getMygovCarrelloId() != null && dovutoInDB.getMygovCarrelloId().getMygovCarrelloId() !=null ) {
				throw new ValidatorException(messageSource.getMessage("pa.gestioneDovuto.dovuto.errore.dovuto.in.pagamento", null, Locale.ITALY));
			}

		validazioneFormaleDovuto(newDovuto, dovutoInDB, enteTipoDovuto);

		validazioneBusinessDovuto(newDovuto, dovutoInDB, enteTipoDovuto);

		if (newDovuto.getAnagrafica() != null)
			dovutoInDB.setCodRpSoggPagIdUnivPagTipoIdUnivoco(newDovuto.getTipoSoggetto().charAt(0));
		dovutoInDB.setCodRpDatiVersTipoVersamento(ente.getCodRpDatiVersTipoVersamento());
		BigDecimal importo;

		if (enteTipoDovuto.getImporto() != null)
			importo = enteTipoDovuto.getImporto();
		else
			importo = new BigDecimal(newDovuto.getImporto());


		dovutoInDB.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(importo);
		dovutoInDB.setNumRpDatiVersDatiSingVersCommissioneCaricoPa(ente.getNumRpDatiVersDatiSingVersCommissioneCaricoPa());
		//dovutoInDB.setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione("9/" + dovutoInDB.getCodTipoDovuto());

		String deBilancio = null;
		if (StringUtils.isNotBlank(enteTipoDovuto.getDeBilancioDefault())) {
			try {
				deBilancio = defDovutoService.calculateBilancio(true, enteTipoDovuto.getDeBilancioDefault(), null, importo, null);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		dovutoInDB.setBilancio(deBilancio);

		if(newDovuto.isFlgGenerateIuv()) {
			String iuv = generateIUV(ente, importo.toString(), Constants.IUV_GENERATOR_17, Constants.ALL_PAGAMENTI);
			dovutoInDB.setCodIuv(iuv);
		}

		int updatedRec = dovutoDao.update(dovutoInDB);

		if (updatedRec != 1) {
			throw new MyPayException("Errore interno aggiornamento  dovuto");
		}
		return this.getDetailsForOperatore(username, mygovEnteId, newDovuto.getId());
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public DovutoOperatoreTo insertDovuto(String username, Long mygovEnteId, DovutoOperatoreTo newDovuto) {

		List<EnteTipoDovuto> enteTipoDovutoOfOperator = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(mygovEnteId, username);
		EnteTipoDovuto enteTipoDovuto = enteTipoDovutoOfOperator.stream()
				.filter(etd -> etd.getCodTipo().equals(newDovuto.getTipoDovuto().getCodTipo()))
				.findFirst()
				.orElseThrow(() -> new ValidatorException("EnteTipoDovuto non e' attivo per l'operatore."));

		Dovuto dovutoToInsert = new Dovuto();
		dovutoToInsert.setMygovAnagraficaStatoId(anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_DA_PAGARE,Constants.STATO_TIPO_DOVUTO));
		dovutoToInsert.setFlgDovutoAttuale(true);
		dovutoToInsert.setNumRigaFlusso(0);
		dovutoToInsert.setCodIud(Utilities.getRandomIUD());

		Date now = new Date();
		dovutoToInsert.setDtCreazione(now);
		dovutoToInsert.setDtUltimaModifica(now);

		dovutoToInsert.setCodTipoDovuto(enteTipoDovuto.getCodTipo());

		Ente ente = enteService.getEnteById(mygovEnteId);
		List<Flusso> flussi = flussoService.getByEnte(ente.getMygovEnteId(), true);

		if (flussi == null || flussi.isEmpty())
			throw new MyPayException(messageSource.getMessage("pa.errore.internalError", null, Locale.ITALY));

		dovutoToInsert.setMygovFlussoId(flussi.get(0));

		validazioneFormaleDovuto(newDovuto, dovutoToInsert, enteTipoDovuto);
		validazioneBusinessDovuto(newDovuto, dovutoToInsert, enteTipoDovuto);

		if (newDovuto.getAnagrafica() != null)
			dovutoToInsert.setCodRpSoggPagIdUnivPagTipoIdUnivoco(newDovuto.getTipoSoggetto().charAt(0));
		dovutoToInsert.setCodRpDatiVersTipoVersamento(ente.getCodRpDatiVersTipoVersamento());
		BigDecimal importo;
		if (enteTipoDovuto.getImporto() != null)
			importo = enteTipoDovuto.getImporto();
		else
			importo = new BigDecimal(newDovuto.getImporto());

		dovutoToInsert.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(importo);
		dovutoToInsert.setNumRpDatiVersDatiSingVersCommissioneCaricoPa(ente.getNumRpDatiVersDatiSingVersCommissioneCaricoPa());
		dovutoToInsert.setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(enteTipoDovuto.getCodTassonomico() + enteTipoDovuto.getCodTipo());

		String deBilancio = null;
		if (StringUtils.isNotBlank(enteTipoDovuto.getDeBilancioDefault())) {
			try {
				deBilancio = defDovutoService.calculateBilancio(true, enteTipoDovuto.getDeBilancioDefault(), null, importo, null);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		dovutoToInsert.setBilancio(deBilancio);

		if(newDovuto.isFlgGenerateIuv()) {
			String iuv = generateIUV(ente, importo.toString(), Constants.IUV_GENERATOR_17, Constants.ALL_PAGAMENTI);
			dovutoToInsert.setCodIuv(iuv);
		}

		long newDovutoId = dovutoDao.insert(dovutoToInsert);
		log.info("insert dovuto, new id: "+newDovutoId);

		// manage avviso digitale only if avviso: i.e. has IUV and length in [15 , 17])
		Dovuto dovuto = getById(newDovutoId);
		if ( Utilities.isAvviso(dovuto.getCodIuv()) ) {
			/* AVVISATURA DIGITALE WS */
			asynchAvvisiDigitaliService.manageAvvisoDigitale(
					ente,                              /* Struttura che invia l'avviso Digitale. */
					dovuto.getDeRpSoggPagAnagraficaPagatore(),        /* Indica il nominativo o la ragione sociale del pagatore. */
					dovuto.getDeRpSoggPagEmailPagatore(),          /* Email */
					null,                                /* cellulare soggetto */
					dovuto.getCodIuv(),                        /* IUV dell’avviso di pagamento. */
					dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco() + "",        /* Dato alfanumerico che indica la natura del pagatore (F o G). */
					dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco(),        /* Il codice fiscale o, in alternativa, la partita IVA del pagatore. */
					dovuto.getDtRpDatiVersDataEsecuzionePagamento(),        /* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
					dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento(),  /* L'importo relativo alla somma da versare. */
					dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento(),      /* Testo libero a disposizione dell'Ente per descrivere le motivazioni del pagamento. */
					"I");                              /* Azione richiesta (I = Inserimento */

			/* NOTIFICA PIATTAFORMA IO */
			asynchAvvisiDigitaliService.manageNotificaIO(
					ente,                              /* Struttura che invia l'avviso Digitale. */
					dovuto.getDeRpSoggPagAnagraficaPagatore(),        /* Indica il nominativo o la ragione sociale del pagatore. */
					dovuto.getCodRpSoggPagIdUnivPagTipoIdUnivoco() + "",        /* Dato alfanumerico che indica la natura del pagatore (F o G). */
					dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco(),        /* Il codice fiscale o, in alternativa, la partita IVA del pagatore. */
					dovuto.getDtRpDatiVersDataEsecuzionePagamento(),        /* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
					dovuto.getDtRpDatiVersDataEsecuzionePagamento(),        /* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
					dovuto.getNumRpDatiVersDatiSingVersImportoSingoloVersamento(),  /* L'importo relativo alla somma da versare. */
					dovuto.getDeRpSoggPagEmailPagatore(),          /* Email soggetto */
					dovuto.getDeRpDatiVersDatiSingVersCausaleVersamento(),        /* Testo libero a disposizione dell'Ente per descrivere le motivazioni del pagamento. */
					dovuto.getCodIuv(),                      /* IUV dell’avviso di pagamento. */
					dovuto.getCodTipoDovuto(),                    /* Tipo dovuto */
					"I");
		}

		return this.getDetailsForOperatore(username, mygovEnteId, newDovutoId);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int removeDovuto(String username, Long mygovEnteId, Long mygovDovutoId) {
		int updatedRec = 0;
		Dovuto dovuto = dovutoDao.getById(mygovDovutoId);
		List<EnteTipoDovuto> enteTipoDovutoOfOperator = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(mygovEnteId, username);
		long count = enteTipoDovutoOfOperator.stream().filter(etd -> etd.getCodTipo().equals(dovuto.getCodTipoDovuto())).count();
		if (count == 0)
			new ValidatorException("EnteTipoDovuto non e' attivo per l'operatore.");
		if (Constants.STATO_DOVUTO_PAGAMENTO_INIZIATO.equals(dovuto.getMygovAnagraficaStatoId().getCodStato()))
			new ValidatorException(messageSource.getMessage("pa.debiti.nonAnnullabile", null, Locale.ITALY));

		dovutoElaboratoService.elaborateDovuto(dovuto, Constants.STATO_DOVUTO_DA_PAGARE, Constants.STATO_DOVUTO_ANNULLATO);

		//TODO Check if the data in avvisoDegitale is present and the update is needed.
		try {
			Ente ente = enteService.getEnteById(mygovEnteId);
			List<EnteFunzionalita> listaFunzionalita = enteFunzionalitaService.getAllByCodIpaEnte(ente.getCodIpaEnte(), true);
			if (listaFunzionalita.stream()
					.filter(f -> Constants.FUNZIONALITA_AVVISATURA_DIGITALE.equals(f.getCodFunzionalita())).count() > 0) {
				updatedRec = avvisoDigitaleService.changeStateToAnnullato(dovuto, ente);
			}
		} catch (RuntimeException e) {
			log.warn("Errore cambio di stato nell'anagrafica digitale", e);
			throw e;
		}
		return updatedRec;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int removeDovuto(Dovuto dovuto) {
		int deletedRec = dovutoDao.delete(dovuto);
		if (deletedRec != 1) {
			throw new MyPayException("Dovuto delete internal error");
		}
		log.info("Dovuto with id: "+dovuto.getMygovDovutoId()+" is deleted");
		return deletedRec;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public DovutoFunctionOut callInsertFunction(Long n_mygov_ente_id, Long n_mygov_flusso_id, Integer n_num_riga_flusso,
																 Long n_mygov_anagrafica_stato_id, Long n_mygov_carrello_id, String n_cod_iud, String n_cod_iuv, Date n_dt_creazione,
																 Date n_dt_ultima_modifica, String n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco,
																 String n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco, String n_de_rp_sogg_pag_anagrafica_pagatore,
																 String n_de_rp_sogg_pag_indirizzo_pagatore, String n_de_rp_sogg_pag_civico_pagatore, String n_cod_rp_sogg_pag_cap_pagatore,
																 String n_de_rp_sogg_pag_localita_pagatore, String n_de_rp_sogg_pag_provincia_pagatore, String n_cod_rp_sogg_pag_nazione_pagatore,
																 String n_de_rp_sogg_pag_email_pagatore, Date n_dt_rp_dati_vers_data_esecuzione_pagamento,
																 String n_cod_rp_dati_vers_tipo_versamento, Double n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
																 Double n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, String n_cod_tipo_dovuto,
																 String n_de_rp_dati_vers_dati_sing_vers_causale_versamento, String n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione,
																 Long n_mygov_utente_id, String n_bilancio, boolean insert_avv_dig, boolean n_flg_genera_iuv) {
		OutParameters out = dovutoDao.callInsertFunction(n_mygov_ente_id, n_mygov_flusso_id, n_num_riga_flusso, n_mygov_anagrafica_stato_id, n_mygov_carrello_id, n_cod_iud,
				n_cod_iuv, n_dt_creazione, n_dt_ultima_modifica, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
				n_de_rp_sogg_pag_anagrafica_pagatore, n_de_rp_sogg_pag_indirizzo_pagatore, n_de_rp_sogg_pag_civico_pagatore, n_cod_rp_sogg_pag_cap_pagatore,
				n_de_rp_sogg_pag_localita_pagatore, n_de_rp_sogg_pag_provincia_pagatore, n_cod_rp_sogg_pag_nazione_pagatore, n_de_rp_sogg_pag_email_pagatore,
				n_dt_rp_dati_vers_data_esecuzione_pagamento, n_cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
				n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, n_cod_tipo_dovuto, n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
				n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_mygov_utente_id, n_bilancio, insert_avv_dig, n_flg_genera_iuv);
		return out==null?null:DovutoFunctionOut.builder()
				.result(out.getString("result"))
				.resultDesc(out.getString("result_desc"))
				.build();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public DovutoFunctionOut callModificaFunction(Long n_mygov_ente_id, Long n_mygov_flusso_id, Integer n_num_riga_flusso,
																								Long n_mygov_anagrafica_stato_id, Long n_mygov_carrello_id, String n_cod_iud, String n_cod_iuv, Date n_dt_creazione,
																								String n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, String n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
																								String n_de_rp_sogg_pag_anagrafica_pagatore, String n_de_rp_sogg_pag_indirizzo_pagatore, String n_de_rp_sogg_pag_civico_pagatore,
																								String n_cod_rp_sogg_pag_cap_pagatore, String n_de_rp_sogg_pag_localita_pagatore, String n_de_rp_sogg_pag_provincia_pagatore,
																								String n_cod_rp_sogg_pag_nazione_pagatore, String n_de_rp_sogg_pag_email_pagatore,
																								Date n_dt_rp_dati_vers_data_esecuzione_pagamento, String n_cod_rp_dati_vers_tipo_versamento,
																								Double n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento, Double n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa,
																								String n_cod_tipo_dovuto, String n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
																								String n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, Long n_mygov_utente_id, String n_bilancio, boolean insert_avv_dig, boolean n_flg_genera_iuv) {
		OutParameters out = dovutoDao.callModificaFunction(n_mygov_ente_id, n_mygov_flusso_id, n_num_riga_flusso, n_mygov_anagrafica_stato_id, n_mygov_carrello_id, n_cod_iud,
				n_cod_iuv, n_dt_creazione, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
				n_de_rp_sogg_pag_anagrafica_pagatore, n_de_rp_sogg_pag_indirizzo_pagatore, n_de_rp_sogg_pag_civico_pagatore, n_cod_rp_sogg_pag_cap_pagatore,
				n_de_rp_sogg_pag_localita_pagatore, n_de_rp_sogg_pag_provincia_pagatore, n_cod_rp_sogg_pag_nazione_pagatore, n_de_rp_sogg_pag_email_pagatore,
				n_dt_rp_dati_vers_data_esecuzione_pagamento, n_cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
				n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, n_cod_tipo_dovuto, n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
				n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_mygov_utente_id, n_bilancio, insert_avv_dig, n_flg_genera_iuv);
		return out==null?null:DovutoFunctionOut.builder()
				.result(out.getString("result"))
				.resultDesc(out.getString("result_desc"))
				.build();
	}

	/**
	 * Validazione formale dei campi obbligatori del form
	 *
	 * @param newDovuto:     new dovuto with which inserted/updated.
	 * @param enteTipoDovuto
	 * @return
	 */
	private void validazioneFormaleDovuto(DovutoOperatoreTo newDovuto, Dovuto dovutoInDB, EnteTipoDovuto enteTipoDovuto) {

		if (newDovuto.getTipoDovuto() == null || StringUtils.isBlank(newDovuto.getTipoDovuto().getCodTipo())) {
			log.error("DovutoGestione.html -- Campo codice tipo dovuto non valorizzato");
			throw new ValidatorException(messageSource.getMessage("pa.gestioneDovuto.errore.tipo.dovuto.cod", null, Locale.ITALY));
		}

		if (enteTipoDovuto.isFlgCfAnonimo()) {
			if(newDovuto.isFlgAnagraficaAnonima()) {
				dovutoInDB.setCodRpSoggPagIdUnivPagCodiceIdUnivoco("ANONIMO");
			} else if (StringUtils.isNotBlank(newDovuto.getCodFiscale())) {
				dovutoInDB.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(newDovuto.getCodFiscale());
			} else {
				log.error("DovutoGestione.html -- Campo codice fiscale o p iva non inserita");
				throw new ValidatorException(messageSource.getMessage("pa.gestioneDovuto.errore.flg.anonimo", null, Locale.ITALY));
			}
		} else {
			if (StringUtils.isNotBlank(newDovuto.getCodFiscale()))
				dovutoInDB.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(newDovuto.getCodFiscale());
			else {
				log.error("DovutoGestione.html -- Campo codice fiscale o p iva non inserita, flag anonimo attivo, ma il dovuto non è configurato per esso.");
				throw new ValidatorException(messageSource.getMessage("pa.anagrafica.dovuto.errore.codiceFiscale", null, Locale.ITALY));
			}
		}

		if (StringUtils.isNotBlank(newDovuto.getAnagrafica()))
			dovutoInDB.setDeRpSoggPagAnagraficaPagatore(newDovuto.getAnagrafica());
		else {
			log.error("DovutoGestione.html -- Campo anagrafica non inserito");
			throw new ValidatorException(messageSource.getMessage("pa.anagrafica.dovuto.errore.anagrafica", null, Locale.ITALY));
		}

		if (StringUtils.isNotBlank(newDovuto.getEmail())){
			if(Utilities.isValidEmail(newDovuto.getEmail()))
				dovutoInDB.setDeRpSoggPagEmailPagatore(newDovuto.getEmail());
			else{
				log.error("DovutoGestione.html -- Campo email non valido");
				throw new ValidatorException(messageSource.getMessage("pa.gestioneDovuto.errore.email", null, Locale.ITALY));
			}
		} else {
			dovutoInDB.setDeRpSoggPagEmailPagatore(null);
		}

		if (StringUtils.isNotBlank(newDovuto.getCausale()))
			dovutoInDB.setDeRpDatiVersDatiSingVersCausaleVersamento(newDovuto.getCausale());
		else {
			log.error("DovutoGestione.html -- Campo causale non inserita");
			throw new ValidatorException(messageSource.getMessage("pa.gestioneDovuto.errore.causale", null, Locale.ITALY));
		}

		/**
		 * Data : controllo flag scadenza obbligatoria,
		 *  se true -> la data scadenza è obbligatoria e la sua validità deve essere compresa da oggi in poi;
		 *  se false -> e la data viene inserita, si fa solo controllo del parse della data corretta
		 */

		if (enteTipoDovuto.isFlgScadenzaObbligatoria()) {
			if (newDovuto.getDataScadenza() != null) {
				if (newDovuto.getDataScadenza().isBefore(LocalDate.now())) {
					log.error("DovutoGestione.html -- Campo data scadenza non valida.");
					throw new ValidatorException(messageSource.getMessage("pa.gestioneDovuto.errore.data.scadenza.flag.obbli", null, Locale.ITALY));
				}

				dovutoInDB.setDtRpDatiVersDataEsecuzionePagamento(java.sql.Date.valueOf(newDovuto.getDataScadenza()));
			} else {
				log.error("DovutoGestione.html -- Campo data scadenza non valida, campo obbligatorio.");
				throw new ValidatorException(messageSource.getMessage("pa.gestioneDovuto.errore.data.scadenza.obbligatoria", null, Locale.ITALY));
			}
		} else {
			if (newDovuto.getDataScadenza() != null) {
				if (newDovuto.getDataScadenza().isBefore(LocalDate.now())) {
					log.error("DovutoGestione.html -- Campo data scadenza non valida.");
					throw new ValidatorException(messageSource.getMessage("pa.gestioneDovuto.errore.data.scadenza.flag.obbli", null, Locale.ITALY));
				}
				dovutoInDB.setDtRpDatiVersDataEsecuzionePagamento(java.sql.Date.valueOf(newDovuto.getDataScadenza()));
			}
		}

		try{
			BigDecimal importoFromDovuto = BigDecimal.ZERO;
			if(StringUtils.isNotBlank(newDovuto.getImporto()))
				importoFromDovuto = new BigDecimal(newDovuto.getImporto()).setScale(2);
			if (enteTipoDovuto.getImporto() != null){
				if(!importoFromDovuto.equals(enteTipoDovuto.getImporto()))
					throw new ValidatorException("invalid importo for tipo dovuto");
			} else {
				String errorImporto = Utilities.verificaImporto(importoFromDovuto);
				if(errorImporto!=null)
					throw new ValidatorException(errorImporto);
			}
		}catch(Exception e){
			log.error("invalid importo", e);
			throw new ValidatorException("invalid importo");
		}

		if (StringUtils.isBlank(newDovuto.getImporto())) {
			log.error("DovutoGestione.html -- Campo importo non inserito");
			throw new ValidatorException(messageSource.getMessage("pa.gestioneDovuto.errore.importo", null, Locale.ITALY));
		}
	}

	/**
	 * Controlli elaborati
	 *
	 * @param newDovuto:     new dovuto with which inserted/updated.
	 * @param enteTipoDovuto
	 * @return
	 */
	private void validazioneBusinessDovuto(DovutoOperatoreTo newDovuto, Dovuto dovutoInDB, EnteTipoDovuto enteTipoDovuto) {

		//Dovuto dovuto = (Dovuto) tempMap.get("dovuto");
		/**
		 *  CAP valorizzato controllo che il campo nazione sia valorizzato correttamente
		 */
		if (StringUtils.isNotBlank(newDovuto.getCap())) {
			if (newDovuto.getNazione() == null || StringUtils.isBlank(newDovuto.getNazione().getCodiceIsoAlpha2())) {
				log.error("DovutoGestione.html -- Campo nazione non valido");
				throw new ValidatorException(messageSource.getMessage("pa.anagrafica.nazioneNonValida", null, Locale.ITALY));
			} else {
				if (!Utilities.isValidCAP(newDovuto.getCap(), newDovuto.getNazione().getCodiceIsoAlpha2())) {
					log.error("DovutoGestione.html -- Campo cap non valido");
					throw new ValidatorException(messageSource.getMessage("pa.anagrafica.capNonValido", null, Locale.ITALY));
				}
			}
		}
		dovutoInDB.setCodRpSoggPagCapPagatore(newDovuto.getCap());

		//check nazione is valid
        NazioneTo nazione = null;
		if(newDovuto.getNazione()!=null && StringUtils.isNotBlank(newDovuto.getNazione().getCodiceIsoAlpha2()))
			nazione = Optional.ofNullable(locationService.getNazioneByCodIso(newDovuto.getNazione().getCodiceIsoAlpha2()))
					.orElseThrow(() -> new ValidatorException(messageSource.getMessage("pa.anagrafica.nazioneNonValida", null, Locale.ITALY)));
		dovutoInDB.setCodRpSoggPagNazionePagatore(nazione!=null?nazione.getCodiceIsoAlpha2():null);

        ProvinciaTo provincia = null;
		if (newDovuto.getProv() != null && StringUtils.isNotBlank(newDovuto.getProv().getSigla())){
			if(nazione==null || !nazione.hasProvince())
				throw new ValidatorException(messageSource.getMessage("pa.anagrafica.provinciaNonValid", null, Locale.ITALY));
			provincia = Optional.ofNullable(locationService.getProvinciaBySigla(newDovuto.getProv().getSigla()))
					.orElseThrow(() -> new ValidatorException(messageSource.getMessage("pa.anagrafica.provinciaNonValid", null, Locale.ITALY)));
		}
		dovutoInDB.setDeRpSoggPagProvinciaPagatore(provincia != null ? provincia.getSigla() : null);

		String comune = null;
		if (newDovuto.getComune() != null && StringUtils.isNotBlank(newDovuto.getComune().getComune())) {
			if(provincia==null)
				throw new ValidatorException(messageSource.getMessage("pa.anagrafica.localitaNonValida", null, Locale.ITALY));
			comune = locationService.getComuneByNameAndSiglaProvincia(newDovuto.getComune().getComune(), provincia.getSigla())
					.map(ComuneTo::getComune).orElse(newDovuto.getComune().getComune());
		}
		dovutoInDB.setDeRpSoggPagLocalitaPagatore(comune);

		if (StringUtils.isNotBlank(newDovuto.getIndirizzo())) {
			if (!Utilities.validaIndirizzoAnagrafica(newDovuto.getIndirizzo(), false)) {
				log.error("DovutoGestione.html -- Campo indirizzo non valido");
				throw new ValidatorException(messageSource.getMessage("pa.anagraficaVersante.indirizzoNonValido.psp", null, Locale.ITALY));
			}
		}
		dovutoInDB.setDeRpSoggPagIndirizzoPagatore(newDovuto.getIndirizzo());

		if (StringUtils.isNotBlank(newDovuto.getNumCiv())) {
			if (!Utilities.validaCivicoAnagrafica(newDovuto.getNumCiv(), false)) {
				log.error("DovutoGestione.html -- Campo indirizzo non valido");
				throw new ValidatorException(messageSource.getMessage("pa.anagraficaVersante.civicoNonValido.psp", null, Locale.ITALY));
			}
		}
		dovutoInDB.setDeRpSoggPagCivicoPagatore(newDovuto.getNumCiv());
	}


	public String generateIUV(Ente ente, String importo, String typeGeneration, String typePayment) {
		String iuv;

		NodoSILChiediIUV nodoSILChiediIUV = new NodoSILChiediIUV();

		nodoSILChiediIUV.setIdentificativoDominio(ente.getCodiceFiscaleEnte());
		nodoSILChiediIUV.setTipoGeneratore(typeGeneration);
		nodoSILChiediIUV.setTipoVersamento(typePayment);
		nodoSILChiediIUV.setImporto(importo);
		nodoSILChiediIUV.setAuxDigit(Constants.SMALL_IUV_AUX_DIGIT);

		NodoSILChiediIUVRisposta nodoSILChiediIUVRisposta = pagamentiTelematiciRPClient.nodoSILChiediIUV(nodoSILChiediIUV);
		if (nodoSILChiediIUVRisposta.getFault() == null || nodoSILChiediIUVRisposta.getFault().getFaultCode() == null)
			iuv = nodoSILChiediIUVRisposta.getIdentificativoUnivocoVersamento();
		else{
			log.error("error nodoSILChiediIUV, faultCode:"+nodoSILChiediIUVRisposta.getFault().getFaultCode()+
					" - description: "+nodoSILChiediIUVRisposta.getFault().getDescription());
			throw new WSFaultException(nodoSILChiediIUVRisposta.getFault().getFaultCode(), nodoSILChiediIUVRisposta.getFault().getDescription());
		}

		return iuv;
	}


	@Transactional(propagation = Propagation.REQUIRED)
	public String askRT(String username, Long mygovEnteId, Long mygovDovutoId) {
		List<EnteTipoDovuto> enteTipoDovutoOfOperator = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(mygovEnteId, username);
		List<String> listCodTipoDovuto = enteTipoDovutoOfOperator.stream().map(enteTipoDovuto -> enteTipoDovuto.getCodTipo()).collect(Collectors.toList());

		Dovuto dovuto = Optional.ofNullable(dovutoDao.getById(mygovDovutoId))
				.filter(d -> listCodTipoDovuto.contains(d.getCodTipoDovuto()))
				.orElseThrow(NotFoundException::new);

		if(!dovuto.getMygovAnagraficaStatoId().getCodStato().equals(AnagraficaStato.STATO_DOVUTO_PAGAMENTO_INIZIATO)) {
			log.info("askRT - invalid state of dovuto {}: {}", mygovDovutoId, dovuto.getMygovAnagraficaStatoId().getCodStato());
			throw new MyPayException("Invalid state of dovuto");
		}

		NodoSILRichiediRT nodoSILRichiediRT = new NodoSILRichiediRT();
		nodoSILRichiediRT.setIdentificativoDominio(dovuto.getMygovCarrelloId().getCodRpSilinviarpIdDominio());
		nodoSILRichiediRT.setIdentificativoUnivocoVersamento(dovuto.getMygovCarrelloId().getCodRpSilinviarpIdUnivocoVersamento());
		nodoSILRichiediRT.setCodiceContestoPagamento(dovuto.getMygovCarrelloId().getCodRpSilinviarpCodiceContestoPagamento());

		NodoSILRichiediRTRisposta nodoSILRichiediRTRisposta = pagamentiTelematiciRPClient.nodoSILRichiediRT(nodoSILRichiediRT);
		if (nodoSILRichiediRTRisposta.getFault() == null || StringUtils.isBlank(nodoSILRichiediRTRisposta.getFault().getFaultCode())) {
			return nodoSILRichiediRTRisposta.getEsito();
		} else {
			log.error("error askRT, fault: {}", ToStringBuilder.reflectionToString(nodoSILRichiediRTRisposta.getFault()));
			throw new WSFaultException(nodoSILRichiediRTRisposta.getFault().getFaultCode(), nodoSILRichiediRTRisposta.getFault().getDescription());
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void putInCarrello(Carrello carrello, Dovuto dovuto) {
		dovuto.setMygovCarrelloId(carrello);
		int updatedRec = dovutoDao.update(dovuto);

		if (updatedRec != 1) {
			throw new MyPayException("Errore interno aggiornamento  dovuto");
		}
		log.info("Dovuto ["+dovuto.getMygovDovutoId()+"] just put into Carrello ["+carrello.getMygovCarrelloId()+"]");
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void putInCarrello(Carrello carrello, List<CartItem> items) {
		List<Long> ids = items.stream().map(CartItem::getId).collect(Collectors.toList());
		int updatedRec = dovutoDao.putInCarrello(ids, carrello.getMygovCarrelloId());
		if (updatedRec < 1) {
			throw new MyPayException("Errore interno aggiornamento  dovuto");
		}
		log.info("Dovuto/i ["+String.join("|",ids.iterator()+"] just put into Carrello ["+carrello.getMygovCarrelloId()+"]"));
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateStatus(List<Dovuto> dovuti, String status) {
		AnagraficaStato newStatus = anagraficaStatoService.getByCodStatoAndTipoStato(status, Constants.STATO_TIPO_DOVUTO);
		List<Long> ids = dovuti.stream().map(Dovuto::getMygovDovutoId).collect(Collectors.toList());
		int updatedRec = dovutoDao.updateStatus(ids, newStatus.getMygovAnagraficaStatoId());
		if (updatedRec < 1) {
			throw new MyPayException("Errore interno aggiornamento  dovuto");
		}
		log.info("Dovuto/i ["+String.join("|",ids.iterator()+"] status ["+status+"] is up to date"));
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void restorePagabile(Long mygovDovutoId) {
		int updatedRec = dovutoDao.updateStatusAndResetCarrello(mygovDovutoId, mygovAnagraficaStatoDovutoDaPagare.getMygovAnagraficaStatoId());
		if (updatedRec != 1) {
			throw new MyPayException("Errore interno aggiornamento  dovuto");
		}
		log.debug("Dovuto ["+mygovDovutoId+"] status ["+Constants.STATO_DOVUTO_DA_PAGARE+"] is up to date");
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public Long insertDovutoFromSpontaneo(CartItem item, Ente ente, boolean generateIuv) {
		log.debug("insertDovutoFromSpontaneo start");

		AnagraficaPagatore anagraficaPagatore = item.getIntestatario();

		Dovuto dovuto = new Dovuto();
		dovuto.setMygovAnagraficaStatoId(mygovAnagraficaStatoDovutoDaPagare);
		// flg Dovuto Attuale viene usato dal batch di export per l'esportazione
		// e false il dovuto NON viene esportato
		dovuto.setFlgDovutoAttuale(true);
		dovuto.setCodIud(Optional.ofNullable(item.getIud()).orElse(Utilities.getRandomIUD()));

		Date now = new Date();
		dovuto.setDtCreazione(now);
		dovuto.setDtUltimaModifica(now);
		Flusso flusso = null;
		if(StringUtils.isNotEmpty(item.getIdentificativoUnivocoFlusso())){
			Optional<Flusso> optionalFlusso = flussoService.getByIuf(item.getIdentificativoUnivocoFlusso());
			flusso = optionalFlusso.orElseThrow(()-> new NotFoundException(
					"Error on ente ["+ente.getCodIpaEnte()+"] configuration: missing flusso ["+item.getIdentificativoUnivocoFlusso()+"]" ));
		} else {
			List<Flusso> flussi = flussoService.getByEnte(ente.getMygovEnteId(), true);
			if(flussi.isEmpty())
				throw new NotFoundException("Error on ente ["+ente.getCodIpaEnte()+"] configuration: missing flusso spontaneo");
			flusso = flussi.get(0);
		}
		dovuto.setMygovFlussoId(flusso);
		dovuto.setNumRigaFlusso(flusso.getNumRigheTotali());
		if (anagraficaPagatore != null) {
			dovuto.setCodRpSoggPagIdUnivPagTipoIdUnivoco(anagraficaPagatore.getTipoIdentificativoUnivoco());
			dovuto.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(anagraficaPagatore.getCodiceIdentificativoUnivoco());
			dovuto.setDeRpSoggPagAnagraficaPagatore(anagraficaPagatore.getAnagrafica());
			dovuto.setDeRpSoggPagIndirizzoPagatore(anagraficaPagatore.getIndirizzo());
			dovuto.setDeRpSoggPagCivicoPagatore(anagraficaPagatore.getCivico());
			dovuto.setCodRpSoggPagCapPagatore(anagraficaPagatore.getCap());
			dovuto.setDeRpSoggPagEmailPagatore(anagraficaPagatore.getEmail());
			Optional.ofNullable(anagraficaPagatore.getNazione()).ifPresent(dovuto::setCodRpSoggPagNazionePagatore);
			Optional.ofNullable(anagraficaPagatore.getProvincia()).ifPresent(dovuto::setDeRpSoggPagProvinciaPagatore);
			Optional.ofNullable(anagraficaPagatore.getLocalita()).ifPresent(dovuto::setDeRpSoggPagLocalitaPagatore);
		}
		dovuto.setCodRpDatiVersTipoVersamento(ente.getCodRpDatiVersTipoVersamento());

		BigDecimal importo = item.getImporto();
		dovuto.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(importo);
		dovuto.setNumRpDatiVersDatiSingVersCommissioneCaricoPa(ente.getNumRpDatiVersDatiSingVersCommissioneCaricoPa());
		dovuto.setDeRpDatiVersDatiSingVersCausaleVersamento(item.getCausale());
		EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getOptionalByCodTipo(item.getCodTipoDovuto(), ente.getCodIpaEnte(), true).get();
		dovuto.setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(Optional.ofNullable(item.getDatiSpecificiRiscossione())
				.orElse(enteTipoDovuto.getCodTassonomico() + enteTipoDovuto.getCodTipo()));

		Calendar dataScadenzaCalendar = Calendar.getInstance();
		dataScadenzaCalendar.add(Calendar.DAY_OF_YEAR, 30);
		/*
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date dataScadenza = formatter.format(dataScadenzaCalendar.getTime());
		*/
		Date dataScadenza = dataScadenzaCalendar.getTime();
		dovuto.setDtRpDatiVersDataEsecuzionePagamento(dataScadenza);
		dovuto.setCodTipoDovuto(item.getCodTipoDovuto());
		Utilities.setIfNotBlank(item.getCausaleVisualizzata(), dovuto::setDeCausaleVisualizzata);
		Optional.ofNullable(item.getBilancio()).filter(StringUtils::isNotBlank).ifPresent(dovuto::setBilancio);
		Optional.ofNullable(item.getBolloDigitale()).map(marcaBolloDigitaleService::insert).ifPresent(dovuto::setMygovDatiMarcaBolloDigitaleId);

		if (generateIuv) {
			String iuv = generateIUV(ente, item.getImporto().toString(), Constants.IUV_GENERATOR_17, Constants.PAY_PRESSO_PSP);
			dovuto.setCodIuv(iuv);
			Avviso avviso = avvisoService.insert(ente, iuv, anagraficaPagatore);
			dovuto.setMygovAvvisoId(avviso);
			identificativoUnivocoService.insert(flusso.getMygovFlussoId(), ente.getMygovEnteId(), "IUV", iuv);
		}
		identificativoUnivocoService.insert(flusso.getMygovFlussoId(), ente.getMygovEnteId(), "IUD", dovuto.getCodIud());
		long newId = dovutoDao.insert(dovuto);
		log.info("insert Carrello base, new Id: "+newId);
		return newId;
	}

	public List<Dovuto> getDovutiInCarrello(Long idCarrello){
		return dovutoDao.getDovutiInCarrello(idCarrello);
	}

	public List<Dovuto> searchDovutoByIuvEnte(String iuv, String codIpaEnte){
		return dovutoDao.searchDovutoByIuvEnte(iuv, codIpaEnte);
	}

	public boolean isDovutoScaduto(Dovuto dovuto, boolean flagScadenzaObbligatoria) {
		if(flagScadenzaObbligatoria){
			return dovuto.getDtRpDatiVersDataEsecuzionePagamento()!=null &&
				LocalDate.now().isAfter(Utilities.toLocalDate(dovuto.getDtRpDatiVersDataEsecuzionePagamento()));
		}
		return false;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public PaaSILImportaDovutoRisposta importDovuto(String tipoEvento, boolean flagGeneraIuv, String codIpaEnte, String password, byte[] dovuto){

		Versamento versamento = null;
		//workaround because need that iuv is final
		final String[] iuv = {Constants.EMPTY};
		MyPayException unmarshallingException = null;
		try {
			versamento = jaxbTransformService.unmarshalling(dovuto, Versamento.class,"/wsdl/pa/PagInf_Dovuti_Pagati_6_2_0.xsd");
			try{
				iuv[0] = versamento.getDatiVersamento().getIdentificativoUnivocoVersamento();
			}catch(Exception eIuv){
				log.debug("cannot retrieve IUV from versamento, ignoring the error for now", eIuv);
			}
		} catch (MyPayException e) {
			unmarshallingException = e;
		}


		PaaSILImportaDovutoRisposta paaSILImportaDovutoRisposta = new PaaSILImportaDovutoRisposta();
		Ente ente = enteService.getEnteByCodIpa(codIpaEnte);
		String identificativoDominio = Optional.ofNullable(ente).map(Ente::getCodiceFiscaleEnte).orElse(Constants.EMPTY);
		Date now = new Date();
		String body = "";
		try {
			String ctVersamento = new String(dovuto, StandardCharsets.UTF_8);
			if (StringUtils.isNotBlank(codIpaEnte))
				body = body + codIpaEnte + ": ";
			body += ctVersamento;
			giornalePaService.registraEvento(
					null,
					identificativoDominio,
					iuv[0],
					Constants.CODICE_CONTESTO_PAGAMENTO_NA,
					Constants.EMPTY,
					Constants.EMPTY,
					Constants.COMPONENTE_PA,
					Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
					tipoEvento,
					Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.toString(),
					"NodoDeiPagamentiRVE",
					this.identificativoStazioneIntermediariaPa,
					this.identificativoStazioneIntermediariaPa,
					Constants.EMPTY,
					body,
					Constants.GIORNALE_ESITO_EVENTO.OK.toString()
			);
		} catch (Exception e) {
			log.warn("importa [REQUEST] impossible to insert in the event log", e);
		}

		Consumer<String> registraGiornaleEventoError = parametriSpecificiInterfaccia -> {
			try {
				giornalePaService.registraEvento(
						null,
						identificativoDominio,
						iuv[0],
						Constants.CODICE_CONTESTO_PAGAMENTO_NA,
						Constants.EMPTY,
						Constants.EMPTY,
						Constants.COMPONENTE_PA,
						Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
						tipoEvento,
						Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
						"NodoDeiPagamentiRVE",
						this.identificativoStazioneIntermediariaPa,
						this.identificativoStazioneIntermediariaPa,
						Constants.EMPTY,
						parametriSpecificiInterfaccia,
						Constants.GIORNALE_ESITO_EVENTO.KO.toString()
				);
			} catch (Exception e) {
				log.warn("importa [RESPONSE] impossible to insert in the event log", e);
			}
		};

		if(unmarshallingException!=null){
			log.error("paaSILImportaDovuto error unmarshalling: [" + unmarshallingException.getMessage() + "]", unmarshallingException);
			registraGiornaleEventoError.accept("paaSILImportaDovuto error unmarshalling: [" + unmarshallingException.getMessage() + "]");

			StringBuffer buffer = new StringBuffer();
			buffer.append("XML ricevuto per paaSILImportaDovuto non conforme all' XSD per ente ["
				+ codIpaEnte + "]");
			buffer.append("XML Error: \n").append(unmarshallingException.getMessage());
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_XML_NON_VALIDO_CODE,
				"XML dei dovuti non valido", buffer.toString()));
			return paaSILImportaDovutoRisposta;
		}

		if (ente == null) {
			log.error("paaSILImportaDovuto: Ente non valido: " + codIpaEnte);
			FaultBean faultBean = this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_ENTE_NON_VALIDO_CODE,
					"codice IPA Ente [" + codIpaEnte + "] non valido", null);
			registraGiornaleEventoError.accept(faultBean.getDescription());
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(faultBean);
			return paaSILImportaDovutoRisposta;
		}

		boolean isStatoInserito = Utilities.checkIfStatoInserito(ente) ;
		if (isStatoInserito) {
			FaultBean faultBean = new FaultBean();
			faultBean.setId(ente.getCodIpaEnte());
			faultBean.setFaultCode(FaultCodeConstants.PAA_ENTE_NON_VALIDO_CODE);
			faultBean.setFaultString("Stato Ente Non Valido");
			faultBean.setSerial(0);
			registraGiornaleEventoError.accept(faultBean.getDescription());
			paaSILImportaDovutoRisposta.setEsito("KO");
			paaSILImportaDovutoRisposta.setFault(faultBean);
			return paaSILImportaDovutoRisposta;

		}

		Boolean passwordValidaPerEnte = enteService.verificaPassword(codIpaEnte, password);
		if (!passwordValidaPerEnte) {
			log.error("paaSILImportaDovuto: Password non valida per ente: " + codIpaEnte);
			FaultBean faultBean = this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_ENTE_NON_VALIDO_CODE,
					"Password non valida per ente [" + codIpaEnte + "]", null);
			registraGiornaleEventoError.accept(faultBean.getDescription());
			paaSILImportaDovutoRisposta.setEsito("KO");
			paaSILImportaDovutoRisposta.setFault(faultBean);
			return paaSILImportaDovutoRisposta;
		}

		String azione = versamento.getAzione();
		if (!Arrays.asList(new String[] { "I", "M", "A", "S" }).contains(azione)) {
			log.error("paaSILImportaDovuto: azione non valida: " + azione);
			registraGiornaleEventoError.accept("paaSILImportaDovuto: azione non valida: " + azione);
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_AZIONE_NON_VALIDO_CODE,
					"azione non valida [" + azione + "]", null));
			return paaSILImportaDovutoRisposta;

		}

		// CONTROLLI FORMALI COME BATCH IMPORT

		CtDatiVersamento datiVersamento = versamento.getDatiVersamento();
		String identificativoUnivocoVersamento = datiVersamento.getIdentificativoUnivocoVersamento();
		if (!Utilities.validaIUV(identificativoUnivocoVersamento, flagGeneraIuv, ente.getApplicationCode())) {
			registraGiornaleEventoError.accept("paaSILImportaDovuto: IdentificativoUnivocoVersamento non valido: " + identificativoUnivocoVersamento);
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IUV_NON_VALIDO_CODE,
					"IdentificativoUnivocoVersamento non valido [" + identificativoUnivocoVersamento
							+ "] per ente [" + codIpaEnte + "]", null));
			return paaSILImportaDovutoRisposta;
		}

		// RECUPER UTENTE
		Utente utente = utenteService.getByCodFedUserId(ente.getCodIpaEnte() + "-" + Constants.WS_USER).get();

		// DEFAULT COMMISSIONE CARICO PA
		BigDecimal commissioneCaricoPA = Optional.ofNullable(datiVersamento.getCommissioneCaricoPA()).orElse(BigDecimal.ZERO);
		if (commissioneCaricoPA != null && commissioneCaricoPA.compareTo(Constants.MAX_AMOUNT) > 0){
			log.error("paaSILImportaDovuto: commissioneCaricoPA non valido: " + commissioneCaricoPA);
			registraGiornaleEventoError.accept("paaSILImportaDovuto: commissioneCaricoPA non valido: "+commissioneCaricoPA);
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IMPORTO_SINGOLO_VERSAMENTO_NON_VALIDO_CODE,
					"commissioneCaricoPA non valido [" + commissioneCaricoPA + "]", null));
			return paaSILImportaDovutoRisposta;
		}

		// CONTROLLO IMPORTO DIVERSO DA ZERO
		BigDecimal importoSingoloVersamento = datiVersamento.getImportoSingoloVersamento();
		if (importoSingoloVersamento.compareTo(BigDecimal.ZERO) == 0) {
			log.error("paaSILImportaDovuto: ImportoSingoloVersamento non valido: " + importoSingoloVersamento);
			registraGiornaleEventoError.accept("paaSILImportaDovuto: ImportoSingoloVersamento non valido: " + importoSingoloVersamento);
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IMPORTO_SINGOLO_VERSAMENTO_NON_VALIDO_CODE,
					"ImportoSingoloVersamento non valido: " + importoSingoloVersamento + "]", null));
			return paaSILImportaDovutoRisposta;
		}

		if (((StringUtils.isNotBlank(identificativoUnivocoVersamento) &&
				(identificativoUnivocoVersamento.length() == 15 || identificativoUnivocoVersamento.length() == 17)) ||
				(flagGeneraIuv)) &&
				datiVersamento.getImportoSingoloVersamento().compareTo(Constants.MAX_AMOUNT) > 0
		) {
			log.error("paaSILImportaDovuto: ImportoSingoloVersamento non valido: " + importoSingoloVersamento);
			registraGiornaleEventoError.accept("paaSILImportaDovuto: ImportoSingoloVersamento non valido: " + importoSingoloVersamento);
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IMPORTO_SINGOLO_VERSAMENTO_NON_VALIDO_CODE,
					"ImportoSingoloVersamento non valido: " + importoSingoloVersamento + "]", null));
			return paaSILImportaDovutoRisposta;
		} else if (datiVersamento.getImportoSingoloVersamento().compareTo(Constants.MAX_AMOUNT) > 0){
			log.error("paaSILImportaDovuto: ImportoSingoloVersamento non valido: " + importoSingoloVersamento);
			registraGiornaleEventoError.accept("paaSILImportaDovuto: ImportoSingoloVersamento non valido: " + importoSingoloVersamento);
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IMPORTO_SINGOLO_VERSAMENTO_NON_VALIDO_CODE,
					"ImportoSingoloVersamento non valido: " + importoSingoloVersamento + "]", null));
			return paaSILImportaDovutoRisposta;
		}

//		MODIFICHE ACCERTAMENTO PER CAPITOLI PREDETERMINATI
		Bilancio bilancio = datiVersamento.getBilancio();
		String bilancioString = null;
		if (bilancio != null) {
			if (!Utilities.verificaImportoBilancio(bilancio, importoSingoloVersamento)) {
				log.error("paaSILImportaDovuto: Importo bilancio"
						+ " non congruente per dovuto con importo [" + importoSingoloVersamento + "]");
				registraGiornaleEventoError.accept("paaSILImportaDovuto: Importo bilancio"
						+ " non congruente per dovuto con importo [" + importoSingoloVersamento + "]");
				paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
				paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IMPORTO_BILANCIO_NON_VALIDO_CODE,
						"Importo bilancio non congruente per dovuto con importo [" + importoSingoloVersamento + "]", null));
				return paaSILImportaDovutoRisposta;
			}
			bilancioString = jaxbTransformService.marshallingNoNamespace(bilancio, Bilancio.class);
		}

		String codTipo = datiVersamento.getIdentificativoTipoDovuto();
		Optional<EnteTipoDovuto> enteTipoDovutoOptional = enteTipoDovutoService.getOptionalByCodTipo(codTipo, ente.getCodIpaEnte(), true);
		if (enteTipoDovutoOptional.isEmpty()) {
			log.error("paaSILImportaDovuto: Error tipo dovuto [" + codTipo + "] non censito per ente ["
					+ ente.getCodIpaEnte() + "]");
			registraGiornaleEventoError.accept("paaSILImportaDovuto: Error tipo dovuto [" + codTipo
					+ "] non censito per ente [" + ente.getCodIpaEnte() + "]");
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IDENTIFICATIVO_TIPO_DOVUTO_NON_VALIDO_CODE,
					"Error tipo dovuto [" + codTipo + "] non censito per ente [" + ente.getCodIpaEnte() + "]", null));
			return paaSILImportaDovutoRisposta;
		} else if (!enteTipoDovutoOptional.get().isFlgAttivo()) {
			log.error("paaSILImportaDovuto: Error tipo dovuto [" + codTipo + "] non abilitato per ente ["
					+ ente.getCodIpaEnte() + " (flgAttivo: false)]");
			registraGiornaleEventoError.accept("paaSILImportaDovuto: Error tipo dovuto [" + codTipo
					+ "] non censito per ente [" + ente.getCodIpaEnte() + "]");
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IDENTIFICATIVO_TIPO_DOVUTO_NON_ABILITATO_CODE,
					"identificativoTipoDovuto [" + codTipo + "] non abilitato", null));
			return paaSILImportaDovutoRisposta;
		} else if (enteTipoDovutoOptional.get().getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)) {
			log.error("paaSILImportaDovuto: Error tipo dovuto [" + codTipo + "] non abilitato per funzione di import");
			registraGiornaleEventoError.accept("paaSILImportaDovuto: Error tipo dovuto [" + codTipo
				+ "] non abilitato per funzione di import");
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IDENTIFICATIVO_TIPO_DOVUTO_NON_VALIDO_CODE,
				"Error tipo dovuto [" + codTipo + "] non abilitato per funzione di import", null));
			return paaSILImportaDovutoRisposta;
		}
		CtSoggettoPagatore soggettoPagatore = versamento.getSoggettoPagatore();
		if (soggettoPagatore == null) {
			String msg = "paaSILImportaDovuto: soggettoPagatore non valorizzato. ";
			log.error(msg);
			registraGiornaleEventoError.accept(msg);
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_SOGG_PAG_NON_VALORIZZATO_CODE, msg, null));
			return paaSILImportaDovutoRisposta;
		}

		String tipoVersamento = versamento.getDatiVersamento().getTipoVersamento();
		if (!Utilities.validaTipoVersamento(tipoVersamento)) {
			String msg = "paaSILImportaDovuto: Tipo versamento non valido: " + tipoVersamento;
			log.error(msg);
			registraGiornaleEventoError.accept(msg);
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_TIPO_VERSAMENTO_NON_VALIDO_CODE, msg, null));
			return paaSILImportaDovutoRisposta;
		}

		// DEFAULT TIPO VERSAMENTO
		if (StringUtils.isBlank(tipoVersamento)) {
			tipoVersamento = Constants.ALL_PAGAMENTI;
		}

		String IUD = versamento.getDatiVersamento().getIdentificativoUnivocoDovuto();
		if (!Utilities.validaIUD(IUD)) {
			String msg = "paaSILImportaDovuto: IdentificativoUnivocoDovuto non valido: " + IUD;
			log.error(msg);
			registraGiornaleEventoError.accept(msg);
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IUD_NON_VALIDO_CODE, msg, null));
			return paaSILImportaDovutoRisposta;
		}

		String iIdentificativoTipoDovuto = versamento.getDatiVersamento().getIdentificativoTipoDovuto();
		if (iIdentificativoTipoDovuto == null) {
			String msg = "paaSILImportaDovuto: iIdentificativoTipoDovuto non specificato ";
			log.error(msg);
			registraGiornaleEventoError.accept(msg);
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IDENT_TIPO_DOVUTO_NON_PRESENTE_CODE, msg, null));
			return paaSILImportaDovutoRisposta;
		}

		String causaleVersamento = versamento.getDatiVersamento().getCausaleVersamento();
		if (causaleVersamento == null) {
			String msg = "paaSILImportaDovuto: causaleVersamento non specificato ";
			log.error(msg);
			registraGiornaleEventoError.accept(msg);
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_CAUSALE_NON_PRESENTE_CODE, msg, null));
			return paaSILImportaDovutoRisposta;
		}

		String datiSpecificiRiscossione = versamento.getDatiVersamento().getDatiSpecificiRiscossione();
		if (datiSpecificiRiscossione == null || !Utilities.validaDatiSpecificiRiscossione(datiSpecificiRiscossione)) {
			String msg = "paaSILImportaDovuto: DatiSpecificiRiscossione non valido: " + datiSpecificiRiscossione;
			log.error(msg);
			registraGiornaleEventoError.accept(msg);
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_DATI_SPECIFICI_RISCOSSIONE_NON_VALIDO_CODE, msg, null));
			return paaSILImportaDovutoRisposta;
		}

		String tipoIdentificativoUnivoco = versamento.getSoggettoPagatore()
				.getIdentificativoUnivocoPagatore().getTipoIdentificativoUnivoco().toString();
		String codiceIdentificativoUnivoco = versamento.getSoggettoPagatore()
				.getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco().toUpperCase();

		//MODIFICA LOGICA PER CODICE UTILIZZO FISCALE ANONIMO
		boolean isFlagCfAnonimo = enteTipoDovutoOptional.map(EnteTipoDovuto::isFlgCfAnonimo).orElse(false);
		log.debug("flag CF " + Constants.CODICE_FISCALE_ANONIMO + "[" + isFlagCfAnonimo + "]");
		if (!Utilities.isValidCodIdUnivocoConAnonimo(isFlagCfAnonimo, tipoIdentificativoUnivoco, codiceIdentificativoUnivoco)) {
			String messageTemplate = "paaSILImportaDovuto: %s [%s]";
			String faultCode;
			String faultString = codiceIdentificativoUnivoco.equals(Constants.CODICE_FISCALE_ANONIMO)? "Funzionalita CF ANONIMO non valida" : null;
			if (tipoIdentificativoUnivoco.equals(Constants.TIPOIDENTIFICATIVOUNIVOCO_F)) {
				faultCode = FaultCodeConstants.PAA_CODICE_FISCALE_NON_VALIDO_CODE;
				faultString = StringUtils.defaultString(faultString, "Codice fiscale non valido");
			} else if (tipoIdentificativoUnivoco.equals(Constants.TIPOIDENTIFICATIVOUNIVOCO_G)) {
				faultCode = FaultCodeConstants.PAA_P_IVA_NON_VALIDO_CODE;
				faultString = StringUtils.defaultString(faultString, "P.IVA non valida");
			} else {
				throw new MyPayException(String.format(messageTemplate, "error tipoIdentificativoUnivoco non valido", tipoIdentificativoUnivoco));
			}
			String msg = String.format(messageTemplate, faultString, codiceIdentificativoUnivoco);
			log.error(msg);
			registraGiornaleEventoError.accept(msg);
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, faultCode, msg, null));
			return paaSILImportaDovutoRisposta;
		}

		// bonifica indirizzo pagatore
		String indirizzoPagatore = soggettoPagatore.getIndirizzoPagatore();
		String indirizzoPagatoreBonificato = null;
		if (StringUtils.isNotBlank(indirizzoPagatore)) {
			indirizzoPagatoreBonificato = Utilities.bonificaIndirizzoAnagrafica(indirizzoPagatore);
		}

		// bonifica civico pagatore
		String civicoPagatore = soggettoPagatore.getCivicoPagatore();
		String civicoPagatoreBonificato = null;
		if (StringUtils.isNotBlank(civicoPagatore)) {
			civicoPagatoreBonificato = Utilities.bonificaCivicoAnagrafica(civicoPagatore);
		}

		soggettoPagatore.setNazionePagatore(
				Optional.ofNullable(soggettoPagatore.getNazionePagatore()).stream()
						.flatMap(item -> locationService.getNazioni()
						.stream()
                                .map(NazioneTo::getCodiceIsoAlpha2)
						.filter(item::equalsIgnoreCase)
						.map(String::toUpperCase))
						.findAny()
						.orElse(null)
		);

		String siglaProvincia = null;
		if (Constants.CODICE_NAZIONE_ITALIA.equals(soggettoPagatore.getNazionePagatore())
				&& StringUtils.isNotBlank(soggettoPagatore.getProvinciaPagatore())) {
			siglaProvincia = locationService.getProvince()
					.stream()
                    .map(ProvinciaTo::getSigla)
					.filter(soggettoPagatore.getProvinciaPagatore()::equalsIgnoreCase)
					.map(String::toUpperCase)
					.findAny()
					.orElse(null);
		}
		soggettoPagatore.setProvinciaPagatore(siglaProvincia);

		// RECUPERO FLUSSO
		String nomeFlusso = "_" + ente.getCodIpaEnte() + "_IMPORT-DOVUTO";
		Optional<Flusso> flusso = flussoService.getByIuf(nomeFlusso);
		DovutoFunctionOut dovutoFunctionOut = null;

		try {

			switch (azione) {
				case "I": // inserimento : insert_mygov_dovuto
					AnagraficaStato anagraficaStato = anagraficaStatoService
							.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_DA_PAGARE, Constants.STATO_TIPO_DOVUTO);

					if (identificativoUnivocoVersamento == null && (flagGeneraIuv)) {
						try {
							identificativoUnivocoVersamento = this.generateIUV(ente,
									datiVersamento.getImportoSingoloVersamento().toString(),
									Constants.IUV_GENERATOR_17,
									tipoVersamento);
							iuv[0] = identificativoUnivocoVersamento;
							paaSILImportaDovutoRisposta.setIdentificativoUnivocoVersamento(identificativoUnivocoVersamento);
						} catch (Exception e) {
							String msg = "paaSILImportaDovuto: Errore import dovuto IUV ["
									+ identificativoUnivocoVersamento + "] e IUD [" + IUD + "]: "
									+ "Errore generazione iuv: " + e.getMessage();
							log.error(msg);
							registraGiornaleEventoError.accept(msg);
							paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
							paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_SYSTEM_ERROR, msg, null));
							return paaSILImportaDovutoRisposta;
						}
					}

					// SPAC#45
					if (soggettoPagatore.getProvinciaPagatore() != null) {
						soggettoPagatore.setProvinciaPagatore(soggettoPagatore.getProvinciaPagatore().toUpperCase());
					}

					dovutoFunctionOut = this.callInsertFunction(ente.getMygovEnteId(), flusso.get().getMygovFlussoId(), 0, anagraficaStato.getMygovAnagraficaStatoId(),
							null, IUD, identificativoUnivocoVersamento, now, now,
							tipoIdentificativoUnivoco, codiceIdentificativoUnivoco,
							soggettoPagatore.getAnagraficaPagatore(), indirizzoPagatoreBonificato, civicoPagatoreBonificato,
							soggettoPagatore.getCapPagatore(), soggettoPagatore.getLocalitaPagatore(),
							soggettoPagatore.getProvinciaPagatore(), soggettoPagatore.getNazionePagatore(),
							soggettoPagatore.getEMailPagatore(), datiVersamento.getDataEsecuzionePagamento() != null ? datiVersamento.getDataEsecuzionePagamento().toGregorianCalendar().getTime() : null,
							tipoVersamento, datiVersamento.getImportoSingoloVersamento().doubleValue(),
							commissioneCaricoPA.doubleValue(), codTipo, datiVersamento.getCausaleVersamento(),
							datiVersamento.getDatiSpecificiRiscossione(), utente.getMygovUtenteId(), bilancioString, Boolean.FALSE, flagGeneraIuv);

					if (dovutoFunctionOut!=null && dovutoFunctionOut.getResult()==null)
					try {
						/**
						 * Controllo se il dovuto è un avviso, perchè è solo per questi che devo generare un avviso digitale;
						 * NOTA: Un dovuto è un avviso se ha uno IUV e se lo IUV ha una lunghezza di 15 o 17 caratteri.
						 */
						if (Utilities.isAvviso(identificativoUnivocoVersamento)) {

							/* AVVISATURA DIGITALE WS */
							asynchAvvisiDigitaliService.manageAvvisoDigitale(
									ente,                          /* Struttura che invia l'avviso Digitale. */
									soggettoPagatore.getAnagraficaPagatore(),        /* Indica il nominativo o la ragione sociale del pagatore. */
									null, null,                      /* Email / cellulare soggetto */
									identificativoUnivocoVersamento,            /* IUV dell’avviso di pagamento. */
									tipoIdentificativoUnivoco,                /* Dato alfanumerico che indica la natura del pagatore (F o G). */
									codiceIdentificativoUnivoco,              /* Il codice fiscale o, in alternativa, la partita IVA del pagatore. */
									datiVersamento.getDataEsecuzionePagamento().toGregorianCalendar().getTime(),  /* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
									datiVersamento.getImportoSingoloVersamento(),      /* L'importo relativo alla somma da versare. */
									datiVersamento.getCausaleVersamento(),            /* Testo libero a disposizione dell'Ente per descrivere le motivazioni del pagamento. */
									"I"
							);
						}
					} catch (Exception e) {
						log.warn("Errore nella chiamata al service per l'avvisatira digitale.", e);
					}

					break;
				case "S":
					if ((flagGeneraIuv) || StringUtils.isBlank(identificativoUnivocoVersamento)) {
						String msg = "paaSILImportaDovuto: Errore import dovuto IUV ["
								+ identificativoUnivocoVersamento + "] e IUD [" + IUD + "]: "
								+ "Errore richiesta azione 'S' prevede presenza di 'IUV' e assenza 'flag_genera_iuv'";
						log.error(msg);
						registraGiornaleEventoError.accept(msg);
						paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
						paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_SYSTEM_ERROR, msg, null));
						return paaSILImportaDovutoRisposta;
					}
					try {
						ByteArrayOutputStream byteArrayOutputStream = jasperService.generateAvviso(
								Dovuto.builder().codTipoDovuto(codTipo).nestedEnte(ente).nestedAvviso(null)
										.numRpDatiVersDatiSingVersImportoSingoloVersamento(datiVersamento.getImportoSingoloVersamento())
										.deRpSoggPagIndirizzoPagatore(indirizzoPagatoreBonificato)
										.deRpSoggPagCivicoPagatore(civicoPagatoreBonificato)
										.codRpSoggPagCapPagatore(soggettoPagatore.getCapPagatore())
										.deRpSoggPagLocalitaPagatore(soggettoPagatore.getLocalitaPagatore())
										.deRpSoggPagProvinciaPagatore(soggettoPagatore.getProvinciaPagatore())
										.deRpSoggPagAnagraficaPagatore(soggettoPagatore.getAnagraficaPagatore())
										.codRpSoggPagIdUnivPagCodiceIdUnivoco(codiceIdentificativoUnivoco)
										.deCausaleVisualizzata(datiVersamento.getCausaleVersamento())
										.deRpDatiVersDatiSingVersCausaleVersamento("")
										.dtRpDatiVersDataEsecuzionePagamento(Utilities.toDate(datiVersamento.getDataEsecuzionePagamento()))
										.codIuv(identificativoUnivocoVersamento).build()
						);
						DataHandler avviso = new DataHandler(new ByteArrayDataSource(byteArrayOutputStream.toByteArray(), "application/octet-stream"));
						paaSILImportaDovutoRisposta.setBase64ZipAvviso(avviso);
					} catch (Exception e) {
						String msg = "paaSILImportaDovuto: Errore import dovuto IUV ["
								+ identificativoUnivocoVersamento + "] e IUD [" + IUD + "]: "
								+ "Errore generazione pdf: " + e.getMessage();
						log.error(msg);
						registraGiornaleEventoError.accept(msg);
						paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
						paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_SYSTEM_ERROR, msg, null));
						return paaSILImportaDovutoRisposta;
					}
					break;
				case "M": // modifica: modify_mygov_dovuto
					anagraficaStato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_DA_PAGARE,
							Constants.STATO_TIPO_DOVUTO);

					if (identificativoUnivocoVersamento == null && flagGeneraIuv) {
						try {
							identificativoUnivocoVersamento = this.generateIUV(ente,
									datiVersamento.getImportoSingoloVersamento().toString(),
									Constants.IUV_GENERATOR_17,
									tipoVersamento);
							iuv[0] = identificativoUnivocoVersamento;
							paaSILImportaDovutoRisposta.setIdentificativoUnivocoVersamento(identificativoUnivocoVersamento);
						} catch (Exception e) {
							String msg = "paaSILImportaDovuto: Errore import dovuto IUV ["
									+ identificativoUnivocoVersamento + "] e IUD [" + IUD + "]: "
									+ "Errore generazione iuv: " + e.getMessage();
							log.error(msg);
							registraGiornaleEventoError.accept(msg);
							paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
							paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_SYSTEM_ERROR, msg, null));
							return paaSILImportaDovutoRisposta;
						}
					}

					dovutoFunctionOut = this.callModificaFunction(ente.getMygovEnteId(), flusso.get().getMygovFlussoId(), 0, anagraficaStato.getMygovAnagraficaStatoId(),
							null, IUD, identificativoUnivocoVersamento, now, tipoIdentificativoUnivoco,
							codiceIdentificativoUnivoco, soggettoPagatore.getAnagraficaPagatore(), indirizzoPagatoreBonificato,
							civicoPagatoreBonificato, soggettoPagatore.getCapPagatore(), soggettoPagatore.getLocalitaPagatore(),
							soggettoPagatore.getProvinciaPagatore(), soggettoPagatore.getNazionePagatore(),
							soggettoPagatore.getEMailPagatore(), datiVersamento.getDataEsecuzionePagamento() != null ? datiVersamento.getDataEsecuzionePagamento().toGregorianCalendar().getTime() : null,
							tipoVersamento, datiVersamento.getImportoSingoloVersamento().doubleValue(),
							commissioneCaricoPA.doubleValue(), codTipo, datiVersamento.getCausaleVersamento(),
							datiVersamento.getDatiSpecificiRiscossione(), utente.getMygovUtenteId(), bilancioString, Boolean.FALSE, flagGeneraIuv);

					if (dovutoFunctionOut!=null && dovutoFunctionOut.getResult()==null)
					try {
						/**
						 * Controllo se il dovuto è un avviso, perchè è solo per questi che devo generare un avviso digitale;
						 * NOTA: Un dovuto è un avviso se ha uno IUV e se lo IUV ha una lunghezza di 15 o 17 caratteri.
						 */
						if (Utilities.isAvviso(identificativoUnivocoVersamento)) {

							/* AVVISATURA DIGITALE WS */
							asynchAvvisiDigitaliService.manageAvvisoDigitale(
									ente,                          /* Struttura che invia l'avviso Digitale. */
									soggettoPagatore.getAnagraficaPagatore(),        /* Indica il nominativo o la ragione sociale del pagatore. */
									null, null,                      /* Email / cellulare soggetto */
									identificativoUnivocoVersamento,            /* IUV dell’avviso di pagamento. */
									tipoIdentificativoUnivoco,                /* Dato alfanumerico che indica la natura del pagatore (F o G). */
									codiceIdentificativoUnivoco,              /* Il codice fiscale o, in alternativa, la partita IVA del pagatore. */
									datiVersamento.getDataEsecuzionePagamento().toGregorianCalendar().getTime(),  /* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
									datiVersamento.getImportoSingoloVersamento(),      /* L'importo relativo alla somma da versare. */
									datiVersamento.getCausaleVersamento(),            /* Testo libero a disposizione dell'Ente per descrivere le motivazioni del pagamento. */
									"I"
							);

						}
					} catch (Exception e) {
						log.warn("Errore nella chiamata al service per l'avvisatira digitale.", e);
					}

					break;
				case "A": // annulla: insert_mygov_dovuto_elaborato
					anagraficaStato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_ANNULLATO,
							Constants.STATO_TIPO_DOVUTO);

					dovutoFunctionOut = dovutoElaboratoService.callAnnullaFunction(ente.getMygovEnteId(), flusso.get().getMygovFlussoId(), 0, anagraficaStato.getMygovAnagraficaStatoId(),
							null, IUD, identificativoUnivocoVersamento, now, "-", tipoIdentificativoUnivoco,
							codiceIdentificativoUnivoco, soggettoPagatore.getAnagraficaPagatore(), indirizzoPagatoreBonificato,
							civicoPagatoreBonificato, soggettoPagatore.getCapPagatore(), soggettoPagatore.getLocalitaPagatore(),
							soggettoPagatore.getProvinciaPagatore(), soggettoPagatore.getNazionePagatore(),
							soggettoPagatore.getEMailPagatore(), datiVersamento.getDataEsecuzionePagamento() != null ? datiVersamento.getDataEsecuzionePagamento().toGregorianCalendar().getTime() : null,
							tipoVersamento, datiVersamento.getImportoSingoloVersamento().doubleValue(),
							commissioneCaricoPA.doubleValue(), codTipo, datiVersamento.getCausaleVersamento(),
							datiVersamento.getDatiSpecificiRiscossione(), utente.getMygovUtenteId(), true);

					break;
			}
		} catch(Exception e){
			String msg = "paaSILImportaDovuto: Errore import dovuto IUV ["
					+ identificativoUnivocoVersamento + "] e IUD [" + IUD + "]: "
					+ "Errore generazione iuv: " + e.getMessage();
			log.error(msg, e);
			registraGiornaleEventoError.accept(msg);
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_SYSTEM_ERROR, msg, null));
			return paaSILImportaDovutoRisposta;
		}

		if (dovutoFunctionOut!=null && dovutoFunctionOut.getResult()!=null){
			log.error("errore dovutoFunctionOut: {}", dovutoFunctionOut);
			String msg = "paaSILImportaDovuto: Errore import dovuto IUV ["
					+ identificativoUnivocoVersamento + "] e IUD [" + IUD + "]: "
					+ dovutoFunctionOut.getResultDesc();
			log.error(msg+ ": {}", dovutoFunctionOut);
			registraGiornaleEventoError.accept(msg);
			paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
			paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, dovutoFunctionOut.getResult(), msg, null));
			return paaSILImportaDovutoRisposta;
		}
		if (StringUtils.isNotBlank(identificativoUnivocoVersamento) &&
				(identificativoUnivocoVersamento.length() == 15 || identificativoUnivocoVersamento.length() == 17) &&
				!"S".equalsIgnoreCase(versamento.getAzione()) &&
				!"A".equalsIgnoreCase(versamento.getAzione()) &&
				!"9".equalsIgnoreCase(versamento.getAzione()) &&
				flagGeneraIuv) {
			//se ha caratteristiche di avviso (iuv) e tutto ok creo link per scaricarlo

			//recupero il dovuto corrispondente
			List<Dovuto> dovuti = this.searchDovutoByIuvEnte(identificativoUnivocoVersamento, ente.getCodIpaEnte());
			if(dovuti.size()!=1){
				throw new MyPayException("il numero di dovuti per ente ["+ente.getCodIpaEnte()+"] e iuv ["+identificativoUnivocoVersamento+"] è: "+dovuti.size());
			} else {
				String dovutoId = ""+dovuti.get(0).getMygovDovutoId();
				String securityTokenAvviso = jwtTokenUtil.generateSecurityToken(null, dovutoId, 3600*24*365*2); //expire in 2 years
				paaSILImportaDovutoRisposta.setUrlFileAvviso(landingService.getUrlDownloadAvviso(dovutoId, securityTokenAvviso));
			}

		}

		try {
			giornalePaService.registraEvento(
					null,
					identificativoDominio,
					identificativoUnivocoVersamento,
					Constants.CODICE_CONTESTO_PAGAMENTO_NA,
					Constants.EMPTY,
					tipoVersamento,
					Constants.COMPONENTE_PA,
					Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
					tipoEvento,
					Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.toString(),
					"NodoDeiPagamentiRVE",
					this.identificativoStazioneIntermediariaPa,
					this.identificativoStazioneIntermediariaPa,
					Constants.EMPTY,
					body,
					Constants.GIORNALE_ESITO_EVENTO.OK.toString()
			);
		} catch (Exception e) {
			log.warn("importa [RESPONSE] impossible to insert in the event log", e);
		}
		paaSILImportaDovutoRisposta.setEsito("OK");
		return paaSILImportaDovutoRisposta;

	}

	private FaultBean getFaultBean(String faultID, String faultCode, String faultString, String description) {

		if (faultCode.equals(FaultCodeConstants.PAA_PAGAMENTO_NON_INIZIATO_CODE) || faultCode.equals(FaultCodeConstants.PAA_PAGAMENTO_IN_CORSO_CODE)
				|| faultCode.equals(FaultCodeConstants.PAA_PAGAMENTO_ANNULLATO_CODE) || faultCode.equals(FaultCodeConstants.PAA_PAGAMENTO_SCADUTO_CODE)) {
			// niente log
		} else {
			log.error(faultCode + " " + faultString + " " + description);
		}

		FaultBean faultBean = new FaultBean();
		faultBean.setId(faultID);
		faultBean.setFaultCode(faultCode);
		faultBean.setFaultString(faultString);
		faultBean.setDescription(description);

		return faultBean;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int updateStato(List<Long> mygovCarrelloIdList, Long mygovAnagraficaStatoId){
		return dovutoDao.updateStatus(mygovCarrelloIdList, mygovAnagraficaStatoId);
	}

	public void validateAndNormalizeAnagraficaPagatore(AnagraficaPagatore.TIPO tipo, AnagraficaPagatore anagraficaPagatore, boolean allowCfAnonimo) throws ValidatorException {
		if (anagraficaPagatore == null || StringUtils.isBlank(anagraficaPagatore.getAnagrafica()))
			throw new ValidatorException(messageSource.getMessage("pa.anagrafica"+tipo+".anagraficaNonValida", null, Locale.ITALY));

		if(anagraficaPagatore.getTipoIdentificativoUnivoco()==Character.MIN_VALUE)
			anagraficaPagatore.setTipoIdentificativoUnivoco(Constants.TIPOIDENTIFICATIVOUNIVOCO_F.charAt(0));

		if(!ArrayUtils.contains(Constants.TIPOIDENTIFICATIVOUNIVOCO_VALID_VALUES, anagraficaPagatore.getTipoIdentificativoUnivoco()))
			throw new ValidatorException(messageSource.getMessage("pa.anagrafica"+tipo+".tipoIdentificativoUnivocoNonValido", null, Locale.ITALY));

		if(StringUtils.isBlank(anagraficaPagatore.getCodiceIdentificativoUnivoco()))
			anagraficaPagatore.setCodiceIdentificativoUnivoco(Constants.CODICE_FISCALE_ANONIMO);

		if (!Utilities.isValidCodIdUnivocoConAnonimo(allowCfAnonimo,
				Character.toString(anagraficaPagatore.getTipoIdentificativoUnivoco()), anagraficaPagatore.getCodiceIdentificativoUnivoco())) {
			throw new ValidatorException(messageSource.getMessage("pa.anagrafica"+tipo+".codiceIdentificativoUnivocoNonValido", null, Locale.ITALY));
		}

		if( StringUtils.isBlank(anagraficaPagatore.getEmail()) && tipo.equals(AnagraficaPagatore.TIPO.Versante) ||
				StringUtils.isNotBlank(anagraficaPagatore.getEmail()) && !Utilities.isValidEmail(anagraficaPagatore.getEmail()) )
			throw new ValidatorException(messageSource.getMessage("pa.anagrafica"+tipo+".emailNonValida", null, Locale.ITALY));

		if (anagraficaPagatore.getNazioneId() != null) {
			NazioneTo nazione = locationService.getNazione(anagraficaPagatore.getNazioneId());
			if (nazione == null)
				throw new ValidatorException(messageSource.getMessage("pa.anagrafica"+tipo+".nazioneNonValida", null, Locale.ITALY));
			anagraficaPagatore.setNazione(nazione.getCodiceIsoAlpha2());
		}

		if (anagraficaPagatore.getProvinciaId() != null) {
			ProvinciaTo provincia = locationService.getProvincia(anagraficaPagatore.getProvinciaId());
			if (provincia == null || !StringUtils.equals(anagraficaPagatore.getNazione(), "IT"))
				throw new ValidatorException(messageSource.getMessage("pa.anagrafica"+tipo+".provinciaNonValida", null, Locale.ITALY));
			anagraficaPagatore.setProvincia(provincia.getSigla());
		}

		if (anagraficaPagatore.getLocalitaId() != null) {
			ComuneTo comune = locationService.getComune(anagraficaPagatore.getLocalitaId());
			if (comune == null || StringUtils.isBlank(anagraficaPagatore.getProvincia()) || !StringUtils.equals(anagraficaPagatore.getNazione(), "IT"))
				throw new ValidatorException(messageSource.getMessage("pa.anagrafica"+tipo+".localitaNonValida", null, Locale.ITALY));
			anagraficaPagatore.setLocalita(comune.getComune());
		}
		if (StringUtils.isNotBlank(anagraficaPagatore.getCap())) {
			//if (StringUtils.isBlank(anagraficaPagatore.getNazione()))
			//	throw new ValidatorException(messageSource.getMessage("pa.anagrafica"+tipo+".nazioneNonValida", null, Locale.ITALY));
			//else
			if (!Utilities.isValidCAP(anagraficaPagatore.getCap(), anagraficaPagatore.getNazione()))
				throw new ValidatorException(messageSource.getMessage("pa.anagrafica"+tipo+".capNonValido", null, Locale.ITALY));
		}


		if (StringUtils.isNotBlank(anagraficaPagatore.getIndirizzo()) && !Utilities.validaIndirizzoAnagrafica(anagraficaPagatore.getIndirizzo(), false))
			throw new ValidatorException(messageSource.getMessage("pa.anagrafica"+tipo+".indirizzoNonValido.psp", null, Locale.ITALY));

		if (StringUtils.isNotBlank(anagraficaPagatore.getCivico()) && !Utilities.validaCivicoAnagrafica(anagraficaPagatore.getCivico(), false))
			throw new ValidatorException(messageSource.getMessage("pa.anagrafica"+tipo+".civicoNonValido.psp", null, Locale.ITALY));
	}

	public boolean hasDovutoNoScadenza(Long mygovEnteId, String codTipo) {
		return dovutoDao.hasDovutoNoScadenza(mygovEnteId, codTipo);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void addIdSession(Long mygovDovutoId, String idSession) {
		int updatedRec = dovutoDao.addIdSession(mygovDovutoId, idSession);

		if (updatedRec != 1) {
			throw new MyPayException("Errore interno aggiornamento  dovuto");
		}
		log.info("idSession [%s] has been added to Dovuto [%d]", idSession, mygovDovutoId);
	}
}


