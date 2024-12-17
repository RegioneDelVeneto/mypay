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
import it.regioneveneto.mygov.payment.mypay4.dao.DovutoMultibeneficiarioDao;
import it.regioneveneto.mygov.payment.mypay4.dto.*;
import it.regioneveneto.mygov.payment.mypay4.dto.fesp.DovutoEntePrimarioTo;
import it.regioneveneto.mygov.payment.mypay4.exception.*;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.security.JwtTokenUtil;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import it.regioneveneto.mygov.payment.mypay4.service.pagopa.GpdService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.ListWithCount;
import it.regioneveneto.mygov.payment.mypay4.util.MaxResultsHelper;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.iface.fesp.PagamentiTelematiciRP;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.regioneveneto.mygov.payment.mypay4.ws.util.SumUtilis;
import it.veneto.regione.pagamenti.ente.ElementoListaDovutiEntiSecondari;
import it.veneto.regione.pagamenti.ente.FaultBean;
import it.veneto.regione.pagamenti.ente.ListaDovutiEntiSecondari;
import it.veneto.regione.pagamenti.ente.PaaSILImportaDovutoRisposta;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.*;
import it.veneto.regione.schemas._2012.pagamenti.ente.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
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
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato.*;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class DovutoService {

    private static LocalDate thresholdDateNewIuv15;
    @Value("${pa.gpd.enabled}")
    private boolean gpdEnabled;
    @Value("${pa.gpd.preload}")
    private boolean gpdPreload;
    @Value("${pa.dataLimiteNuoviIuv15:31/12/2099}")
    private String _thresholdIuv15Param;
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
    @Autowired
    private DovutoMultibeneficiarioDao dovutoMultibenDao;
    @Autowired
    private TassonomiaService tassonomiaService;
    @Autowired(required = false)
    private GpdService gpdService;
    @Autowired
    private DatiMarcaBolloDigitaleService datiMarcaBolloDigitaleService;
    private AnagraficaStato mygovAnagraficaStatoDovutoDaPagare = null;
    private AnagraficaStato mygovAnagraficaStatoDovutoPredisposto = null;
    private AnagraficaStato mygovAnagraficaStatoDovutoAnnullato = null;

    public static boolean canCreateNowIUV15() {
        return thresholdDateNewIuv15.isAfter(LocalDate.now());
    }

    @PostConstruct
    private void initialize() {
        mygovAnagraficaStatoDovutoDaPagare = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_DA_PAGARE, Constants.STATO_TIPO_DOVUTO);
        mygovAnagraficaStatoDovutoPredisposto = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_PREDISPOSTO, Constants.STATO_TIPO_DOVUTO);
        mygovAnagraficaStatoDovutoAnnullato = anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_ANNULLATO, Constants.STATO_TIPO_DOVUTO);
        thresholdDateNewIuv15 = LocalDate.parse(_thresholdIuv15Param, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public boolean checkAppStatusDovuto() {
        int retries = 3;
        boolean found = false;
        boolean error = false;
        while (!found && retries-- > 0) {
            try {
                found = dovutoDao.getRandomCodiceFiscaleOnDovutoTable()
                        .map(cf -> this.searchLastDovuto(cf, 3))
                        .map(List::size)
                        .map(i -> i > 0)
                        .orElse(false);
                error = false;
            } catch (Exception e) {
                log.warn("checkAppStatusDovuto error [{}]", retries, e);
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

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(List<Long> ids) {
        int numMultiBenefCancellati = dovutoMultibenDao.deleteByIdsDovuti(ids);
        int numCancellati = dovutoDao.delete(ids);
        log.info("Cancellati {} dovuti (che contenevano {} multibeneficiari)", numCancellati, numMultiBenefCancellati);
    }


    public Dovuto getById(Long id) {
        return dovutoDao.getById(id);
    }

    public Dovuto getByIdForUpdate(Long id) {
        return dovutoDao.getByIdForUpdate(id);
    }

    public DovutoTo getToById(Long id) {
        return dovutoDao.getToById(id);
    }

    public long count(String codIpaEnte, String codTipoDovuto) {
        return dovutoDao.count(codIpaEnte, codTipoDovuto);
    }

    public List<Dovuto> getByFlussoId(Long flussoId) {
        return dovutoDao.getByFlussoId(flussoId);
    }

    // INIT GPD MASSIVO

    // per flussoID
    public List<Dovuto> getByFlussoIdAndGpdStatusWithIuvNotNull(Long flussoId, char gpdStatus, int limit) {
        return dovutoDao.getByFlussoIdAndGpdStatusWithIuvNotNull(flussoId, gpdStatus, limit);
    }

    public List<Dovuto> getByFlussoIdAndGpdStatusWithIuvNotNullAndIupdNull(Long flussoId, char gpdStatus, int limit) {
        return dovutoDao.getByFlussoIdAndGpdStatusWithIuvNotNullAndIupdNull(flussoId, gpdStatus, limit);
    }

    // per enteID
    public List<Dovuto> getByEnteIdAndGpdStatusWithIuvNotNull(Long enteId, char gpdStatus, int maxResults) {
        return dovutoDao.getByEnteIdAndGpdStatusWithIuvNotNull(enteId, gpdStatus, maxResults);
    }

    public List<Dovuto> getByEnteIdAndGpdStatusWithIuvNotNullAndIupdNull(Long enteId, char gpdStatus, int maxResults) {
        return dovutoDao.getByEnteIdAndGpdStatusWithIuvNotNullAndIupdNull(enteId, gpdStatus, maxResults);
    }

    public List<Dovuto> getErrDovutiByIdFlusso(Long idFlusso) {
        return dovutoDao.getErrDovutiByIdFlusso(idFlusso);
    }

    public Integer getCountDovutiWithIuvNullByFlusso(Long flussoId) {
        return dovutoDao.getCountDovutiWithIuvNullByFlusso(flussoId);
    }

    public Integer getCountDovutiToSyncByFlusso(Long flussoId) {
        return dovutoDao.getCountDovutiToSyncByFlusso(flussoId);
    }

    // END GPD MASSIVO

    public List<Dovuto> getUnpaidByEnteIdUnivocoPersona(String codRpSoggPagIdUnivPagTipoIdUnivoco, String codRpSoggPagIdUnivPagCodiceIdUnivoco, String codIpaEnte) {
        Long idStatoDovutoDaPagare = anagraficaStatoService.getIdByTipoAndCode(STATO_TIPO_DOVUTO, "INSERIMENTO_DOVUTO");
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

    public ListWithCount<DovutoTo> searchDovuto(String codIpaEnte, String idUnivocoPagatoreVersante,
                                       String codTipoDovuto, String causale,
                                       LocalDate dataFrom, LocalDate dataTo, boolean noticeOnly, boolean payableOnly) {
        Long idStatoDovutoPredisposto = anagraficaStatoService.getIdByTipoAndCode(STATO_TIPO_DOVUTO, "PREDISPOSTO");
        Long idStatoDovutoScaduto = anagraficaStatoService.getIdByTipoAndCode(STATO_TIPO_DOVUTO, "SCADUTO");
        var listStatiDovuto = List.of(idStatoDovutoPredisposto, idStatoDovutoScaduto);
        Long idStatoFlussoCaricato = anagraficaStatoService.getIdByTipoAndCode(STATO_TIPO_FLUSSO, "CARICATO");
        Long idStatoEnteEsercizio = anagraficaStatoService.getIdByTipoAndCode(STATO_ENTE, STATO_ENTE_ESERCIZIO);

        ListWithCount<DovutoTo> payload = maxResultsHelper.manageMaxResults(
                maxResults -> dovutoDao.searchDovuto(codIpaEnte, idStatoEnteEsercizio, idUnivocoPagatoreVersante,
                        codTipoDovuto, causale, dataFrom, dataTo.plusDays(1), listStatiDovuto, idStatoFlussoCaricato, noticeOnly, payableOnly, maxResults),
                () -> dovutoDao.searchDovutoCount(codIpaEnte, idStatoEnteEsercizio, idUnivocoPagatoreVersante,
                        codTipoDovuto, causale, dataFrom, dataTo.plusDays(1), listStatiDovuto, idStatoFlussoCaricato, noticeOnly, payableOnly));

        //Retrieve information from Dovuto Ente primario and Dovuto Multi beneficiario
        if (!payload.isEmpty())
            dovutoMultibenDao.getListDovutoMultibeneficiarioByIdDovuto(payload.stream().map(DovutoTo::getId).collect(Collectors.toList())).forEach(dm ->
                    payload.stream().filter(d -> dm.getIdDovuto().equals(d.getId())).forEach(d -> {
                        d.setImporto(SumUtilis.sumAmount(d.getImporto(), dm.getImportoSecondario()));
                        d.setMultibeneficiario(true);
                    }));

        return payload;
    }

    public Optional<Boolean> hasReplicaDovuto(String codIpaEnte, char tipoIdUnivocoPagatoreVersante,
                                              String idUnivocoPagatoreVersante, String causale, String codTipoDovuto) {
        if (StringUtils.isBlank(codIpaEnte) || StringUtils.isBlank(idUnivocoPagatoreVersante)
                || StringUtils.isBlank(causale) || StringUtils.isBlank(codTipoDovuto))
            return Optional.empty();
        else
            return dovutoDao.hasReplicaDovuto(codIpaEnte, tipoIdUnivocoPagatoreVersante, idUnivocoPagatoreVersante, causale, codTipoDovuto);
    }

    public List<DovutoTo> searchLastDovuto(String idUnivocoPagatoreVersante, Integer num) {
        Long idStatoDovutoPredisposto = anagraficaStatoService.getIdByTipoAndCode(STATO_TIPO_DOVUTO, "PREDISPOSTO");
        Long idStatoDovutoScaduto = anagraficaStatoService.getIdByTipoAndCode(STATO_TIPO_DOVUTO, "SCADUTO");
        var listStatiDovuto = List.of(idStatoDovutoPredisposto, idStatoDovutoScaduto);
        Long idStatoFlussoCaricato = anagraficaStatoService.getIdByTipoAndCode(STATO_TIPO_FLUSSO, "CARICATO");
        Long idStatoEnteEsercizio = anagraficaStatoService.getIdByTipoAndCode(STATO_ENTE, STATO_ENTE_ESERCIZIO);

        List<DovutoTo> result = dovutoDao.searchLastDovuto(idUnivocoPagatoreVersante, idStatoEnteEsercizio, listStatiDovuto, idStatoFlussoCaricato, num);

        //retrieve fields for dovuto multi-beneficiario
        if (!result.isEmpty())
            dovutoMultibenDao.getListDovutoMultibeneficiarioByIdDovuto(result.stream().map(DovutoTo::getId).collect(Collectors.toList()))
                    .forEach(mb -> result.stream().filter(d -> d.getId() == mb.getIdDovuto()).forEach(d -> {
                        d.setMultibeneficiario(true);
                        d.setImporto(SumUtilis.sumAmount(d.getImporto(), mb.getImportoSecondario()));
                    }));

        return result;
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

        List<String> listCodTipoDovuto = enteTipoDovutoFiltered.stream().map(EnteTipoDovuto::getCodTipo).collect(Collectors.toList());
        List<String> listCodTipoDovutoDataNonObbl = enteTipoDovutoService.getAttiviByMygovEnteIdAndFlags(mygovEnteId, null, false)
                .stream().distinct().filter(enteTipoDovutoFiltered::contains)
                .map(EnteTipoDovuto::getCodTipo).collect(Collectors.toList());
        String codStatoPagamento;
        final boolean notEnforceDataScadenza;
        final boolean flgIuvVolatile;
        if ("predisposto".equals(codStato)) {
            codStatoPagamento = AnagraficaStato.STATO_DOVUTO_PREDISPOSTO;
            if (dataFrom.isBefore(LocalDate.now()))
                dataFrom = LocalDate.now();
            notEnforceDataScadenza = true;
            flgIuvVolatile = false;
        } else if ("daPagare".equals(codStato)) {
            codStatoPagamento = AnagraficaStato.STATO_DOVUTO_DA_PAGARE;
            if (dataFrom.isBefore(LocalDate.now()))
                dataFrom = LocalDate.now();
            notEnforceDataScadenza = true;
            flgIuvVolatile = false;
        } else if ("pagamentoIniziato".equals(codStato)) {
            codStatoPagamento = AnagraficaStato.STATO_DOVUTO_PAGAMENTO_INIZIATO;
            notEnforceDataScadenza = true;
            flgIuvVolatile = true;
        } else if ("scaduto".equals(codStato)) {
            codStatoPagamento = AnagraficaStato.STATO_DOVUTO_DA_PAGARE;
            if (dataTo.isAfter(LocalDate.now().minusDays(1)))
                dataTo = LocalDate.now().minusDays(1);
            notEnforceDataScadenza = false;
            flgIuvVolatile = false;
        } else {
            codStatoPagamento = null;
            notEnforceDataScadenza = true;
            flgIuvVolatile = false;
        }
        final LocalDate dataToAdjusted = dataTo.plusDays(1);
        final LocalDate dataFromFinal = dataFrom;

        final Long idStatoDovuto = codStatoPagamento != null ? anagraficaStatoService.getIdByTipoAndCode(STATO_TIPO_DOVUTO, codStatoPagamento) : null;

        List<DovutoOperatoreTo> payload = maxResultsHelper.manageMaxResults(
                maxResults -> dovutoDao.searchDovutoForOperatore(mygovEnteId, dataFromFinal, dataToAdjusted, idStatoDovuto, flgIuvVolatile, flgIuvVolatile ? "OR" : "AND",
                        notEnforceDataScadenza, nomeFlusso, causale, codFiscale,
                        iud, iuv, listCodTipoDovuto, listCodTipoDovutoDataNonObbl, maxResults),
                () -> dovutoDao.searchDovutoForOperatoreCount(mygovEnteId, dataFromFinal, dataToAdjusted, idStatoDovuto, flgIuvVolatile, flgIuvVolatile ? "OR" : "AND",
                        notEnforceDataScadenza, nomeFlusso, causale, codFiscale,
                        iud, iuv, listCodTipoDovuto, listCodTipoDovutoDataNonObbl));

        if (!payload.isEmpty())
            dovutoMultibenDao.getListDovutoMultibeneficiarioByIdDovuto(payload.stream().map(DovutoOperatoreTo::getId).collect(Collectors.toList()))
                    .forEach(dm -> payload.stream().filter(d -> d.getId().equals(dm.getIdDovuto())).forEach(d -> {
                        d.setImporto(SumUtilis.sumAmount(d.getImporto(), dm.getImportoSecondario()));
                        d.setDovutoMultibeneficiario(dm);
                        d.setFlgMultibeneficiario(true);
                    }));

        return payload;
    }

    public DovutoOperatoreTo getDetailsForOperatore(String username, Long mygovEnteId, Long mygovDovutoId) {
        List<EnteTipoDovuto> enteTipoDovutoOfOperator = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(mygovEnteId, username);
        List<String> listCodTipoDovuto = enteTipoDovutoOfOperator.stream().map(EnteTipoDovuto::getCodTipo).collect(Collectors.toList());
        DovutoOperatoreTo payload = dovutoDao.getDovutoDetailsOperatore(listCodTipoDovuto, mygovDovutoId, "true");
        if (payload != null) {
            DovutoEntePrimarioTo entePrimario = dovutoDao.getInfoEntePrimarioByIdDovuto(mygovDovutoId);
            payload.setEntePrimarioDetail(entePrimario);
            DovutoMultibeneficiarioTo result = dovutoMultibenDao.getDovutoMultibeneficiarioByIdDovuto(mygovDovutoId);
            if (result != null)
                payload.setDovutoMultibeneficiario(result);
        }

        return payload;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public DovutoOperatoreTo updateDovuto(String username, Long mygovEnteId, Long mygovDovutoId, DovutoOperatoreTo newDovuto) {
        if (newDovuto.getTipoDovuto().getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE))
            throw new ValidatorException("Operazione non autorizzata utilizzando il tipo dovuto MARCA_BOLLO_DIGITALE");
        List<EnteTipoDovuto> enteTipoDovutoOfOperator = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(mygovEnteId, username);
        List<String> listCodTipoDovuto = enteTipoDovutoOfOperator.stream().map(EnteTipoDovuto::getCodTipo).collect(Collectors.toList());
        DovutoOperatoreTo authorizedDovuto = dovutoDao.getDovutoDetailsOperatore(listCodTipoDovuto, mygovDovutoId, "true");
        if (authorizedDovuto == null || !authorizedDovuto.getId().equals(newDovuto.getId())) {
            throw new ValidatorException("EnteTipoDovuto non e' attivo per l'operatore.");
        }

        EnteTipoDovuto enteTipoDovuto = enteTipoDovutoOfOperator.stream().filter(etd -> etd.getCodTipo()
                .equals(newDovuto.getTipoDovuto().getCodTipo())).collect(Collectors.toList()).get(0);

        Ente ente = enteService.getEnteById(mygovEnteId);
        Dovuto dovutoInDB = dovutoDao.getById(authorizedDovuto.getId());

        if (!Constants.STATO_DOVUTO_DA_PAGARE.equals(dovutoInDB.getMygovAnagraficaStatoId().getCodStato()))
            throw new ValidatorException(messageSource.getMessage("pa.debiti.nonModificabile", null, Locale.ITALY));

        //Retrieve the DovutoMultibeneficiario by id dovuto
        DovutoMultibeneficiario dm = dovutoMultibenDao.getByIdDovuto(mygovDovutoId);
        DovutoMultibeneficiario dovutoMultiBenToInsert = null;

        if (!newDovuto.isFlgMultibeneficiario() && null != dm) // if flg multibenefiario is false
            dovutoMultibenDao.delete(dm);




        /*
         * Caso in cui il dovuto è stato pagato
         */
        if (!dovutoInDB.getCodIud().equals(newDovuto.getIud())) {
            throw new ValidatorException(messageSource.getMessage("pa.gestioneDovuto.dovuto.errore.dovuto.pagato", null, Locale.ITALY));
        }

        /*
         * Verifico che non sia iniziata la transazione di pagamento del dovuto.
         */
        if (dovutoInDB.getMygovAnagraficaStatoId() == null ||
                !Constants.STATO_TIPO_DOVUTO.equals(dovutoInDB.getMygovAnagraficaStatoId().getDeTipoStato()) ||
                !Constants.STATO_DOVUTO_DA_PAGARE.equals(dovutoInDB.getMygovAnagraficaStatoId().getCodStato()) ||
                dovutoInDB.getMygovCarrelloId() != null && dovutoInDB.getMygovCarrelloId().getMygovCarrelloId() != null) {
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

        if (newDovuto.isFlgGenerateIuv()) {
            if (gpdEnabled || gpdPreload) {
                String tmpIupd = gpdService.generateRandomIupd(dovutoInDB.getNestedEnte().getCodiceFiscaleEnte());
                dovutoInDB.setGpdIupd(dovutoInDB.getGpdIupd() == null ? tmpIupd : dovutoInDB.getGpdIupd());
                if (gpdEnabled) {
                    dovutoInDB.setGpdStatus(Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA);
                }
            }

            String iuv = generateIUV(ente, importo.toString(), Constants.IUV_GENERATOR_17, Constants.ALL_PAGAMENTI);
            dovutoInDB.setCodIuv(iuv);
        }


        int updatedRec = dovutoDao.update(dovutoInDB);

        if (updatedRec != 1) {
            throw new MyPayException("Errore interno aggiornamento  dovuto");
        }

        //Update related DovutoMultibeneficiario
        if (dm != null && newDovuto.isFlgMultibeneficiario()) { // dm is not null (it's exists in db)

            //Validate DovutoMultiBeneficiario
            validateDovutoMultibeneficiario(newDovuto.getDovutoMultibeneficiario());

            dm.setDeRpEnteBenefDenominazioneBeneficiario(newDovuto.getDovutoMultibeneficiario().getDenominazioneBeneficiario());
            dm.setCodiceFiscaleEnte(newDovuto.getDovutoMultibeneficiario().getCodiceIdentificativoUnivoco());
            dm.setCodIuv(dovutoInDB.getCodIuv());

            //check if exsist importo for ente multibeneficiario
            if (newDovuto.getDovutoMultibeneficiario() != null && newDovuto.isFlgMultibeneficiario() && org.springframework.util.StringUtils.hasText(newDovuto.getDovutoMultibeneficiario().getImportoSecondario()))
                dm.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(new BigDecimal(newDovuto.getDovutoMultibeneficiario().getImportoSecondario()));

            dm.setDeRpEnteBenefCivicoBeneficiario(newDovuto.getDovutoMultibeneficiario().getCivicoBeneficiario());
            dm.setDeRpEnteBenefIndirizzoBeneficiario(newDovuto.getDovutoMultibeneficiario().getIndirizzoBeneficiario());
            dm.setDeRpEnteBenefLocalitaBeneficiario(newDovuto.getDovutoMultibeneficiario().getLocalitaBeneficiario());
            dm.setDeRpEnteBenefProvinciaBeneficiario(newDovuto.getDovutoMultibeneficiario().getProvinciaBeneficiario());
            dm.setCodRpEnteBenefNazioneBeneficiario(newDovuto.getDovutoMultibeneficiario().getNazioneBeneficiario());
            dm.setCodRpEnteBenefCapBeneficiario(newDovuto.getDovutoMultibeneficiario().getCapBeneficiario());
            dm.setCodRpDatiVersDatiSingVersIbanAccredito(newDovuto.getDovutoMultibeneficiario().getIbanAddebito());
            dm.setDeRpDatiVersDatiSingVersCausaleVersamento(newDovuto.getDovutoMultibeneficiario().getCausaleMB());
            dm.setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(newDovuto.getDovutoMultibeneficiario().getDatiSpecificiRiscossione());
            dm.setDtUltimaModifica(new Date());

            //update dovuto multibeneficiario
            int updatedDovutoMB = dovutoMultibenDao.update(dm);

            if (updatedDovutoMB != 1)
                throw new MyPayException("Errore interno aggiornamento  dovuto multibeneficiario");
        } else if (dm == null && newDovuto.isFlgMultibeneficiario()) { // else, i save for the first time

            //Validate DovutoMultiBeneficiario
            validateDovutoMultibeneficiario(newDovuto.getDovutoMultibeneficiario());

            dovutoMultiBenToInsert = new DovutoMultibeneficiario();
            dovutoMultiBenToInsert.setDeRpEnteBenefDenominazioneBeneficiario(newDovuto.getDovutoMultibeneficiario().getDenominazioneBeneficiario());
            dovutoMultiBenToInsert.setCodiceFiscaleEnte(newDovuto.getDovutoMultibeneficiario().getCodiceIdentificativoUnivoco());
            dovutoMultiBenToInsert.setCodIuv(dovutoInDB.getCodIuv());
            dovutoMultiBenToInsert.setCodIud(dovutoInDB.getCodIud());

            if (newDovuto.getDovutoMultibeneficiario() != null && newDovuto.isFlgMultibeneficiario() && org.springframework.util.StringUtils.hasText(newDovuto.getDovutoMultibeneficiario().getImportoSecondario()))
                dovutoMultiBenToInsert.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(new BigDecimal(newDovuto.getDovutoMultibeneficiario().getImportoSecondario()));

            dovutoMultiBenToInsert.setDeRpEnteBenefCivicoBeneficiario(newDovuto.getDovutoMultibeneficiario().getCivicoBeneficiario());
            dovutoMultiBenToInsert.setDeRpEnteBenefIndirizzoBeneficiario(newDovuto.getDovutoMultibeneficiario().getIndirizzoBeneficiario());
            dovutoMultiBenToInsert.setDeRpEnteBenefLocalitaBeneficiario(newDovuto.getDovutoMultibeneficiario().getLocalitaBeneficiario());
            dovutoMultiBenToInsert.setDeRpEnteBenefProvinciaBeneficiario(newDovuto.getDovutoMultibeneficiario().getProvinciaBeneficiario());
            dovutoMultiBenToInsert.setCodRpEnteBenefNazioneBeneficiario(newDovuto.getDovutoMultibeneficiario().getNazioneBeneficiario());
            dovutoMultiBenToInsert.setCodRpEnteBenefCapBeneficiario(newDovuto.getDovutoMultibeneficiario().getCapBeneficiario());
            dovutoMultiBenToInsert.setCodRpDatiVersDatiSingVersIbanAccredito(newDovuto.getDovutoMultibeneficiario().getIbanAddebito());
            dovutoMultiBenToInsert.setDeRpDatiVersDatiSingVersCausaleVersamento(newDovuto.getDovutoMultibeneficiario().getCausaleMB());
            dovutoMultiBenToInsert.setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(newDovuto.getDovutoMultibeneficiario().getDatiSpecificiRiscossione());
            dovutoMultiBenToInsert.setMygovDovutoId(dovutoInDB);
            dovutoMultiBenToInsert.setDtCreazione(new Date());
            dovutoMultiBenToInsert.setDtUltimaModifica(new Date());

            long newDovutoMultiBenId = dovutoMultibenDao.insert(dovutoMultiBenToInsert);
            log.info("insert dovuto multibeneficiario, new id: " + newDovutoMultiBenId);

        }

        if ((gpdEnabled || gpdPreload) && dovutoInDB.getGpdIupd() != null) {
           if(gpdEnabled) {
               if (newDovuto.isFlgGenerateIuv())
                   gpdService.newDebtPosition(List.of(dovutoInDB), Optional.ofNullable(dovutoMultiBenToInsert));
               else
                   gpdService.updateDebtPosition(List.of(dovutoInDB), Optional.ofNullable(dovutoMultiBenToInsert));
           }else if (gpdPreload) {
               gpdService.managePreload('M',dovutoInDB, dovutoInDB.getMygovDovutoId());
           }
        }


        return this.getDetailsForOperatore(username, mygovEnteId, newDovuto.getId());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public DovutoOperatoreTo insertDovuto(String username, Long mygovEnteId, DovutoOperatoreTo newDovuto) {
        if (newDovuto.getTipoDovuto().getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE))
            throw new ValidatorException("Operazione non autorizzata utilizzando il tipo dovuto MARCA_BOLLO_DIGITALE");

        List<EnteTipoDovuto> enteTipoDovutoOfOperator = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(mygovEnteId, username);
        EnteTipoDovuto enteTipoDovuto = enteTipoDovutoOfOperator.stream()
                .filter(etd -> etd.getCodTipo().equals(newDovuto.getTipoDovuto().getCodTipo()))
                .findFirst()
                .orElseThrow(() -> new ValidatorException("EnteTipoDovuto non e' attivo per l'operatore."));

        Dovuto dovutoToInsert = new Dovuto();
        dovutoToInsert.setMygovAnagraficaStatoId(anagraficaStatoService.getByCodStatoAndTipoStato(Constants.STATO_DOVUTO_DA_PAGARE, Constants.STATO_TIPO_DOVUTO));
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

        Flusso flusso = flussi.get(0);

        dovutoToInsert.setMygovFlussoId(flusso);
        dovutoToInsert.setNestedEnte(ente);
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

        if (newDovuto.isFlgGenerateIuv()) {
            String iuv = generateIUV(ente, importo.toString(), Constants.IUV_GENERATOR_17, Constants.ALL_PAGAMENTI);
            dovutoToInsert.setCodIuv(iuv);
            AnagraficaPagatore anagraficaPagatore = AnagraficaPagatore.builder()
                    .codiceIdentificativoUnivoco(dovutoToInsert.getCodRpSoggPagIdUnivPagCodiceIdUnivoco())
                    .tipoIdentificativoUnivoco(dovutoToInsert.getCodRpSoggPagIdUnivPagTipoIdUnivoco())
                    .anagrafica(dovutoToInsert.getDeRpSoggPagAnagraficaPagatore())
                    .email(dovutoToInsert.getDeRpSoggPagEmailPagatore())
                    .indirizzo(dovutoToInsert.getDeRpSoggPagIndirizzoPagatore())
                    .civico(dovutoToInsert.getDeRpSoggPagCivicoPagatore())
                    .cap(dovutoToInsert.getCodRpSoggPagCapPagatore())
                    .nazione(dovutoToInsert.getCodRpSoggPagNazionePagatore())
                    .provincia(dovutoToInsert.getDeRpSoggPagProvinciaPagatore())
                    .localita(dovutoToInsert.getDeRpSoggPagLocalitaPagatore())
                    .build();

            Avviso avviso = avvisoService.insert(ente, iuv, anagraficaPagatore);

            dovutoToInsert.setMygovAvvisoId(avviso);
            identificativoUnivocoService.insert(flusso.getMygovFlussoId(), ente.getMygovEnteId(), "IUV", iuv);

            if(gpdEnabled || gpdPreload) {
                String codIupd = gpdService.generateRandomIupd(dovutoToInsert.getNestedEnte().getCodiceFiscaleEnte());
                dovutoToInsert.setGpdIupd(codIupd);

                if (gpdEnabled) {
                    dovutoToInsert.setGpdStatus(Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA);
                }
            }
        }
        identificativoUnivocoService.insert(flusso.getMygovFlussoId(), ente.getMygovEnteId(), "IUD", dovutoToInsert.getCodIud());


        long newDovutoId = upsert(dovutoToInsert).getMygovDovutoId();
        log.info("insert dovuto, new id: " + newDovutoId);

        Dovuto dovuto = getById(newDovutoId);

        //check if object 'DovutoMultibeneficiarioTo' is not null,
        //after, save in db
        DovutoMultibeneficiario dovutoMultiBenToInsert = null;

        if (newDovuto.getDovutoMultibeneficiario() != null && newDovuto.isFlgMultibeneficiario()) {
            //Validate DovutoMultiBeneficiario
            validateDovutoMultibeneficiario(newDovuto.getDovutoMultibeneficiario());

            log.info("Start saving dovuto multibeneficiario");
            dovutoMultiBenToInsert = new DovutoMultibeneficiario();
            dovutoMultiBenToInsert.setDeRpEnteBenefDenominazioneBeneficiario(newDovuto.getDovutoMultibeneficiario().getDenominazioneBeneficiario());
            dovutoMultiBenToInsert.setCodiceFiscaleEnte(newDovuto.getDovutoMultibeneficiario().getCodiceIdentificativoUnivoco());
            dovutoMultiBenToInsert.setCodIuv(dovutoToInsert.getCodIuv());
            dovutoMultiBenToInsert.setCodIud(dovutoToInsert.getCodIud());

            //check if exsist importo for ente multibeneficiario
            if (newDovuto.getDovutoMultibeneficiario() != null && newDovuto.isFlgMultibeneficiario() && org.springframework.util.StringUtils.hasText(newDovuto.getDovutoMultibeneficiario().getImportoSecondario()))
                dovutoMultiBenToInsert.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(new BigDecimal(newDovuto.getDovutoMultibeneficiario().getImportoSecondario()));

            dovutoMultiBenToInsert.setDeRpEnteBenefCivicoBeneficiario(newDovuto.getDovutoMultibeneficiario().getCivicoBeneficiario());
            dovutoMultiBenToInsert.setDeRpEnteBenefIndirizzoBeneficiario(newDovuto.getDovutoMultibeneficiario().getIndirizzoBeneficiario());
            dovutoMultiBenToInsert.setDeRpEnteBenefLocalitaBeneficiario(newDovuto.getDovutoMultibeneficiario().getLocalitaBeneficiario());
            dovutoMultiBenToInsert.setDeRpEnteBenefProvinciaBeneficiario(newDovuto.getDovutoMultibeneficiario().getProvinciaBeneficiario());
            dovutoMultiBenToInsert.setCodRpEnteBenefNazioneBeneficiario(newDovuto.getDovutoMultibeneficiario().getNazioneBeneficiario());
            dovutoMultiBenToInsert.setCodRpEnteBenefCapBeneficiario(newDovuto.getDovutoMultibeneficiario().getCapBeneficiario());
            dovutoMultiBenToInsert.setCodRpDatiVersDatiSingVersIbanAccredito(newDovuto.getDovutoMultibeneficiario().getIbanAddebito());
            dovutoMultiBenToInsert.setDeRpDatiVersDatiSingVersCausaleVersamento(newDovuto.getDovutoMultibeneficiario().getCausaleMB());
            dovutoMultiBenToInsert.setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(newDovuto.getDovutoMultibeneficiario().getDatiSpecificiRiscossione());
            dovutoMultiBenToInsert.setMygovDovutoId(dovuto);
            dovutoMultiBenToInsert.setDtCreazione(now);
            dovutoMultiBenToInsert.setDtUltimaModifica(now);

            long newDovutoMultiBenId = dovutoMultibenDao.insert(dovutoMultiBenToInsert);
            log.info("insert dovuto multibeneficiario, new id: " + newDovutoMultiBenId);
        }


        // manage avviso digitale only if avviso: i.e. has IUV and length in [15 , 17])
        if (Utilities.isAvviso(dovuto.getCodIuv())) {
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

            if (gpdEnabled) {
                if (dovuto.getGpdIupd().isBlank()) {
                    String codIupd = gpdService.generateRandomIupd(dovuto.getNestedEnte().getCodiceFiscaleEnte());
                    dovuto.setGpdIupd(codIupd);
                }
                gpdService.newDebtPosition(List.of(dovuto), Optional.ofNullable(dovutoMultiBenToInsert));
                dovuto.setGpdStatus(Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA);
            }else if(gpdPreload){
                gpdService.managePreload('I',dovuto, null);
            }
        }


        return this.getDetailsForOperatore(username, mygovEnteId, newDovutoId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public int removeDovutoForCittadino(String username, Long mygovDovutoId) {
        int updatedRec = 0;
        Dovuto dovuto = Optional.ofNullable(dovutoDao.getById(mygovDovutoId)).orElseThrow(NotFoundException::new);

        if (!StringUtils.equalsIgnoreCase(username, dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco()) ||
                !Boolean.TRUE.equals(dovuto.getMygovFlussoId().getFlgSpontaneo()) ||
                !Constants.STATO_DOVUTO_DA_PAGARE.equals(dovuto.getMygovAnagraficaStatoId().getCodStato()) ||
                StringUtils.isBlank(dovuto.getCodIuv()) || !Utilities.isAvviso(dovuto.getCodIuv())) {
            log.warn("Error removing dovuto for cittadino idDovuto[{}] flgSpontaneo[{}] codRpSoggPagIdUnivPagCodiceIdUnivoco[{}] codStato[{}] codIuv[{}]",
                    mygovDovutoId, dovuto.getMygovFlussoId().getFlgSpontaneo(), dovuto.getCodRpSoggPagIdUnivPagCodiceIdUnivoco(),
                    dovuto.getMygovAnagraficaStatoId().getCodStato(), dovuto.getCodIuv());
            throw new ValidatorException(messageSource.getMessage("pa.debiti.nonAnnullabile", null, Locale.ITALY));
        }
        dovutoElaboratoService.elaborateDovuto(dovuto, Constants.STATO_DOVUTO_DA_PAGARE, Constants.STATO_DOVUTO_ANNULLATO);

        //TODO Check if the data in avvisoDegitale is present and the update is needed.
        try {
            List<EnteFunzionalita> listaFunzionalita = enteFunzionalitaService.getAllByCodIpaEnte(dovuto.getNestedEnte().getCodIpaEnte(), true);
            if (listaFunzionalita.stream().anyMatch(f -> Constants.FUNZIONALITA_AVVISATURA_DIGITALE.equals(f.getCodFunzionalita()))) {
                updatedRec = avvisoDigitaleService.changeStateToAnnullato(dovuto, dovuto.getNestedEnte());
            }
        } catch (RuntimeException e) {
            log.warn("Errore cambio di stato nell'anagrafica digitale", e);
            throw e;
        }

        if (gpdEnabled && StringUtils.isNotBlank(dovuto.getGpdIupd())) {
            gpdService.deleteDebtPosition(dovuto.getNestedEnte().getCodiceFiscaleEnte(), dovuto.getGpdIupd(), dovuto.getCodIuv());
        } else if (gpdPreload) {
            gpdService.managePreload('A',dovuto, null);
        }

        return updatedRec;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public int removeDovutoForOperatore(String username, Long mygovEnteId, Long mygovDovutoId) {
        int updatedRec = 0;
        Dovuto dovuto = dovutoDao.getById(mygovDovutoId);
        List<EnteTipoDovuto> enteTipoDovutoOfOperator = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(mygovEnteId, username);
        long count = enteTipoDovutoOfOperator.stream().filter(etd -> etd.getCodTipo().equals(dovuto.getCodTipoDovuto())).count();
        if (count == 0)
            throw new ValidatorException("EnteTipoDovuto non e' attivo per l'operatore.");
        if (!Constants.STATO_DOVUTO_DA_PAGARE.equals(dovuto.getMygovAnagraficaStatoId().getCodStato()))
            throw new ValidatorException(messageSource.getMessage("pa.debiti.nonAnnullabile", null, Locale.ITALY));
        dovutoElaboratoService.elaborateDovuto(dovuto, Constants.STATO_DOVUTO_DA_PAGARE, Constants.STATO_DOVUTO_ANNULLATO);

        //TODO Check if the data in avvisoDegitale is present and the update is needed.
        try {
            Ente ente = enteService.getEnteById(mygovEnteId);
            List<EnteFunzionalita> listaFunzionalita = enteFunzionalitaService.getAllByCodIpaEnte(ente.getCodIpaEnte(), true);
            if (listaFunzionalita.stream().anyMatch(f -> Constants.FUNZIONALITA_AVVISATURA_DIGITALE.equals(f.getCodFunzionalita()))) {
                updatedRec = avvisoDigitaleService.changeStateToAnnullato(dovuto, ente);
            }
        } catch (RuntimeException e) {
            log.warn("Errore cambio di stato nell'anagrafica digitale", e);
            throw e;
        }

        if (gpdEnabled && StringUtils.isNotBlank(dovuto.getGpdIupd())) {
            gpdService.deleteDebtPosition(dovuto.getNestedEnte().getCodiceFiscaleEnte(), dovuto.getGpdIupd(), dovuto.getCodIuv());
        }else if(gpdPreload){
            gpdService.managePreload('A',dovuto, null);
        }

        return updatedRec;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public int removeDovuto(Dovuto dovuto) {
        int deletedRec = dovutoDao.delete(dovuto);
        if (deletedRec != 1) {
            throw new MyPayException("Dovuto delete internal error");
        }
        if (!Constants.STATO_DOVUTO_DA_PAGARE.equals(dovuto.getMygovAnagraficaStatoId().getCodStato()))
            throw new ValidatorException(messageSource.getMessage("pa.debiti.nonAnnullabile", null, Locale.ITALY));
        log.info("Dovuto with id: " + dovuto.getMygovDovutoId() + " is deleted");

        if (gpdEnabled && StringUtils.isNotBlank(dovuto.getGpdIupd())) {
            gpdService.deleteDebtPosition(dovuto.getNestedEnte().getCodiceFiscaleEnte(), dovuto.getGpdIupd(), dovuto.getCodIuv());
        }else if (gpdPreload){
            gpdService.managePreload('A',dovuto, null);
        }

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
                                                Long n_mygov_utente_id, String n_bilancio, boolean insert_avv_dig, boolean n_flg_genera_iuv, String codFiscaleEnte, boolean massiveGpdHandling,
                                                String n_de_nome_ente, String n_cod_rp_dati_vers_dati_sing_vers_iban_accredito, Character n_gpd_status) {
        String codIupd = null;
        if ((gpdEnabled && n_gpd_status != null) || gpdPreload) {
            codIupd = gpdService.generateRandomIupd(codFiscaleEnte);
        }

        OutParameters out = dovutoDao.callInsertFunction(n_mygov_ente_id, n_mygov_flusso_id, n_num_riga_flusso, n_mygov_anagrafica_stato_id, n_mygov_carrello_id, n_cod_iud,
                n_cod_iuv, n_dt_creazione, n_dt_ultima_modifica, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
                n_de_rp_sogg_pag_anagrafica_pagatore, n_de_rp_sogg_pag_indirizzo_pagatore, n_de_rp_sogg_pag_civico_pagatore, n_cod_rp_sogg_pag_cap_pagatore,
                n_de_rp_sogg_pag_localita_pagatore, n_de_rp_sogg_pag_provincia_pagatore, n_cod_rp_sogg_pag_nazione_pagatore, n_de_rp_sogg_pag_email_pagatore,
                n_dt_rp_dati_vers_data_esecuzione_pagamento, n_cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
                n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, n_cod_tipo_dovuto, n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
                n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_mygov_utente_id, n_bilancio, insert_avv_dig, n_flg_genera_iuv, codIupd, n_gpd_status);

        return out == null ? null : DovutoFunctionOut.builder()
                .result(out.getString("result"))
                .resultDesc(out.getString("result_desc"))
                .build();
    }

    /**
     * La function elimina il dovuto dalla mygov_dovuto, e nel caso il dovuto sia legato ad un dovuto multibeneficiario eliminerà anche il dovuto
     * multibeneficiario mygov_dovuto_multibeneficiario
     */
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
                                                  String n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, Long n_mygov_utente_id, String n_bilancio, boolean insert_avv_dig, boolean n_flg_genera_iuv,
                                                  String n_cod_iupd, Character n_gpd_status,
                                                  String codFiscaleEnte, boolean massiveGpdHandling,
                                                  String n_de_nome_ente, String n_cod_rp_dati_vers_dati_sing_vers_iban_accredito) {


        if (((gpdEnabled && n_gpd_status != null)|| gpdPreload)  && (n_cod_iupd == null || n_cod_iupd.isEmpty())) {
            n_cod_iupd = gpdService.generateRandomIupd(codFiscaleEnte);
        }


        OutParameters out = dovutoDao.callModificaFunction(n_mygov_ente_id, n_mygov_flusso_id, n_num_riga_flusso, n_mygov_anagrafica_stato_id, n_mygov_carrello_id, n_cod_iud,
                n_cod_iuv, n_dt_creazione, n_cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco, n_cod_rp_sogg_pag_id_univ_pag_codice_id_univoco,
                n_de_rp_sogg_pag_anagrafica_pagatore, n_de_rp_sogg_pag_indirizzo_pagatore, n_de_rp_sogg_pag_civico_pagatore, n_cod_rp_sogg_pag_cap_pagatore,
                n_de_rp_sogg_pag_localita_pagatore, n_de_rp_sogg_pag_provincia_pagatore, n_cod_rp_sogg_pag_nazione_pagatore, n_de_rp_sogg_pag_email_pagatore,
                n_dt_rp_dati_vers_data_esecuzione_pagamento, n_cod_rp_dati_vers_tipo_versamento, n_num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento,
                n_num_rp_dati_vers_dati_sing_vers_commissione_carico_pa, n_cod_tipo_dovuto, n_de_rp_dati_vers_dati_sing_vers_causale_versamento,
                n_de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, n_mygov_utente_id, n_bilancio, insert_avv_dig, n_flg_genera_iuv,
                n_cod_iupd, n_gpd_status);

        return out == null ? null : DovutoFunctionOut.builder()
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

        if (StringUtils.isNotBlank(newDovuto.getCodFiscale())) {
            if (!Utilities.isValidCodIdUnivocoConAnonimo(enteTipoDovuto.isFlgCfAnonimo(), newDovuto.getTipoSoggetto(), newDovuto.getCodFiscale())) {
                throw new ValidatorException(messageSource.getMessage("pa.anagraficaPagatore.codiceIdentificativoUnivocoNonValido", null, Locale.ITALY));
            }
            dovutoInDB.setCodRpSoggPagIdUnivPagCodiceIdUnivoco(newDovuto.getCodFiscale());
        } else {
            if (enteTipoDovuto.isFlgCfAnonimo() && newDovuto.isFlgAnagraficaAnonima()) {
                dovutoInDB.setCodRpSoggPagIdUnivPagCodiceIdUnivoco("ANONIMO");
            } else {
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

        if (StringUtils.isNotBlank(newDovuto.getEmail())) {
            if (Utilities.isValidEmail(newDovuto.getEmail()))
                dovutoInDB.setDeRpSoggPagEmailPagatore(newDovuto.getEmail());
            else {
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

        /*
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

        try {
            BigDecimal importoFromDovuto = BigDecimal.ZERO;
            if (StringUtils.isNotBlank(newDovuto.getImporto()))
                importoFromDovuto = new BigDecimal(newDovuto.getImporto()).setScale(2);
            if (enteTipoDovuto.getImporto() != null) {
                if (!importoFromDovuto.equals(enteTipoDovuto.getImporto()))
                    throw new ValidatorException("invalid importo for tipo dovuto");
            } else {
                if (importoFromDovuto.compareTo(BigDecimal.ZERO) < (newDovuto.isFlgMultibeneficiario() ? 0 : 1)) {
                    throw new ValidatorException("invalid importo");
                }
            }
        } catch (ValidatorException e) {
            throw e;
        } catch (Exception e) {
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
        /*
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
        if (newDovuto.getNazione() != null && StringUtils.isNotBlank(newDovuto.getNazione().getCodiceIsoAlpha2()))
            nazione = Optional.ofNullable(locationService.getNazioneByCodIso(newDovuto.getNazione().getCodiceIsoAlpha2()))
                    .orElseThrow(() -> new ValidatorException(messageSource.getMessage("pa.anagrafica.nazioneNonValida", null, Locale.ITALY)));
        dovutoInDB.setCodRpSoggPagNazionePagatore(nazione != null ? nazione.getCodiceIsoAlpha2() : null);

        ProvinciaTo provincia = null;
        if (newDovuto.getProv() != null && StringUtils.isNotBlank(newDovuto.getProv().getSigla())) {
            if (nazione == null || !nazione.hasProvince())
                throw new ValidatorException(messageSource.getMessage("pa.anagrafica.provinciaNonValid", null, Locale.ITALY));
            provincia = Optional.ofNullable(locationService.getProvinciaBySigla(newDovuto.getProv().getSigla()))
                    .orElseThrow(() -> new ValidatorException(messageSource.getMessage("pa.anagrafica.provinciaNonValid", null, Locale.ITALY)));
        }
        dovutoInDB.setDeRpSoggPagProvinciaPagatore(provincia != null ? provincia.getSigla() : null);

        String comune = null;
        if (newDovuto.getComune() != null && StringUtils.isNotBlank(newDovuto.getComune().getComune())) {
            if (provincia == null)
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
        else {
            log.error("error nodoSILChiediIUV, faultCode:" + nodoSILChiediIUVRisposta.getFault().getFaultCode() +
                    " - description: " + nodoSILChiediIUVRisposta.getFault().getDescription());
            throw new WSFaultException(nodoSILChiediIUVRisposta.getFault().getFaultCode(), nodoSILChiediIUVRisposta.getFault().getDescription());
        }

        return iuv;
    }

    public String generateCCP(String idDominio, String iuv) {
        String ccp;

        if (iuv.length() == 25)
            return Constants.CODICE_CONTESTO_PAGAMENTO_NA;

        NodoSILChiediCCP nodoSILChiediCCP = new NodoSILChiediCCP();

        nodoSILChiediCCP.setIdDominio(idDominio);
        nodoSILChiediCCP.setIuv(iuv);

        NodoSILChiediCCPRisposta nodoSILChiediCCPRisposta = pagamentiTelematiciRPClient.nodoSILChiediCCP(nodoSILChiediCCP);
        if (nodoSILChiediCCPRisposta.getFault() == null || nodoSILChiediCCPRisposta.getFault().getFaultCode() == null)
            ccp = nodoSILChiediCCPRisposta.getCcp();
        else {
            log.error("error nodoSILChiediCCP, faultCode:" + nodoSILChiediCCPRisposta.getFault().getFaultCode() +
                    " - description: " + nodoSILChiediCCPRisposta.getFault().getDescription());
            throw new WSFaultException(nodoSILChiediCCPRisposta.getFault().getFaultCode(), nodoSILChiediCCPRisposta.getFault().getDescription());
        }

        return ccp;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public String askRT(String username, Long mygovEnteId, Long mygovDovutoId) {
        List<EnteTipoDovuto> enteTipoDovutoOfOperator = enteTipoDovutoService.getByMygovEnteIdAndOperatoreUsername(mygovEnteId, username);
        List<String> listCodTipoDovuto = enteTipoDovutoOfOperator.stream().map(EnteTipoDovuto::getCodTipo).collect(Collectors.toList());

        Dovuto dovuto = Optional.ofNullable(dovutoDao.getById(mygovDovutoId))
                .filter(d -> listCodTipoDovuto.contains(d.getCodTipoDovuto()))
                .orElseThrow(NotFoundException::new);

        if (!dovuto.getMygovAnagraficaStatoId().getCodStato().equals(AnagraficaStato.STATO_DOVUTO_PAGAMENTO_INIZIATO)) {
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
        log.info("Dovuto [" + dovuto.getMygovDovutoId() + "] just put into Carrello [" + carrello.getMygovCarrelloId() + "]");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void putInCarrello(Carrello carrello, List<CartItem> items) {
        List<Long> ids = items.stream().map(CartItem::getId).collect(Collectors.toList());
        int updatedRec = dovutoDao.putInCarrello(ids, carrello.getMygovCarrelloId());
        if (updatedRec < 1) {
            throw new MyPayException("Errore interno aggiornamento  dovuto");
        }
        log.info("Dovuto/i [" + String.join("|", ids.iterator() + "] just put into Carrello [" + carrello.getMygovCarrelloId() + "]"));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateStatus(List<Dovuto> dovuti, String status) {
        AnagraficaStato newStatus = anagraficaStatoService.getByCodStatoAndTipoStato(status, Constants.STATO_TIPO_DOVUTO);
        List<Long> ids = dovuti.stream().map(Dovuto::getMygovDovutoId).collect(Collectors.toList());
        int updatedRec = dovutoDao.updateStatus(ids, newStatus.getMygovAnagraficaStatoId());
        if (updatedRec < 1) {
            throw new MyPayException("Errore interno aggiornamento  dovuto");
        }
        log.info("Dovuto/i [" + String.join("|", ids.iterator() + "] status [" + status + "] is up to date"));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateStatusAndGpdStatusByIupdList(List<String> dovutiIupdList,
                                                   String status,
                                                   char gpdStatus) {
        AnagraficaStato newStatus = anagraficaStatoService.getByCodStatoAndTipoStato(status, Constants.STATO_TIPO_DOVUTO);
      //  List<String> gpdIupdList = dovuti.stream().map(Dovuto::getGpdIupd).collect(Collectors.toList());
        int updatedRec = dovutoDao.updateStatusAndGpdStatusByIupdList(dovutiIupdList,
                newStatus.getMygovAnagraficaStatoId(),
                gpdStatus);

        log.debug("Dovuto/i with gpd_iupd[" + String.join("|", dovutiIupdList.iterator()
                + "] status [" + status
                + "] and gpd-status [" + gpdStatus + "] "
                + " is up to date"));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateGpdStatusByIupdList(List<String> dovutiIupdList,
                                                   char gpdStatus) {
        int updatedRec = dovutoDao.updateGpdStatusByIupdList(dovutiIupdList,
                gpdStatus);

        log.debug("Dovuto/i with gpd_iupd[" + String.join("|", dovutiIupdList.iterator()
                + "] and gpd-status [" + gpdStatus + "] "
                + " is up to date"));
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public void updateGpdIupd(Long mygovDovutoId, String iupd) {
        int updatedRec = dovutoDao.updateGpdIupd(mygovDovutoId, iupd);
        if (updatedRec > 1) {
            throw new MyPayException("Errore interno aggiornamento dovuto " + mygovDovutoId);
        }
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public void updateGpdStatus(List<Long> dovutiIds, char gpdStatus) {
        int updatedRec = dovutoDao.updateGpdStatus(dovutiIds, gpdStatus);

        if (updatedRec < 1) {
            throw new MyPayException("Errore interno aggiornamento  dovuto");
        }
        log.info("Dovuto/i with ids[" + String.join("|", dovutiIds.iterator()
                + "] gpd-status [" + gpdStatus + "] is up to date"));
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public void restorePagabile(Long mygovDovutoId) {
        int updatedRec = dovutoDao.updateStatusAndResetCarrello(mygovDovutoId, mygovAnagraficaStatoDovutoDaPagare.getMygovAnagraficaStatoId());
        if (updatedRec != 1) {
            throw new MyPayException("Errore interno aggiornamento  dovuto");
        }
        log.debug("Dovuto [" + mygovDovutoId + "] status [" + Constants.STATO_DOVUTO_DA_PAGARE + "] is up to date");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateGpdFields(Long mygovDovutoId, char status) {
        int updatedRec = dovutoDao.updateGpd(mygovDovutoId, status);
        if (updatedRec != 1) {
            throw new MyPayException("Errore interno aggiornamento dovuto " + mygovDovutoId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Long insertDovutoFromSpontaneo(CartItem item, Ente ente, boolean generateIuv, DovutiEntiSecondari dovutiEntiSecondari, boolean flgIuvVolatile) {
        log.debug("insertDovutoFromSpontaneo start");

        AnagraficaPagatore anagraficaPagatore = item.getIntestatario();

        Dovuto dovuto = new Dovuto();
        dovuto.setNestedEnte(ente);
        dovuto.setMygovAnagraficaStatoId(mygovAnagraficaStatoDovutoDaPagare);
        // flg Dovuto Attuale viene usato dal batch di export per l'esportazione
        // e false il dovuto NON viene esportato
        dovuto.setFlgDovutoAttuale(true);
        dovuto.setCodIud(Optional.ofNullable(item.getIud()).orElse(Utilities.getRandomIUD()));

        Date now = new Date();
        dovuto.setDtCreazione(now);
        dovuto.setDtUltimaModifica(now);
        Flusso flusso;
        if (StringUtils.isNotEmpty(item.getIdentificativoUnivocoFlusso())) {
            Optional<Flusso> optionalFlusso = flussoService.getByIuf(item.getIdentificativoUnivocoFlusso());
            flusso = optionalFlusso.orElseThrow(() -> new NotFoundException(
                    "Error on ente [" + ente.getCodIpaEnte() + "] configuration: missing flusso [" + item.getIdentificativoUnivocoFlusso() + "]"));
        } else {
            List<Flusso> flussi = flussoService.getByEnte(ente.getMygovEnteId(), true);
            if (flussi.isEmpty())
                throw new NotFoundException("Error on ente [" + ente.getCodIpaEnte() + "] configuration: missing flusso spontaneo");
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

        // IUV_MULTI_09 - multi-beneficiary IUV management in paaSILInviaCarrelloDovuti
        if (null != dovutiEntiSecondari) {
            importo = importo.subtract(dovutiEntiSecondari.getDatiVersamentoEntiSecondari().getImportoSingoloVersamento());
        }

        dovuto.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(importo);
        dovuto.setNumRpDatiVersDatiSingVersCommissioneCaricoPa(ente.getNumRpDatiVersDatiSingVersCommissioneCaricoPa());
        dovuto.setDeRpDatiVersDatiSingVersCausaleVersamento(item.getCausale());
        EnteTipoDovuto enteTipoDovuto = enteTipoDovutoService.getOptionalByCodTipo(item.getCodTipoDovuto(), ente.getCodIpaEnte(), true).orElseThrow();
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

        if (!identificativoUnivocoService.getByEnteAndCodTipoIdAndId(ente.getMygovEnteId(), "IUD", dovuto.getCodIud()).isEmpty()) {
            log.error("IUD already existing - ente[{}] IUD[{}]", ente.getCodIpaEnte(), dovuto.getCodIud());
            throw new PaymentOrderException(messageSource.getMessage("pa.errore.pagamentoInCorso", null, Locale.ITALY));
        }

        if (generateIuv) {
            String iuv = generateIUV(ente, importo.toString(), Constants.IUV_GENERATOR_17, Constants.PAY_PRESSO_PSP);
            dovuto.setCodIuv(iuv);
            Avviso avviso = avvisoService.insert(ente, iuv, anagraficaPagatore);
            dovuto.setMygovAvvisoId(avviso);
            identificativoUnivocoService.insert(flusso.getMygovFlussoId(), ente.getMygovEnteId(), "IUV", iuv);
            if (gpdEnabled) {
                dovuto.setGpdIupd(gpdService.generateRandomIupd(ente.getCodiceFiscaleEnte()));
                dovuto.setGpdStatus(Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA);
            }
        }
        dovuto.setFlgIuvVolatile(flgIuvVolatile);
        identificativoUnivocoService.insert(flusso.getMygovFlussoId(), ente.getMygovEnteId(), "IUD", dovuto.getCodIud());
        long newId = this.upsert(dovuto).getMygovDovutoId();

        // IUV_MULTI_09 - multi-beneficiary IUV management in paaSILInviaCarrelloDovuti
        if (null != dovutiEntiSecondari) {
            Dovuto dovutoPri = getByIudEnte(dovuto.getCodIud(), ente.getCodIpaEnte());
            insertDovutoMultibenef(dovutoPri, dovutiEntiSecondari);
        }
        if (generateIuv && gpdEnabled) {
            DovutoMultibeneficiario dovutoMulti = getDovMultibenefByIdDovuto(dovuto.getMygovDovutoId());
            gpdService.newDebtPosition(List.of(dovuto), Optional.ofNullable(dovutoMulti));
        }

        log.info("insert Dvouto, new Id: " + newId);
        return newId;
    }

    public List<Dovuto> getDovutiInCarrello(Long idCarrello) {
        return dovutoDao.getDovutiInCarrello(idCarrello);
    }

    public List<Dovuto> searchDovutoByIuvEnte(String iuv, String codIpaEnte) {
        return dovutoDao.searchDovutoByIuvEnte(iuv, codIpaEnte);
    }

    public boolean isDovutoScaduto(Dovuto dovuto, boolean flagScadenzaObbligatoria) {
        if (flagScadenzaObbligatoria) {
            return dovuto.getDtRpDatiVersDataEsecuzionePagamento() != null &&
                    LocalDate.now().isAfter(Utilities.toLocalDate(dovuto.getDtRpDatiVersDataEsecuzionePagamento()));
        }
        return false;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public PaaSILImportaDovutoRisposta importDovuto(Boolean flagGeneraIuv, String codIpaEnte, String password, byte[] dovuto, ListaDovutiEntiSecondari listaDovutiEntiSecondari) {

        PaaSILImportaDovutoRisposta paaSILImportaDovutoRisposta = new PaaSILImportaDovutoRisposta();
        Ente ente = enteService.getEnteByCodIpa(codIpaEnte);
        Date now = new Date();

        Versamento versamento;
        try {
            versamento = jaxbTransformService.unmarshalling(dovuto, Versamento.class, "/wsdl/pa/PagInf_Dovuti_Pagati_6_2_0.xsd");
        } catch (MyPayException unmarshallingException) {
            log.error("paaSILImportaDovuto error unmarshalling: [" + unmarshallingException.getMessage() + "]", unmarshallingException);
            String errorMessage = "XML ricevuto per paaSILImportaDovuto non conforme all' XSD per ente [" + codIpaEnte + "]  XML Error: \n" +
                    jaxbTransformService.getDetailUnmarshalExceptionMessage(unmarshallingException, dovuto);
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_XML_NON_VALIDO_CODE,
                    "XML dei dovuti non valido", errorMessage));
            return paaSILImportaDovutoRisposta;
        }

        if (ente == null) {
            log.error("paaSILImportaDovuto: Ente non valido: " + codIpaEnte);
            FaultBean faultBean = this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_ENTE_NON_VALIDO_CODE,
                    "codice IPA Ente [" + codIpaEnte + "] non valido", null);
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(faultBean);
            return paaSILImportaDovutoRisposta;
        }

        boolean isStatoInserito = Utilities.checkIfStatoInserito(ente);
        if (isStatoInserito) {
            FaultBean faultBean = new FaultBean();
            faultBean.setId(ente.getCodIpaEnte());
            faultBean.setFaultCode(FaultCodeConstants.PAA_ENTE_NON_VALIDO_CODE);
            faultBean.setFaultString("Stato Ente Non Valido");
            faultBean.setSerial(0);
            paaSILImportaDovutoRisposta.setEsito("KO");
            paaSILImportaDovutoRisposta.setFault(faultBean);
            return paaSILImportaDovutoRisposta;

        }

        boolean passwordValidaPerEnte = enteService.verificaPassword(codIpaEnte, password);
        if (!passwordValidaPerEnte) {
            log.error("paaSILImportaDovuto: Password non valida per ente: " + codIpaEnte);
            FaultBean faultBean = this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_ENTE_NON_VALIDO_CODE,
                    "Password non valida per ente [" + codIpaEnte + "]", null);
            paaSILImportaDovutoRisposta.setEsito("KO");
            paaSILImportaDovutoRisposta.setFault(faultBean);
            return paaSILImportaDovutoRisposta;
        }

        String azione = versamento.getAzione();
        if (!Arrays.asList(new String[]{"I", "M", "A", "S"}).contains(azione)) {
            log.error("paaSILImportaDovuto: azione non valida: " + azione);
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_AZIONE_NON_VALIDO_CODE,
                    "azione non valida [" + azione + "]", null));
            return paaSILImportaDovutoRisposta;

        }

        // CONTROLLI FORMALI COME BATCH IMPORT

        CtDatiVersamento datiVersamento = versamento.getDatiVersamento();
        String identificativoUnivocoVersamento = datiVersamento.getIdentificativoUnivocoVersamento();
        if (!Utilities.validaIUV(identificativoUnivocoVersamento, flagGeneraIuv, ente.getApplicationCode(), StringUtils.equals(azione, "I"))) {
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IUV_NON_VALIDO_CODE,
                    "IdentificativoUnivocoVersamento non valido [" + identificativoUnivocoVersamento
                            + "] per ente [" + codIpaEnte + "]", null));
            return paaSILImportaDovutoRisposta;
        }

        boolean iuvPresent = identificativoUnivocoVersamento != null && !identificativoUnivocoVersamento.isEmpty();

        boolean dovutoToGpdSync = (gpdEnabled || gpdPreload) && (flagGeneraIuv || iuvPresent);


        Character gpdStatus = null;
        if (gpdEnabled && (flagGeneraIuv || iuvPresent))
            gpdStatus = Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA;


        // RECUPER UTENTE
        Utente utente = utenteService.getByCodFedUserId(ente.getCodIpaEnte() + "-" + Constants.WS_USER).orElseThrow();

        // DEFAULT COMMISSIONE CARICO PA
        BigDecimal commissioneCaricoPA = Optional.ofNullable(datiVersamento.getCommissioneCaricoPA()).orElse(BigDecimal.ZERO);
        if (commissioneCaricoPA.compareTo(Constants.MAX_AMOUNT) > 0) {
            log.error("paaSILImportaDovuto: commissioneCaricoPA non valido: " + commissioneCaricoPA);
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IMPORTO_SINGOLO_VERSAMENTO_NON_VALIDO_CODE,
                    "commissioneCaricoPA non valido [" + commissioneCaricoPA + "]", null));
            return paaSILImportaDovutoRisposta;
        }

        // CONTROLLO IMPORTO DIVERSO DA ZERO
        BigDecimal importoSingoloVersamento = datiVersamento.getImportoSingoloVersamento();
        if (importoSingoloVersamento.compareTo(BigDecimal.ZERO) == 0) {
            log.error("paaSILImportaDovuto: ImportoSingoloVersamento non valido: " + importoSingoloVersamento);
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IMPORTO_SINGOLO_VERSAMENTO_NON_VALIDO_CODE,
                    "ImportoSingoloVersamento non valido: " + importoSingoloVersamento + "]", null));
            return paaSILImportaDovutoRisposta;
        }

        if (((StringUtils.isNotBlank(identificativoUnivocoVersamento) &&
                (identificativoUnivocoVersamento.length() == 15 || identificativoUnivocoVersamento.length() == 17)) ||
                (flagGeneraIuv)) &&
                importoSingoloVersamento.compareTo(Constants.MAX_AMOUNT) > 0
        ) {
            log.error("paaSILImportaDovuto: ImportoSingoloVersamento non valido: " + importoSingoloVersamento);
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IMPORTO_SINGOLO_VERSAMENTO_NON_VALIDO_CODE,
                    "ImportoSingoloVersamento non valido: " + importoSingoloVersamento + "]", null));
            return paaSILImportaDovutoRisposta;
        } else if (importoSingoloVersamento.compareTo(Constants.MAX_AMOUNT) > 0) {
            log.error("paaSILImportaDovuto: ImportoSingoloVersamento non valido: " + importoSingoloVersamento);
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
                paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
                paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IMPORTO_BILANCIO_NON_VALIDO_CODE,
                        "Importo bilancio non congruente per dovuto con importo [" + importoSingoloVersamento + "]", null));
                return paaSILImportaDovutoRisposta;
            }
            bilancioString = jaxbTransformService.marshallingNoNamespace(bilancio, Bilancio.class);
        }

        String codTipo = datiVersamento.getIdentificativoTipoDovuto();
        if (codTipo == null) {
            String msg = "paaSILImportaDovuto: identificativoTipoDovuto non specificato ";
            log.error(msg);
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IDENT_TIPO_DOVUTO_NON_PRESENTE_CODE, msg, null));
            return paaSILImportaDovutoRisposta;
        }
        Optional<EnteTipoDovuto> enteTipoDovutoOptional = enteTipoDovutoService.getOptionalByCodTipo(codTipo, ente.getCodIpaEnte(), true);
        if (enteTipoDovutoOptional.isEmpty()) {
            log.error("paaSILImportaDovuto: Error tipo dovuto [" + codTipo + "] non censito per ente ["
                    + ente.getCodIpaEnte() + "]");
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IDENTIFICATIVO_TIPO_DOVUTO_NON_VALIDO_CODE,
                    "Error tipo dovuto [" + codTipo + "] non censito per ente [" + ente.getCodIpaEnte() + "]", null));
            return paaSILImportaDovutoRisposta;
        } else if (!enteTipoDovutoOptional.get().isFlgAttivo()) {
            log.error("paaSILImportaDovuto: Error tipo dovuto [" + codTipo + "] non abilitato per ente ["
                    + ente.getCodIpaEnte() + " (flgAttivo: false)]");
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IDENTIFICATIVO_TIPO_DOVUTO_NON_ABILITATO_CODE,
                    "identificativoTipoDovuto [" + codTipo + "] non abilitato", null));
            return paaSILImportaDovutoRisposta;
        } else if (enteTipoDovutoOptional.get().getCodTipo().equals(Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE)) {
            log.error("paaSILImportaDovuto: Error tipo dovuto [" + codTipo + "] non abilitato per funzione di import");
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IDENTIFICATIVO_TIPO_DOVUTO_NON_VALIDO_CODE,
                    "Error tipo dovuto [" + codTipo + "] non abilitato per funzione di import", null));
            return paaSILImportaDovutoRisposta;
        }
        CtSoggettoPagatore soggettoPagatore = versamento.getSoggettoPagatore();
        if (soggettoPagatore == null) {
            String msg = "paaSILImportaDovuto: soggettoPagatore non valorizzato. ";
            log.error(msg);
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_SOGG_PAG_NON_VALORIZZATO_CODE, msg, null));
            return paaSILImportaDovutoRisposta;
        }

        String tipoVersamento = datiVersamento.getTipoVersamento();
        if (!Utilities.validaTipoVersamento(tipoVersamento)) {
            String msg = "paaSILImportaDovuto: Tipo versamento non valido: " + tipoVersamento;
            log.error(msg);
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_TIPO_VERSAMENTO_NON_VALIDO_CODE, msg, null));
            return paaSILImportaDovutoRisposta;
        }

        // DEFAULT TIPO VERSAMENTO
        if (StringUtils.isBlank(tipoVersamento)) {
            tipoVersamento = Constants.ALL_PAGAMENTI;
        }

        String identificativoUnivocoDovuto = datiVersamento.getIdentificativoUnivocoDovuto();
        if (!Utilities.validaIUD(identificativoUnivocoDovuto)) {
            String msg = "paaSILImportaDovuto: IdentificativoUnivocoDovuto non valido: " + identificativoUnivocoDovuto;
            log.error(msg);
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_IUD_NON_VALIDO_CODE, msg, null));
            return paaSILImportaDovutoRisposta;
        }

        String causaleVersamento = datiVersamento.getCausaleVersamento();
        if (StringUtils.isBlank(causaleVersamento)) {
            String msg = "paaSILImportaDovuto: causaleVersamento non specificato ";
            log.error(msg);
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_CAUSALE_NON_PRESENTE_CODE, msg, null));
            return paaSILImportaDovutoRisposta;
        }

        String datiSpecificiRiscossione = datiVersamento.getDatiSpecificiRiscossione();
        if (datiSpecificiRiscossione == null || !Utilities.validaDatiSpecificiRiscossione(datiSpecificiRiscossione)) {
            String msg = "paaSILImportaDovuto: DatiSpecificiRiscossione non valido: " + datiSpecificiRiscossione;
            log.error(msg);
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
            String faultString = codiceIdentificativoUnivoco.equals(Constants.CODICE_FISCALE_ANONIMO) ? "Funzionalita CF ANONIMO non valida" : null;
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
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, faultCode, msg, null));
            return paaSILImportaDovutoRisposta;
        }

        // bonifica indirizzo pagatore
        String indirizzoPagatore = Optional.ofNullable(soggettoPagatore.getIndirizzoPagatore())
                .filter(StringUtils::isNotBlank)
                .map(Utilities::bonificaIndirizzoAnagrafica)
                .orElse(null);

        // bonifica civico pagatore
        String civicoPagatore = Optional.ofNullable(soggettoPagatore.getCivicoPagatore())
                .filter(StringUtils::isNotBlank)
                .map(Utilities::bonificaCivicoAnagrafica)
                .orElse(null);

        String nazione = Optional.ofNullable(soggettoPagatore.getNazionePagatore()).stream()
                .flatMap(item -> locationService.getNazioni()
                        .stream()
                        .map(NazioneTo::getCodiceIsoAlpha2)
                        .filter(item::equalsIgnoreCase)
                        .map(String::toUpperCase))
                .findAny()
                .orElse(null);

        String siglaProvincia = null;
        if (Constants.CODICE_NAZIONE_ITALIA.equals(nazione)
                && StringUtils.isNotBlank(soggettoPagatore.getProvinciaPagatore())) {
            siglaProvincia = locationService.getProvince()
                    .stream()
                    .map(ProvinciaTo::getSigla)
                    .filter(soggettoPagatore.getProvinciaPagatore()::equalsIgnoreCase)
                    .map(String::toUpperCase)
                    .findAny()
                    .orElse(null);
        }

        String cap = Optional.ofNullable(soggettoPagatore.getCapPagatore())
                .filter(s -> Utilities.isValidCAP(s, nazione))
                .orElse(null);

        Date dataEsecuzionePagamento = Optional.ofNullable(datiVersamento.getDataEsecuzionePagamento())
                .map(XMLGregorianCalendar::toGregorianCalendar)
                .map(Calendar::getTime)
                .orElse(null);

        // RECUPERO FLUSSO
        String nomeFlusso = "_" + ente.getCodIpaEnte() + "_IMPORT-DOVUTO";
        Optional<Flusso> flusso = flussoService.getByIuf(nomeFlusso);
        DovutoFunctionOut dovutoFunctionOut = null;
        DovutoMultibeneficiario dovMulti = null;
        List<byte[]> dovutiSecondariByteArrayList;
        DovutiEntiSecondari dovutiEntiSecondari = null;

        try {

            switch (azione) {
                case "I": // inserimento : insert_mygov_dovuto
                    if (identificativoUnivocoVersamento == null && (flagGeneraIuv)) {
                        try {
                            identificativoUnivocoVersamento = this.generateIUV(ente,
                                    importoSingoloVersamento.toString(),
                                    Constants.IUV_GENERATOR_17,
                                    tipoVersamento);
                            paaSILImportaDovutoRisposta.setIdentificativoUnivocoVersamento(identificativoUnivocoVersamento);
                        } catch (Exception e) {
                            String msg = "paaSILImportaDovuto: Errore import dovuto IUV ["
                                    + identificativoUnivocoVersamento + "] e IUD [" + identificativoUnivocoDovuto + "]: "
                                    + "Errore generazione iuv: " + e.getMessage();
                            log.error(msg);
                            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
                            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_SYSTEM_ERROR, msg, null));
                            return paaSILImportaDovutoRisposta;
                        }
                    }

                    /*
                     * Multi-beneficiary IUV management if defined (IUV_MULTI_10)
                     * Controllo se il dovuto è un dovuto multibeneficiario, perchè è solo per questi che devo inserire le informazioni
                     * nella tabella mygov_dovuto_multibeneficiario;
                     * NOTA: Un dovuto è multibeneficiario se listaDovutiEntiSecondari.getElementoListaDovutiEntiSecondaris() è diverso da null e size 1.
                     */

                    dovutiSecondariByteArrayList = Optional.ofNullable(listaDovutiEntiSecondari)
                      .map(ListaDovutiEntiSecondari::getElementoListaDovutiEntiSecondaris)
                      .orElse(Collections.emptyList())
                      .stream()
                      .map(ElementoListaDovutiEntiSecondari::getDovutiEntiSecondari)
                      .filter(Objects::nonNull)
                      .filter(b -> b.length > 0)
                      .collect(Collectors.toList());

                    if(dovutiSecondariByteArrayList.size() == 1) {
                        try {
                            dovutiEntiSecondari = jaxbTransformService.unmarshalling(dovutiSecondariByteArrayList.get(0), DovutiEntiSecondari.class, "/wsdl/pa/PagInf_Dovuti_Pagati_6_2_0.xsd");
                        } catch (MyPayException unmarshallingException) {
                            log.error("paaSILImportaDovuto error unmarshalling: [" + unmarshallingException.getMessage() + "]", unmarshallingException);
                            String errorMessage = "XML ricevuto per paaSILImportaDovuto non conforme all' XSD per ente [" + codIpaEnte + "]  XML Error: \n" +
                              jaxbTransformService.getDetailUnmarshalExceptionMessage(unmarshallingException, dovutiSecondariByteArrayList.get(0));
                            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
                            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_XML_NON_VALIDO_CODE, "XML dei dovuti Enti secondari non valido", errorMessage));
                            return paaSILImportaDovutoRisposta;
                        }

                        String datiSpecificiRiscossioneEnteSecondario = dovutiEntiSecondari.getDatiVersamentoEntiSecondari().getDatiSpecificiRiscossione();
                        if (StringUtils.isNotBlank(datiSpecificiRiscossioneEnteSecondario)) {
                            String codiceTassonomicoEnteSec = StringUtils.substringBeforeLast(datiSpecificiRiscossioneEnteSecondario, "/") + "/";
                            if (!tassonomiaService.ifExitsCodiceTassonomico(codiceTassonomicoEnteSec)) {
                                String errore = "paaSILImportaDovuto codice tassonomico dati specifici riscossione Ente Secondario non valido: [" + datiSpecificiRiscossioneEnteSecondario + "]";
                                log.error(errore);
                                paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
                                paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_DATI_SPECIFICI_RISCOSSIONE_NON_VALIDO_CODE, errore, errore));
                                return paaSILImportaDovutoRisposta;
                            }
                        }
                    } else if (dovutiSecondariByteArrayList.size() > 1 ) {
                        log.error("paaSILImportaDovuto error recupero Enti secondari: [" + FaultCodeConstants.PAA_LIMITAZIONE_ENTI_SECONDARI_ERROR + "]");
                        String buffer = "XML ricevuto per paaSILImportaDovuto non conforme all' XSD per enti secondari [" + FaultCodeConstants.PAA_LIMITAZIONE_ENTI_SECONDARI_ERROR + "]";
                        paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
                        paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_LIMITAZIONE_ENTI_SECONDARI_ERROR, "XML dei dovuti Enti secondari supera il limite consentito da PagoPA.", buffer));
                        return paaSILImportaDovutoRisposta;
                    }

                    dovutoFunctionOut = this.callInsertFunction(ente.getMygovEnteId(), flusso.orElseThrow().getMygovFlussoId(), 0, mygovAnagraficaStatoDovutoDaPagare.getMygovAnagraficaStatoId(),
                            null, identificativoUnivocoDovuto, identificativoUnivocoVersamento, now, now,
                            tipoIdentificativoUnivoco, codiceIdentificativoUnivoco,
                            soggettoPagatore.getAnagraficaPagatore(), indirizzoPagatore, civicoPagatore,
                            cap, soggettoPagatore.getLocalitaPagatore(),
                            siglaProvincia, nazione,
                            soggettoPagatore.getEMailPagatore(), dataEsecuzionePagamento,
                            tipoVersamento, importoSingoloVersamento.doubleValue(),
                            commissioneCaricoPA.doubleValue(), codTipo, causaleVersamento,
                            datiSpecificiRiscossione, utente.getMygovUtenteId(), bilancioString, Boolean.FALSE, flagGeneraIuv, ente.getCodiceFiscaleEnte(), false,
                            ente.getDeNomeEnte(), ente.getCodRpDatiVersDatiSingVersIbanAccredito(), gpdStatus);

                    if (dovutoFunctionOut != null && dovutoFunctionOut.getResult() == null) {
                        try {
                            /*
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
                                        dataEsecuzionePagamento,  /* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
                                        datiVersamento.getImportoSingoloVersamento(),      /* L'importo relativo alla somma da versare. */
                                        causaleVersamento,            /* Testo libero a disposizione dell'Ente per descrivere le motivazioni del pagamento. */
                                        "I"
                                );
                            }
                        } catch (Exception e) {
                            log.warn("Errore nella chiamata al service per l'avvisatura digitale.", e);
                        }

                        Dovuto dovutoPri = getByIudEnte(identificativoUnivocoDovuto, ente.getCodIpaEnte());

                        if(dovutiEntiSecondari != null) {
                            dovMulti = insertDovutoMultibenef(dovutoPri, dovutiEntiSecondari);
                        }

                        //sync to GPD
                        if (dovutoToGpdSync) {
                            if (gpdEnabled) {
                                gpdService.newDebtPosition(ente.getCodiceFiscaleEnte(), dovutoPri.getGpdIupd(), identificativoUnivocoVersamento, importoSingoloVersamento.doubleValue(),
                                  dovutoPri.getCodRpSoggPagIdUnivPagCodiceIdUnivoco(),tipoIdentificativoUnivoco.charAt(0), soggettoPagatore.getAnagraficaPagatore(), indirizzoPagatore, civicoPagatore,
                                  cap, soggettoPagatore.getLocalitaPagatore(), siglaProvincia, nazione, soggettoPagatore.getEMailPagatore(),
                                  ente.getCodIpaEnte(), ente.getDeNomeEnte(), now, causaleVersamento, dataEsecuzionePagamento, ente.getCodRpDatiVersDatiSingVersIbanAccredito(), codTipo,
                                  dovutoPri.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(), Optional.ofNullable(dovMulti));
                            } else if (gpdPreload) {
                                gpdService.managePreload('I', dovutoPri, null);

                            }
                        }

                    }

                    break;
                case "S":
                    if ((flagGeneraIuv) || StringUtils.isBlank(identificativoUnivocoVersamento)) {
                        String msg = "paaSILImportaDovuto: Errore import dovuto IUV ["
                                + identificativoUnivocoVersamento + "] e IUD [" + identificativoUnivocoDovuto + "]: "
                                + "Errore richiesta azione 'S' prevede presenza di 'IUV' e assenza 'flag_genera_iuv'";
                        log.error(msg);
                        paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
                        paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_SYSTEM_ERROR, msg, null));
                        return paaSILImportaDovutoRisposta;
                    }
                    List<Dovuto> dovutiAvviso = this.getByIuvEnte(identificativoUnivocoVersamento, codIpaEnte);
                    if (dovutiAvviso.isEmpty()) {
                        String error = String.format("Error nessun dovuto trovato per IUV [%s] ed ente [%s]", identificativoUnivocoVersamento, codIpaEnte);
                        log.error(error);
                        paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
                        paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.CODE_PAA_IUV_NON_VALIDO, error, null));
                        return paaSILImportaDovutoRisposta;
                    }
                    try {
                        ByteArrayOutputStream avvisoOutputStream = jasperService.generateAvviso(
                                Dovuto.builder().codTipoDovuto(codTipo).nestedEnte(ente).nestedAvviso(null)
                                        .numRpDatiVersDatiSingVersImportoSingoloVersamento(datiVersamento.getImportoSingoloVersamento())
                                        .deRpSoggPagIndirizzoPagatore(indirizzoPagatore)
                                        .deRpSoggPagCivicoPagatore(civicoPagatore)
                                        .codRpSoggPagCapPagatore(cap)
                                        .deRpSoggPagLocalitaPagatore(soggettoPagatore.getLocalitaPagatore())
                                        .deRpSoggPagProvinciaPagatore(siglaProvincia)
                                        .deRpSoggPagAnagraficaPagatore(soggettoPagatore.getAnagraficaPagatore())
                                        .codRpSoggPagIdUnivPagCodiceIdUnivoco(codiceIdentificativoUnivoco)
                                        .deCausaleVisualizzata(causaleVersamento)
                                        .deRpDatiVersDatiSingVersCausaleVersamento("")
                                        .dtRpDatiVersDataEsecuzionePagamento(dataEsecuzionePagamento)
                                        .codIuv(identificativoUnivocoVersamento).build()
                        );
                        ByteArrayOutputStream zipByteArrayOutputStream = new ByteArrayOutputStream();
                        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipByteArrayOutputStream)) {
                            String name = "avviso_" + ente.getCodiceFiscaleEnte() + "_" + identificativoUnivocoVersamento + ".pdf";
                            zos.putArchiveEntry(new ZipArchiveEntry(name));
                            zos.write(avvisoOutputStream.toByteArray());
                            zos.closeArchiveEntry();
                            zos.finish();
                        }
                        DataHandler avviso = new DataHandler(new ByteArrayDataSource(zipByteArrayOutputStream.toByteArray(), "application/octet-stream"));
                        paaSILImportaDovutoRisposta.setBase64ZipAvviso(avviso);
                    } catch (Exception e) {
                        String msg = "paaSILImportaDovuto: Errore import dovuto IUV ["
                                + identificativoUnivocoVersamento + "] e IUD [" + identificativoUnivocoDovuto + "]: "
                                + "Errore generazione pdf: " + e.getMessage();
                        log.error(msg);
                        paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
                        paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_SYSTEM_ERROR, msg, null));
                        return paaSILImportaDovutoRisposta;
                    }
                    break;
                case "M": // modifica: modify_mygov_dovuto
                    if (identificativoUnivocoVersamento == null && flagGeneraIuv) {
                        try {
                            identificativoUnivocoVersamento = this.generateIUV(ente,
                                    datiVersamento.getImportoSingoloVersamento().toString(),
                                    Constants.IUV_GENERATOR_17,
                                    tipoVersamento);
                            paaSILImportaDovutoRisposta.setIdentificativoUnivocoVersamento(identificativoUnivocoVersamento);
                        } catch (Exception e) {
                            String msg = "paaSILImportaDovuto: Errore import dovuto IUV ["
                                    + identificativoUnivocoVersamento + "] e IUD [" + identificativoUnivocoDovuto + "]: "
                                    + "Errore generazione iuv: " + e.getMessage();
                            log.error(msg);
                            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
                            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_SYSTEM_ERROR, msg, null));
                            return paaSILImportaDovutoRisposta;
                        }
                    }

                    Dovuto dovutoCorrente = getByIudEnte(identificativoUnivocoDovuto, ente.getCodIpaEnte());
                    if (dovutoCorrente == null) {
                        throw new MyPayException("Dovuto non trovato per IUD [" + identificativoUnivocoDovuto + "] e codice IPA Ente [" + ente.getCodIpaEnte() + "]");
                    }
                    if (!Constants.STATO_DOVUTO_DA_PAGARE.equals(dovutoCorrente.getMygovAnagraficaStatoId().getCodStato()))
                        throw new ValidatorException(messageSource.getMessage("pa.debiti.nonModificabile", null, Locale.ITALY));
                    String gpdIupdOriginal = dovutoCorrente.getGpdIupd();
                    if (gpdEnabled) {
                        gpdStatus = Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_OK_CON_PAGOPA;
                    }

                    /*
                     * Multi-beneficiary IUV management if defined (IUV_MULTI_10)
                     * Controllo se il dovuto è un dovuto multibeneficiario, perchè è solo per questi che devo modificare le informazioni
                     * nella tabella mygov_dovuto_multibeneficiario;
                     * NOTA: Un dovuto è multibeneficiario se listaDovutiEntiSecondari.getElementoListaDovutiEntiSecondaris() è diverso da null e size 1.
                     */

                    dovutiSecondariByteArrayList = Optional.ofNullable(listaDovutiEntiSecondari)
                      .map(ListaDovutiEntiSecondari::getElementoListaDovutiEntiSecondaris)
                      .orElse(Collections.emptyList())
                      .stream()
                      .map(ElementoListaDovutiEntiSecondari::getDovutiEntiSecondari)
                      .filter(Objects::nonNull)
                      .filter(b -> b.length > 0)
                      .collect(Collectors.toList());

                    if(dovutiSecondariByteArrayList.size() == 1){
                        try {
                            dovutiEntiSecondari = jaxbTransformService.unmarshalling(dovutiSecondariByteArrayList.get(0), DovutiEntiSecondari.class, "/wsdl/pa/PagInf_Dovuti_Pagati_6_2_0.xsd");
                        } catch (MyPayException unmarshallingException) {
                            log.error("paaSILImportaDovuto error unmarshalling: [" + unmarshallingException.getMessage() + "]", unmarshallingException);
                            String errorMessage = "XML ricevuto per paaSILImportaDovuto non conforme all' XSD per ente [" + codIpaEnte + "]  XML Error: \n" +
                              jaxbTransformService.getDetailUnmarshalExceptionMessage(unmarshallingException, dovutiSecondariByteArrayList.get(0));
                            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
                            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_XML_NON_VALIDO_CODE, "XML dei dovuti Enti secondari non valido", errorMessage));
                            return paaSILImportaDovutoRisposta;
                        }

                        String datiSpecificiRiscossioneEnteSecondario = dovutiEntiSecondari.getDatiVersamentoEntiSecondari().getDatiSpecificiRiscossione();
                        if (StringUtils.isNotBlank(datiSpecificiRiscossioneEnteSecondario)) {
                            String codiceTassonomicoEnteSec = StringUtils.substringBeforeLast(datiSpecificiRiscossioneEnteSecondario, "/") + "/";
                            if (!tassonomiaService.ifExitsCodiceTassonomico(codiceTassonomicoEnteSec)) {
                                String errore = "paaSILImportaDovuto codice tassonomico dati specifici riscossione Ente Secodnario non valido: [" + datiSpecificiRiscossioneEnteSecondario + "]";
                                log.error(errore);
                                paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
                                paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_DATI_SPECIFICI_RISCOSSIONE_NON_VALIDO_CODE, errore, errore));
                                return paaSILImportaDovutoRisposta;
                            }
                        }
                    } else if (dovutiSecondariByteArrayList.size() > 1) {
                        log.error("paaSILImportaDovuto error recupero Enti secondari: [" + FaultCodeConstants.PAA_LIMITAZIONE_ENTI_SECONDARI_ERROR + "]");
                        String buffer = "XML ricevuto per paaSILImportaDovuto non conforme all' XSD per enti secondari [" + FaultCodeConstants.PAA_LIMITAZIONE_ENTI_SECONDARI_ERROR + "]";
                        paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
                        paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_LIMITAZIONE_ENTI_SECONDARI_ERROR, "XML dei dovuti Enti secondari supera il limite consentito da PagoPA.", buffer));
                        return paaSILImportaDovutoRisposta;
                    }

                    dovutoFunctionOut = this.callModificaFunction(ente.getMygovEnteId(), flusso.get().getMygovFlussoId(), 0, mygovAnagraficaStatoDovutoDaPagare.getMygovAnagraficaStatoId(),
                            null, identificativoUnivocoDovuto, identificativoUnivocoVersamento, now, tipoIdentificativoUnivoco,
                            codiceIdentificativoUnivoco, soggettoPagatore.getAnagraficaPagatore(), indirizzoPagatore,
                            civicoPagatore, cap, soggettoPagatore.getLocalitaPagatore(),
                            siglaProvincia, nazione,
                            soggettoPagatore.getEMailPagatore(), dataEsecuzionePagamento,
                            tipoVersamento, datiVersamento.getImportoSingoloVersamento().doubleValue(),
                            commissioneCaricoPA.doubleValue(), codTipo, causaleVersamento,
                            datiSpecificiRiscossione, utente.getMygovUtenteId(), bilancioString, Boolean.FALSE, flagGeneraIuv,
                            gpdIupdOriginal, gpdStatus,
                            ente.getCodiceFiscaleEnte(), true,
                            ente.getDeNomeEnte(), ente.getCodRpDatiVersDatiSingVersIbanAccredito());

                    if (dovutoFunctionOut != null && dovutoFunctionOut.getResult() == null) {

                        try {
                            /*
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
                                        dataEsecuzionePagamento,  /* Indica la data entro la quale si richiede che venga effettuato il pagamento. */
                                        datiVersamento.getImportoSingoloVersamento(),      /* L'importo relativo alla somma da versare. */
                                        causaleVersamento,            /* Testo libero a disposizione dell'Ente per descrivere le motivazioni del pagamento. */
                                        "I"
                                );

                            }
                        } catch (Exception e) {
                            log.warn("Errore nella chiamata al service per l'avvisatura digitale.", e);
                        }

                        Dovuto dovutoPri = getByIudEnte(identificativoUnivocoDovuto, ente.getCodIpaEnte());

                        if(dovutiEntiSecondari != null) {
                            dovMulti = insertDovutoMultibenef(dovutoPri, dovutiEntiSecondari);
                        }

                        //sync to GPD
                        if (dovutoToGpdSync) {
                            if (gpdEnabled){
                                if (gpdIupdOriginal== null)
                                    gpdService.newDebtPosition(ente.getCodiceFiscaleEnte(), dovutoPri.getGpdIupd(), identificativoUnivocoVersamento, importoSingoloVersamento.doubleValue(),
                                            dovutoPri.getCodRpSoggPagIdUnivPagCodiceIdUnivoco(), tipoIdentificativoUnivoco.charAt(0), soggettoPagatore.getAnagraficaPagatore(), indirizzoPagatore, civicoPagatore,
                                            cap, soggettoPagatore.getLocalitaPagatore(), siglaProvincia, nazione, soggettoPagatore.getEMailPagatore(),
                                            ente.getCodIpaEnte(), ente.getDeNomeEnte(), now, causaleVersamento, dataEsecuzionePagamento, ente.getCodRpDatiVersDatiSingVersIbanAccredito(), codTipo,
                                            dovutoPri.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(), Optional.ofNullable(dovMulti));
                                else
                                    gpdService.updateDebtPosition(ente.getCodiceFiscaleEnte(), dovutoPri.getGpdIupd(), identificativoUnivocoVersamento, importoSingoloVersamento.doubleValue(),
                                        dovutoPri.getCodRpSoggPagIdUnivPagCodiceIdUnivoco(), tipoIdentificativoUnivoco.charAt(0),soggettoPagatore.getAnagraficaPagatore(), indirizzoPagatore, civicoPagatore,
                                        cap, soggettoPagatore.getLocalitaPagatore(), siglaProvincia, nazione, soggettoPagatore.getEMailPagatore(),
                                        ente.getCodIpaEnte(), ente.getDeNomeEnte(), now, causaleVersamento, dataEsecuzionePagamento, ente.getCodRpDatiVersDatiSingVersIbanAccredito(), codTipo,
                                        dovutoPri.getDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(), Optional.ofNullable(dovMulti));
                            } else if (gpdPreload)
                                gpdService.managePreload('M', dovutoPri, dovutoCorrente.getMygovDovutoId());
                        }
                    }

                    break;
                case "A": // annulla: insert_mygov_dovuto_elaborato

                    Dovuto dovutoToDelete = getByIudEnte(identificativoUnivocoDovuto, ente.getCodIpaEnte());
                    if (dovutoToDelete == null) {
                        throw new MyPayException("Dovuto non trovato per IUD [" + identificativoUnivocoDovuto + "] e codice IPA Ente [" + ente.getCodIpaEnte() + "]");
                    }
                    if (!Constants.STATO_DOVUTO_DA_PAGARE.equals(dovutoToDelete.getMygovAnagraficaStatoId().getCodStato()))
                        throw new ValidatorException(messageSource.getMessage("pa.debiti.nonAnnullabile", null, Locale.ITALY));
                    dovutoFunctionOut = dovutoElaboratoService.callAnnullaFunction(ente.getMygovEnteId(), flusso.get().getMygovFlussoId(), 0, mygovAnagraficaStatoDovutoAnnullato.getMygovAnagraficaStatoId(),
                            null, identificativoUnivocoDovuto, identificativoUnivocoVersamento, now, "-", tipoIdentificativoUnivoco,
                            codiceIdentificativoUnivoco, soggettoPagatore.getAnagraficaPagatore(), indirizzoPagatore,
                            civicoPagatore, cap, soggettoPagatore.getLocalitaPagatore(),
                            siglaProvincia, nazione,
                            soggettoPagatore.getEMailPagatore(), dataEsecuzionePagamento,
                            tipoVersamento, datiVersamento.getImportoSingoloVersamento().doubleValue(),
                            commissioneCaricoPA.doubleValue(), codTipo, causaleVersamento,
                            datiSpecificiRiscossione, utente.getMygovUtenteId(), true, dovutoToDelete.getGpdIupd(), null);

                    if ((gpdEnabled || gpdPreload) && (dovutoFunctionOut == null || StringUtils.isBlank(dovutoFunctionOut.getResult()))) {
                        if(gpdEnabled && StringUtils.isNotBlank(dovutoToDelete.getGpdIupd()))
                            gpdService.deleteDebtPosition(dovutoToDelete.getNestedEnte().getCodiceFiscaleEnte(), dovutoToDelete.getGpdIupd(), dovutoToDelete.getCodIuv());
                        else if (gpdPreload) {
                            gpdService.managePreload('A',dovutoToDelete, null);
                        }
                    }

                    /*
                     * Multi-beneficiary IUV management if defined (IUV_MULTI_10)
                     * NOTA: L'annullamento di un dovuto multibeneficiario avviene al richiamo della callAnnullaFunction
                     */

                    break;
            }
        } catch (Exception e) {
            String msg = "paaSILImportaDovuto: Errore import dovuto IUV ["
                    + identificativoUnivocoVersamento + "] e IUD [" + identificativoUnivocoDovuto + "]: "
                    + "Errore generazione iuv: " + e.getMessage();
            log.error(msg, e);
            paaSILImportaDovutoRisposta.setEsito(FaultCodeConstants.ESITO_KO);
            paaSILImportaDovutoRisposta.setFault(this.getFaultBean(codIpaEnte, FaultCodeConstants.PAA_SYSTEM_ERROR, msg, null));
            return paaSILImportaDovutoRisposta;
        }

        if (dovutoFunctionOut != null && dovutoFunctionOut.getResult() != null) {
            log.error("errore dovutoFunctionOut: {}", dovutoFunctionOut);
            String msg = "paaSILImportaDovuto: Errore import dovuto IUV ["
                    + identificativoUnivocoVersamento + "] e IUD [" + identificativoUnivocoDovuto + "]: "
                    + dovutoFunctionOut.getResultDesc();
            log.error(msg + ": {}", dovutoFunctionOut);
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
            if (dovuti.size() != 1) {
                throw new MyPayException("il numero di dovuti per ente [" + ente.getCodIpaEnte() + "] e iuv [" + identificativoUnivocoVersamento + "] è: " + dovuti.size());
            } else {
                String dovutoId = "" + dovuti.get(0).getMygovDovutoId();
                String securityTokenAvviso = jwtTokenUtil.generateSecurityToken(null, dovutoId, 3600L * 24 * 365 * 2); //expire in 2 years
                paaSILImportaDovutoRisposta.setUrlFileAvviso(landingService.getUrlDownloadAvviso(dovutoId, securityTokenAvviso));
            }

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
    public int updateStato(List<Long> mygovCarrelloIdList, Long mygovAnagraficaStatoId) {
        return dovutoDao.updateStatus(mygovCarrelloIdList, mygovAnagraficaStatoId);
    }

    public void validateAndNormalizeAnagraficaPagatore(AnagraficaPagatore.TIPO tipo, AnagraficaPagatore anagraficaPagatore, boolean allowCfAnonimo) throws ValidatorException {
        if (anagraficaPagatore == null || StringUtils.isBlank(anagraficaPagatore.getAnagrafica()))
            throw new ValidatorException(messageSource.getMessage("pa.anagrafica" + tipo + ".anagraficaNonValida", null, Locale.ITALY));

        if (anagraficaPagatore.getTipoIdentificativoUnivoco() == Character.MIN_VALUE)
            anagraficaPagatore.setTipoIdentificativoUnivoco(Constants.TIPOIDENTIFICATIVOUNIVOCO_F.charAt(0));

        if (!ArrayUtils.contains(Constants.TIPOIDENTIFICATIVOUNIVOCO_VALID_VALUES, anagraficaPagatore.getTipoIdentificativoUnivoco()))
            throw new ValidatorException(messageSource.getMessage("pa.anagrafica" + tipo + ".tipoIdentificativoUnivocoNonValido", null, Locale.ITALY));

        if (StringUtils.isBlank(anagraficaPagatore.getCodiceIdentificativoUnivoco()))
            anagraficaPagatore.setCodiceIdentificativoUnivoco(Constants.CODICE_FISCALE_ANONIMO);

        if (!Utilities.isValidCodIdUnivocoConAnonimo(allowCfAnonimo,
                Character.toString(anagraficaPagatore.getTipoIdentificativoUnivoco()), anagraficaPagatore.getCodiceIdentificativoUnivoco())) {
            throw new ValidatorException(messageSource.getMessage("pa.anagrafica" + tipo + ".codiceIdentificativoUnivocoNonValido", null, Locale.ITALY));
        }

        if (StringUtils.isBlank(anagraficaPagatore.getEmail()) && tipo.equals(AnagraficaPagatore.TIPO.Versante) ||
                StringUtils.isNotBlank(anagraficaPagatore.getEmail()) && !Utilities.isValidEmail(anagraficaPagatore.getEmail()))
            throw new ValidatorException(messageSource.getMessage("pa.anagrafica" + tipo + ".emailNonValida", null, Locale.ITALY));

        if (anagraficaPagatore.getNazioneId() != null) {
            NazioneTo nazione = locationService.getNazione(anagraficaPagatore.getNazioneId());
            if (nazione == null)
                throw new ValidatorException(messageSource.getMessage("pa.anagrafica" + tipo + ".nazioneNonValida", null, Locale.ITALY));
            anagraficaPagatore.setNazione(nazione.getCodiceIsoAlpha2());
        }

        if (anagraficaPagatore.getProvinciaId() != null) {
            ProvinciaTo provincia = locationService.getProvincia(anagraficaPagatore.getProvinciaId());
            if (provincia == null || !StringUtils.equals(anagraficaPagatore.getNazione(), "IT"))
                throw new ValidatorException(messageSource.getMessage("pa.anagrafica" + tipo + ".provinciaNonValida", null, Locale.ITALY));
            anagraficaPagatore.setProvincia(provincia.getSigla());
        }

        if (anagraficaPagatore.getLocalitaId() != null) {
            ComuneTo comune = locationService.getComune(anagraficaPagatore.getLocalitaId());
            if (comune == null || StringUtils.isBlank(anagraficaPagatore.getProvincia()) || !StringUtils.equals(anagraficaPagatore.getNazione(), "IT"))
                throw new ValidatorException(messageSource.getMessage("pa.anagrafica" + tipo + ".localitaNonValida", null, Locale.ITALY));
            anagraficaPagatore.setLocalita(comune.getComune());
        }
        if (StringUtils.isNotBlank(anagraficaPagatore.getCap())) {
            //if (StringUtils.isBlank(anagraficaPagatore.getNazione()))
            //	throw new ValidatorException(messageSource.getMessage("pa.anagrafica"+tipo+".nazioneNonValida", null, Locale.ITALY));
            //else
            if (!Utilities.isValidCAP(anagraficaPagatore.getCap(), anagraficaPagatore.getNazione()))
                throw new ValidatorException(messageSource.getMessage("pa.anagrafica" + tipo + ".capNonValido", null, Locale.ITALY));
        }


        if (StringUtils.isNotBlank(anagraficaPagatore.getIndirizzo()) && !Utilities.validaIndirizzoAnagrafica(anagraficaPagatore.getIndirizzo(), false))
            throw new ValidatorException(messageSource.getMessage("pa.anagrafica" + tipo + ".indirizzoNonValido.psp", null, Locale.ITALY));

        if (StringUtils.isNotBlank(anagraficaPagatore.getCivico()) && !Utilities.validaCivicoAnagrafica(anagraficaPagatore.getCivico(), false))
            throw new ValidatorException(messageSource.getMessage("pa.anagrafica" + tipo + ".civicoNonValido.psp", null, Locale.ITALY));
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
        log.info("idSession [{}] has been added to Dovuto [{}]", idSession, mygovDovutoId);
    }

    public Optional<DovutoMultibeneficiario> getOptionalDovMultibenefByIdDovuto(Long idDovuto) {
        return Optional.ofNullable(dovutoMultibenDao.getByIdDovuto(idDovuto));
    }

    public List<Dovuto> getDovutiScadutiByEnteTipoData(final String codIpaEnte, final List<String> tipoDovutoList, final LocalDate dtScadenza) {
        return dovutoDao.getDovutiScadutiByEnteTipoData(codIpaEnte, tipoDovutoList, dtScadenza);
    }

    public DovutoMultibeneficiarioTo getMultibeneficiarioToByIdDovuto(Long idDovuto) {
        return dovutoMultibenDao.getDovutoMultibeneficiarioByIdDovuto(idDovuto);
    }

    public DovutoMultibeneficiario getDovMultibenefByIdDovuto(Long idDovuto) {
        return dovutoMultibenDao.getByIdDovuto(idDovuto);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteDovMultibenef(DovutoMultibeneficiario dovutoMultibeneficiario) {
        return dovutoMultibenDao.delete(dovutoMultibeneficiario);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteDovMultibenefByIdDovuto(Long idDovuto) {
        return dovutoMultibenDao.deleteByIdDovuto(idDovuto);
    }

    private void validateDovutoMultibeneficiario(DovutoMultibeneficiarioTo dovutoMB) {
        if (StringUtils.isNotBlank(dovutoMB.getCodiceIdentificativoUnivoco()) && !Utilities.isValidCFOrPIVA(dovutoMB.getCodiceIdentificativoUnivoco()))
            throw new ValidatorException("Codice Fiscale o Partita Iva dell'Ente Secondario è invalido");
        if (StringUtils.isNotBlank(dovutoMB.getIbanAddebito()) && !Utilities.isValidIban(dovutoMB.getIbanAddebito()))
            throw new ValidatorException("IBAN dell'Ente Secondario è invalido");
        if (StringUtils.isBlank(dovutoMB.getDatiSpecificiRiscossione()))
            throw new ValidatorException("Dati specifici riscossione del dell'Ente Secondario non valorizzato");
        else {
            String codiceTassonomicoEnteSec = StringUtils.substringBeforeLast(dovutoMB.getDatiSpecificiRiscossione(), "/") + "/";
            if (!tassonomiaService.ifExitsCodiceTassonomico(codiceTassonomicoEnteSec)) {
                throw new ValidatorException("Codice tassonomico dei dati specifici riscossione dell'Ente Secondario non presente in archivio [" + codiceTassonomicoEnteSec + "]");
            }
        }

        try {
            if (new BigDecimal(dovutoMB.getImportoSecondario()).compareTo(BigDecimal.ZERO) < 1)
                throw new ValidatorException("invalid importo secondario value");
        } catch (Exception e) {
            throw new ValidatorException("invalid importo secondario");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public BigDecimal getImportoSingoloVerdamentoMBByIdDovuto(Long idDovuto) {
        return dovutoMultibenDao.getImportoDovutoMbByIdDovuto(idDovuto);
    }

    public DovutoMultibeneficiario insertDovutoMultibenef(Dovuto dovuto, DovutiEntiSecondari dovutiEntiSecondari) {

        DovutoMultibeneficiario dovMultibenef = new DovutoMultibeneficiario();

        dovMultibenef.setDeRpEnteBenefDenominazioneBeneficiario(dovutiEntiSecondari.getDatiVersamentoEntiSecondari().getDenominazioneBeneficiario());
        dovMultibenef.setCodiceFiscaleEnte(dovutiEntiSecondari.getDatiVersamentoEntiSecondari().getCodiceFiscaleBeneficiario());
        dovMultibenef.setCodIuv(dovuto.getCodIuv());
        dovMultibenef.setCodIud(dovuto.getCodIud());

        dovMultibenef.setNumRpDatiVersDatiSingVersImportoSingoloVersamento(dovutiEntiSecondari.getDatiVersamentoEntiSecondari().getImportoSingoloVersamento());

        dovMultibenef.setDeRpEnteBenefCivicoBeneficiario(dovutiEntiSecondari.getDatiVersamentoEntiSecondari().getCivicoBeneficiario());
        dovMultibenef.setDeRpEnteBenefIndirizzoBeneficiario(dovutiEntiSecondari.getDatiVersamentoEntiSecondari().getIndirizzoBeneficiario());
        dovMultibenef.setDeRpEnteBenefLocalitaBeneficiario(dovutiEntiSecondari.getDatiVersamentoEntiSecondari().getLocalitaBeneficiario());
        dovMultibenef.setDeRpEnteBenefProvinciaBeneficiario(dovutiEntiSecondari.getDatiVersamentoEntiSecondari().getProvinciaBeneficiario());
        dovMultibenef.setCodRpEnteBenefNazioneBeneficiario(dovutiEntiSecondari.getDatiVersamentoEntiSecondari().getNazioneBeneficiario());
        dovMultibenef.setCodRpEnteBenefCapBeneficiario(dovutiEntiSecondari.getDatiVersamentoEntiSecondari().getCapBeneficiario());
        dovMultibenef.setCodRpDatiVersDatiSingVersIbanAccredito(dovutiEntiSecondari.getDatiVersamentoEntiSecondari().getIbanAccreditoBeneficiario());
        dovMultibenef.setDeRpDatiVersDatiSingVersDatiSpecificiRiscossione(dovutiEntiSecondari.getDatiVersamentoEntiSecondari().getDatiSpecificiRiscossione());
        dovMultibenef.setDeRpDatiVersDatiSingVersCausaleVersamento(dovutiEntiSecondari.getDatiVersamentoEntiSecondari().getCausaleVersamento());
        dovMultibenef.setMygovDovutoId(dovuto);

        Date now = new Date();
        dovMultibenef.setDtCreazione(now);
        dovMultibenef.setDtUltimaModifica(now);

        long newId = dovutoMultibenDao.insert(dovMultibenef);
        dovMultibenef.toBuilder().mygovDovutoMultibeneficiarioId(newId).build();

        return dovMultibenef;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public ImportFlussoTo importDovutoImportFlusso(ImportFlussoTo importFlusso, Ente ente, Utente utente, FileWriter fileErrori, String versione) throws IOException {
        Character gpdStatus = null;

        //Creo gli oggetti per poter richiamare function e validazione
        DovutoFunctionOut dovutoFunctionOut = null;
        String codErrori = "";
        String deErrori = "";
        boolean validazioneFlusso = true;
        Date now = new Date();
        try {
            switch (importFlusso.getAzione()) {
                case "I": // inserimento: insert_mygov_dovuto
                    // SPAC#45
                    if (importFlusso.getProvinciaPagatore() != null) {
                        importFlusso.setProvinciaPagatore(importFlusso.getProvinciaPagatore().toUpperCase());
                    }

                    Long statusInInsert = mygovAnagraficaStatoDovutoDaPagare.getMygovAnagraficaStatoId();
                    if (gpdEnabled && importFlusso.getCodIuv() != null && !importFlusso.getCodIuv().isEmpty()) {
                        gpdStatus = Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA;
                        statusInInsert = mygovAnagraficaStatoDovutoPredisposto.getMygovAnagraficaStatoId();
                    }

                    //Richiamo la function
                    dovutoFunctionOut = this.callInsertFunction(ente.getMygovEnteId(), importFlusso.getIdFlusso().longValue(), Integer.valueOf(importFlusso.getNumRigaFlusso()), statusInInsert,
                            null, importFlusso.getIUD(), importFlusso.getCodIuv(), now, now,
                            importFlusso.getTipoIdentificativoUnivoco(), importFlusso.getCodiceIdentificativoUnivoco(),
                            importFlusso.getAnagraficaPagatore(), importFlusso.getIndirizzoPagatore(), importFlusso.getCivicoPagatore(),
                            importFlusso.getCapPagatore(), importFlusso.getLocalitaPagatore(),
                            importFlusso.getProvinciaPagatore(), importFlusso.getNazionePagatore(),
                            importFlusso.getEmailPagatore(), importFlusso.getDataScadenzaPagamento() != null ? importFlusso.getDataScadenzaPagamento() : null,
                            importFlusso.getTipoVersamento(), importFlusso.getImportoDovuto(),
                            importFlusso.getCommissioneCaricoPa(), importFlusso.getTipoDovuto(), importFlusso.getCausaleVersamento(),
                            importFlusso.getDatiSpecificiRiscossione(), utente.getMygovUtenteId(), importFlusso.getBilancio(), Boolean.FALSE, Boolean.parseBoolean(importFlusso.getFlagGeneraIuv()), ente.getCodiceFiscaleEnte(), true,
                            ente.getDeNomeEnte(), ente.getCodRpDatiVersDatiSingVersIbanAccredito(), gpdStatus);
                    break;
                case "M": // modifica: modify_mygov_dovuto

                    Dovuto dovutoCorrente = getByIudEnte(importFlusso.getIUD(), ente.getCodIpaEnte());
                    if (dovutoCorrente == null) {
                        throw new MyPayException("Dovuto non trovato per IUD [" + importFlusso.getIUD() + "] e codice IPA Ente [" + ente.getCodIpaEnte() + "]");
                    }

                    String gpdIupd = dovutoCorrente.getGpdIupd();
                    Long dovutoId = dovutoCorrente.getMygovDovutoId();
                    Long statusInUpdate = mygovAnagraficaStatoDovutoDaPagare.getMygovAnagraficaStatoId();

                    if (gpdEnabled) {
                        Character gpdStatusCorrente = dovutoCorrente.getGpdStatus();
                        if (dovutoCorrente.getMygovAnagraficaStatoId().equals(mygovAnagraficaStatoDovutoPredisposto.getMygovAnagraficaStatoId())) {
                            // se il dovuto è predisposto non si può modificare
                            throw new MyPayException("Dovuto [" + dovutoId + "] " +
                                    " predisposto per l'invio a pagoPA ma non ancora sincronizzato: impossibile modificarlo");
                        }
                        if (importFlusso.getCodIuv() != null && !importFlusso.getCodIuv().isEmpty()) {
                            if (gpdStatusCorrente == null) {
                                gpdStatus = Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_PREDISP_CON_PAGOPA;
                            } else {
                                gpdStatus = Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_UPDATE_SU_PAGOPA;
                            }
                            statusInUpdate = mygovAnagraficaStatoDovutoPredisposto.getMygovAnagraficaStatoId();
                        }
                    }

                    if (org.springframework.util.StringUtils.hasText(importFlusso.getFlagMultiBeneficiario()) &&
                            Boolean.parseBoolean(importFlusso.getFlagMultiBeneficiario())) {
                        this.deleteDovMultibenefByIdDovuto(dovutoId);
                    }
                    //Richiamo la function
                    dovutoFunctionOut = this.callModificaFunction(ente.getMygovEnteId(), importFlusso.getIdFlusso().longValue(), Integer.valueOf(importFlusso.getNumRigaFlusso()), statusInUpdate,
                            null, importFlusso.getIUD(), importFlusso.getCodIuv(), now, importFlusso.getTipoIdentificativoUnivoco(),
                            importFlusso.getCodiceIdentificativoUnivoco(), importFlusso.getAnagraficaPagatore(), importFlusso.getIndirizzoPagatore(),
                            importFlusso.getCivicoPagatore(), importFlusso.getCapPagatore(), importFlusso.getLocalitaPagatore(),
                            importFlusso.getProvinciaPagatore(), importFlusso.getNazionePagatore(),
                            importFlusso.getEmailPagatore(), importFlusso.getDataScadenzaPagamento() != null ? importFlusso.getDataScadenzaPagamento() : null,
                            importFlusso.getTipoVersamento(), importFlusso.getImportoDovuto(),
                            importFlusso.getCommissioneCaricoPa(), importFlusso.getTipoDovuto(), importFlusso.getCausaleVersamento(),
                            importFlusso.getDatiSpecificiRiscossione(), utente.getMygovUtenteId(), importFlusso.getBilancio(), Boolean.FALSE, Boolean.parseBoolean(importFlusso.getFlagGeneraIuv()),
                            gpdIupd, gpdStatus,
                            ente.getCodiceFiscaleEnte(), true, ente.getDeNomeEnte(), ente.getCodRpDatiVersDatiSingVersIbanAccredito());
                    break;
                case "A": // annulla: insert_mygov_dovuto_elaborato

                    Dovuto dovutoPri = getByIudEnte(importFlusso.getIUD(), ente.getCodIpaEnte());
                    if (dovutoPri == null) {
                        throw new MyPayException("Dovuto non trovato per IUD [" + importFlusso.getIUD() + "] e codice IPA Ente [" + ente.getCodIpaEnte() + "]");
                    }

                    if ((gpdEnabled) &&
                            importFlusso.getCodIuv() != null && !importFlusso.getCodIuv().isEmpty()
                            && dovutoPri.getGpdStatus() != null)
                    {
                        // si aggiorna semplicemente lo stato di sincronizzazione poichè la cancellazione vera e propria
                        // avviene subito dopo dell'invio del flusso a pagoPA

                        //Character gpdStatusCorrente = dovutoPri.getGpdStatus();
                        if (dovutoPri.getMygovAnagraficaStatoId().equals(mygovAnagraficaStatoDovutoPredisposto.getMygovAnagraficaStatoId())) {
                            // se il dovuto è già stato inviato a pagoPA ma non ancora sincronizzato
                            // per cui non si può modificare
                            throw new MyPayException("Dovuto [" + dovutoPri.getMygovDovutoId() + "] " +
                                    " predisposto per l'invio a pagoPA ma non ancora sincronizzato: impossibile annullarlo");
                        }

                        gpdIupd = dovutoPri.getGpdIupd();
                        gpdStatus = Constants.STATO_POS_DEBT_SINCRONIZZAZIONE_DELETE_SU_PAGOPA;

                        dovutoFunctionOut = this.callModificaFunction(ente.getMygovEnteId(), importFlusso.getIdFlusso().longValue(), Integer.valueOf(importFlusso.getNumRigaFlusso()), mygovAnagraficaStatoDovutoPredisposto.getMygovAnagraficaStatoId(),
                                null, importFlusso.getIUD(), importFlusso.getCodIuv(), now, importFlusso.getTipoIdentificativoUnivoco(),
                                importFlusso.getCodiceIdentificativoUnivoco(), importFlusso.getAnagraficaPagatore(), importFlusso.getIndirizzoPagatore(),
                                importFlusso.getCivicoPagatore(), importFlusso.getCapPagatore(), importFlusso.getLocalitaPagatore(),
                                importFlusso.getProvinciaPagatore(), importFlusso.getNazionePagatore(),
                                importFlusso.getEmailPagatore(), importFlusso.getDataScadenzaPagamento() != null ? importFlusso.getDataScadenzaPagamento() : null,
                                importFlusso.getTipoVersamento(), importFlusso.getImportoDovuto(),
                                importFlusso.getCommissioneCaricoPa(), importFlusso.getTipoDovuto(), importFlusso.getCausaleVersamento(),
                                importFlusso.getDatiSpecificiRiscossione(), utente.getMygovUtenteId(), importFlusso.getBilancio(), Boolean.FALSE, Boolean.parseBoolean(importFlusso.getFlagGeneraIuv()),
                                gpdIupd, gpdStatus,
                                ente.getCodiceFiscaleEnte(), true, ente.getDeNomeEnte(), ente.getCodRpDatiVersDatiSingVersIbanAccredito());

                    } else {
                        // Vecchia gestione pari pari
                        if (org.springframework.util.StringUtils.hasText(importFlusso.getFlagMultiBeneficiario()) &&
                                Boolean.parseBoolean(importFlusso.getFlagMultiBeneficiario())) {

                            this.deleteDovMultibenefByIdDovuto(dovutoPri.getMygovDovutoId());
                        }

                        //Richiamo la function
                        dovutoFunctionOut = dovutoElaboratoService.callAnnullaFunction(ente.getMygovEnteId(), importFlusso.getIdFlusso().longValue(), Integer.valueOf(importFlusso.getNumRigaFlusso()), mygovAnagraficaStatoDovutoAnnullato.getMygovAnagraficaStatoId(),
                                null, importFlusso.getIUD(), importFlusso.getCodIuv(), now, "-", importFlusso.getTipoIdentificativoUnivoco(),
                                importFlusso.getCodiceIdentificativoUnivoco(), importFlusso.getAnagraficaPagatore(), importFlusso.getIndirizzoPagatore(),
                                importFlusso.getCivicoPagatore(), importFlusso.getCapPagatore(), importFlusso.getLocalitaPagatore(),
                                importFlusso.getProvinciaPagatore(), importFlusso.getNazionePagatore(),
                                importFlusso.getEmailPagatore(), importFlusso.getDataScadenzaPagamento() != null ? importFlusso.getDataScadenzaPagamento() : null,
                                importFlusso.getTipoVersamento(), importFlusso.getImportoDovuto(),
                                importFlusso.getCommissioneCaricoPa(), importFlusso.getTipoDovuto(), importFlusso.getCausaleVersamento(),
                                importFlusso.getDatiSpecificiRiscossione(), utente.getMygovUtenteId(), true, dovutoPri.getGpdIupd(), null);
                    }
                    break;
            }
        } catch (Exception e) {
            //TALEND - DBERROR1
            String msg = "ImportFlussoTaskService :: importDovutoImportFlusso :: ERROR :: Errore import dovuto IUV ["
                    + importFlusso.getCodIuv() + "] e IUD [" + importFlusso.getIUD() + "]: "
                    + "Errore generazione iuv: " + e.getMessage();
            log.error(msg, e);
            codErrori = "PAA_IMPORT_ERROR";
            deErrori = deErrori + "|" + StringUtils.abbreviate("CODICE " + codErrori + " DESCRIZIONE " + e.getMessage(), 512);
            validazioneFlusso = false;
        }

        if (dovutoFunctionOut != null && dovutoFunctionOut.getResult() != null) {
            log.error("ImportFlussoTaskService :: importDovutoImportFlusso :: ERROR :: dovutoFunctionOut: {}", dovutoFunctionOut);
            String msg = "ImportFlussoTaskService :: importDovutoImportFlusso :: ERROR :: Errore import dovuto IUV ["
                    + importFlusso.getCodIuv() + "] e IUD [" + importFlusso.getIUD() + "]: "
                    + dovutoFunctionOut.getResultDesc();
            log.error(msg + ": {}", dovutoFunctionOut);
            codErrori = codErrori + "|" + dovutoFunctionOut.getResult();
            deErrori = deErrori + "|" + dovutoFunctionOut.getResultDesc();
            validazioneFlusso = false;
        } else {
            /*
             * Multi-beneficiary IUV management if defined (IUV_MULTI_10)
             * Controllo se il dovuto è un dovuto multibeneficiario, perchè è solo per questi che devo inserire le informazioni
             * nella tabella mygov_dovuto_multibeneficiario;
             */

            if (validazioneFlusso && Boolean.parseBoolean(importFlusso.getFlagMultiBeneficiario()) &&
                    (importFlusso.getAzione().equalsIgnoreCase("I") ||
                            importFlusso.getAzione().equalsIgnoreCase("M"))) {
                try {
                    DovutiEntiSecondari dovutiEntiSecondari = new DovutiEntiSecondari();
                    CtDatiVersamentoDovutiEntiSecondari vers = new CtDatiVersamentoDovutiEntiSecondari();
                    vers.setDenominazioneBeneficiario(importFlusso.getDenominazioneEnteSecondario());
                    vers.setCodiceFiscaleBeneficiario(importFlusso.getCodiceFiscaleEnteSecondario());
                    vers.setIbanAccreditoBeneficiario(importFlusso.getIbanAccreditoEnteSecondario());
                    vers.setIndirizzoBeneficiario(importFlusso.getIndirizzoEnteSecondario());
                    vers.setCivicoBeneficiario(importFlusso.getCivicoEnteSecondario());
                    vers.setCapBeneficiario(importFlusso.getCapEnteSecondario());
                    vers.setLocalitaBeneficiario(importFlusso.getLocalitaEnteSecondario());
                    vers.setProvinciaBeneficiario(importFlusso.getProvinciaEnteSecondario());
                    vers.setNazioneBeneficiario(importFlusso.getNazioneEnteSecondario());
                    vers.setDatiSpecificiRiscossione(importFlusso.getDatiSpecificiRiscossioneEnteSecondario());
                    vers.setCausaleVersamento(importFlusso.getCausaleVersamentoEnteSecondario());
                    vers.setImportoSingoloVersamento(BigDecimal.valueOf(importFlusso.getImportoVersamentoEnteSecondario()));
                    dovutiEntiSecondari.setDatiVersamentoEntiSecondari(vers);
                    dovutiEntiSecondari.setVersioneOggetto("6.0");
                    Dovuto dovutoPri = getByIudEnte(importFlusso.getIUD(), ente.getCodIpaEnte());
                    insertDovutoMultibenef(dovutoPri, dovutiEntiSecondari);
                } catch (Exception e) {
                    log.error("ImportFlussoTaskService :: importDovutoImportFlusso :: ERROR :: CtDatiVersamentoDovutiEntiSecondari: {}", e.getMessage());
                    String msg = "ImportFlussoTaskService :: importDovutoImportFlusso :: ERROR :: Errore import dovuto IUV ["
                            + importFlusso.getCodIuv() + "] e IUD [" + importFlusso.getIUD() + "]";
                    log.error("ImportFlussoTaskService :: importDovutoImportFlusso :: ERROR :: Errore nel recupero dei dovuti Enti secondari.");
                    log.error(msg + ": {}", e.getMessage());
                    codErrori = codErrori + "|" + "ERROR elaborateDovutiMultiBenef";
                    deErrori = deErrori + "|" + e.getMessage();
                    validazioneFlusso = false;
                }
                if (importFlusso.getAzione().equalsIgnoreCase("M")) {
                    //todo legare il dovuto_elaborato annullato al nuovo dovuto_multibeneficiario_elaborato
                }
            }
        }
        if (!validazioneFlusso) {
            importFlusso.setCodErrore(codErrori);
            importFlusso.setDeErrore(deErrori);
            String rigaErrore = importFlusso.getNumRigaFlusso() + ";" + codErrori + ";" + deErrori;
            fileErrori.append(rigaErrore);
        }
        return importFlusso;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public String assignIuvAndIupd(List<CartItem> items, String iuv, String iupd) {
        List<Long> ids = items.stream().map(CartItem::getId).collect(Collectors.toList());
        int updatedRec = dovutoDao.assignIuvAndIupd(ids, iuv, iupd);
        if (updatedRec < 1) {
            throw new MyPayException("Errore interno aggiornamento  dovuto");
        }
        log.info("Dovuto/i [" + String.join("|", ids.iterator() + "] just assigned to IUV [" + iuv + "]"));
        return iuv;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public List<Dovuto> getOlderByIuvVolatileAndState(int scadenzaCarrelloMinutiElaborazione, Long statoDovutoPagabileId, int rowLimit) {
        return dovutoDao.getOlderByIuvVolatileAndState(scadenzaCarrelloMinutiElaborazione, statoDovutoPagabileId, rowLimit);
    }

    @Transactional(propagation = Propagation.NESTED)
    public void eraseCheckoutCartsExpired(Dovuto dovuto) {
        if(!dovuto.isFlgIuvVolatile())
            throw new MyPayException("error eraseCheckoutCartsExpired: dovuto with ID["+dovuto.getMygovDovutoId()+"] has not iuvVolatile");
        //retrieve ente if missing
        if(dovuto.getNestedEnte()==null || StringUtils.isEmpty(dovuto.getNestedEnte().getCodiceFiscaleEnte())){
            if(dovuto.getMygovFlussoId()!=null && dovuto.getMygovFlussoId().getMygovFlussoId()!=null){
                //retrieve ente from flusso
                Flusso flusso = flussoService.getById(dovuto.getMygovFlussoId().getMygovFlussoId());
                dovuto.setNestedEnte(flusso.getMygovEnteId());
            } else {
                //retrieve completely dovuto, including ente
                dovuto = getById(dovuto.getMygovDovutoId());
            }
        }
        log.debug("process expired iuvVolatile for Ente[{}/{}] IUV[{}] IUD[{}]",
          dovuto.getNestedEnte().getCodIpaEnte(), dovuto.getNestedEnte().getCodiceFiscaleEnte(), dovuto.getCodIuv(), dovuto.getCodIud());
        dovutoMultibenDao.deleteByIdDovuto(dovuto.getMygovDovutoId());
        dovutoDao.deleteById(dovuto.getMygovDovutoId());
        if (Constants.TIPO_DOVUTO_MARCA_BOLLO_DIGITALE.equals(dovuto.getCodTipoDovuto()))
            datiMarcaBolloDigitaleService.remove(dovuto.getMygovDatiMarcaBolloDigitaleId());
        //remove from GPD and Preload
        if (gpdEnabled && StringUtils.isNotBlank(dovuto.getGpdIupd())) {
            gpdService.deleteDebtPosition(dovuto.getNestedEnte().getCodiceFiscaleEnte(), dovuto.getGpdIupd(), dovuto.getCodIuv());
        } else if (gpdPreload) {
            gpdService.managePreload('A',dovuto, null);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteByIupd( Long enteId, List<String> iupdList) {
        List<Long> ids = dovutoDao.getGetByIupdAndEnteId(iupdList, enteId);
        delete(ids);
    }

}
