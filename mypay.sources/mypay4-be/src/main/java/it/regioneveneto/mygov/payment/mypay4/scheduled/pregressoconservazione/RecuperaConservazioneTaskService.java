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
package it.regioneveneto.mygov.payment.mypay4.scheduled.pregressoconservazione;

import it.gov.pagopa.pagopa_api.pa.pafornode.PaGetPaymentRes;
import it.regioneveneto.mygov.payment.mypay4.AbstractApplication;
import it.regioneveneto.mygov.payment.mypay4.dao.DovutoElaboratoDao;
import it.regioneveneto.mygov.payment.mypay4.dao.ReceiptDao;
import it.regioneveneto.mygov.payment.mypay4.dao.fesp.GiornaleDao;
import it.regioneveneto.mygov.payment.mypay4.dao.fesp.RPTConservazioneDao;
import it.regioneveneto.mygov.payment.mypay4.dao.fesp.RTConservazioneDao;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoElaborato;
import it.regioneveneto.mygov.payment.mypay4.model.Receipt;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Giornale;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RPT_Conservazione;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RT_Conservazione;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@ConditionalOnProperty(name= AbstractApplication.NAME_KEY, havingValue= RecuperaConservazioneTaskApplication.NAME)
public class RecuperaConservazioneTaskService {

    @Resource
    private RecuperaConservazioneTaskService self;

    @Value("${task.recupera_conservazione.data_inizio}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInizioRecuperoConservazione;
    @Autowired
    private    ReceiptDao receiptDao;

    @Autowired
    private DovutoElaboratoDao dovutoElaboratoDao;

    @Autowired
    private GiornaleDao giornaleDao;

    @Autowired
    private RTConservazioneDao rtConservazioneDao;

    @Autowired
    private RPTConservazioneDao rptConservazioneDao;

    @Autowired
    private JAXBTransformService jaxbTransformService;
  //  @Autowired
   // private RPTConservazioneService rptConservazioneService;


    @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)
    public void recuperaConservazione() {
        List<Receipt> listaReceipt = receiptDao.getByDataInizio(dataInizioRecuperoConservazione);
        log.debug("Trovate receipt count: " + listaReceipt.size());

        listaReceipt.forEach(receipt -> {
            try {
                log.debug("Elaboro receipt key: " + receipt.getMygovReceiptId());



                log.debug("Receipt: " + receipt);
                log.debug("dovutoElaboratoCollegato: " + receipt.getMygovDovutoElaboratoId());
                DovutoElaborato dovutoElaborato = dovutoElaboratoDao.getById
                        (receipt.getMygovDovutoElaboratoId().getMygovDovutoElaboratoId());
                //Possono essere piu di 1 per multibeneficiario
                List<Giornale> listaGiornale = giornaleDao.getByCCPandTipoEvento(receipt.getReceiptId(), "paSendRT", "REQ");
                if (listaGiornale != null) {
                    listaGiornale.forEach(giornale -> {
                        Optional<RT_Conservazione> receiptInTable = rtConservazioneDao.getByIdentificativoAndDominio(
                                giornale.getIdentificativoDominio() + "-" +receipt.getReceiptId());
                        if (!receiptInTable.isPresent()) {
                                log.debug("inserisco in rt_conservazione receipt id: "+receipt.getMygovReceiptId());
                                self.insertConservazioneRT(receipt, giornale, dovutoElaborato);
                            }


                    });
                }



            } catch (Exception e) {
                log.error("Errore in rec Cons RT: " + e, e);
            }


        });

        // Parte RPT
        List<Giornale> giornaleListGetPaymentRes = giornaleDao.getByTipoAndSottoTipo("paGetPayment", "RES" , dataInizioRecuperoConservazione );
        log.debug("Trovate getPaRes count: " + giornaleListGetPaymentRes.size());
        giornaleListGetPaymentRes.forEach( giornale ->
        {
            try {
                log.debug("giornale ID:"+giornale.getMygovGiornaleId());
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader( giornale.getParametriSpecificiInterfaccia())));
                log.debug("doc" + this.domToString(doc));
                // Inizializza l'oggetto XPath
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();

                // Cerca il nodo in base al suo nome locale senza specificare il namespace
                XPathExpression expr = xpath.compile("//*[local-name()='paGetPaymentRes']");

                // Esegui la query XPath sul documento XML
                Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
                PaGetPaymentRes resPa = null;
                if (node != null) {

                    String paGetPaymentResContent = this.domToString(node);
                    log.debug("Contenuto del nodo paGetPaymentRes: " + paGetPaymentResContent);
                    resPa = jaxbTransformService.unmarshalling(
                            paGetPaymentResContent.getBytes(StandardCharsets.UTF_8), PaGetPaymentRes.class);
                } else {
                    log.error("Nodo paGetPaymentRes non trovato.");
                }
                if (resPa != null) {
                    log.debug("resPA: "+resPa.toString());
                    log.debug("Maresciallo: "+jaxbTransformService.marshalling(resPa, PaGetPaymentRes.class ));
                    if (resPa.getFault() != null) {
                        log.warn("E' un fault non lo conmsidero per conservazione");
                    }
                    else {
                        String identificativo = new StringBuilder().
                                append(resPa.getData().getTransferList().getTransfers().get(0).getFiscalCodePA()).
                                append("-").append(resPa.getData().getCreditorReferenceId()).append("-").
                                append(resPa.getData().getRetentionDate()).toString();
                        log.debug("Elaboro identificativo: " + identificativo);
                        Optional<RPT_Conservazione> getPaInTable = rptConservazioneDao.getByIdentificativo(identificativo);
                        if (!getPaInTable.isPresent()) {
                            log.debug("Inserisco identificativo: " + identificativo);
                            self.insertConservazioneRPT(giornale, resPa);
                        }
                    }
                }
            }
            catch (Exception e) {
                log.error("Errore in rec Cons RPT: "+e, e );
            }

        }
        );


    }

    @Transactional(transactionManager = "tmFesp", propagation = Propagation.NESTED)
    public void insertConservazioneRPT(Giornale giornale, PaGetPaymentRes resPa) {
        RPT_Conservazione rptConservazione = RPT_Conservazione.builder()
                .rptRtEstrazioneId(0L)
                .mygovGiornaleId(0L)
                .mygovRptRtId(0L)
                .identificativoDominio(resPa.getData().getTransferList().getTransfers().get(0).getFiscalCodePA())
                .identificativoUnivocoVersamento(resPa.getData().getCreditorReferenceId())
                .codiceContestoPagamento("N/A")
                .identificativo(resPa.getData().getTransferList().getTransfers().get(0).getFiscalCodePA()+
                        "-"+
                        resPa.getData().getCreditorReferenceId()+
                        "-"+
                        resPa.getData().getRetentionDate())
                .rptXML(giornale.getParametriSpecificiInterfaccia())
                .dataRegistrazione(resPa.getData().getRetentionDate().toGregorianCalendar().getTime())
                //.oggetto(ctRPT.getDatiVersamento().getDatiSingoloVersamentoArray(0).getCausaleVersamento())
                .oggetto(resPa.getData().getDescription())
                .tipoSoggettoPagatore("P" + resPa.getData().getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierType().value())
                .nominativoPagatore(resPa.getData().getDebtor().getFullName())
                .identificativoPagatore( resPa.getData().getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierValue())
                .indirizzoRiferimentoPagatore(resPa.getData().getDebtor().getEMail())
                .tipoSoggettoBeneficiario("PG")
                .nominativoBeneficiario(resPa.getData().getCompanyName())
                .identificativoBeneficiario(resPa.getData().getTransferList().getTransfers().get(0).getFiscalCodePA())
                .indirizzoRiferimentoBeneficiario("N/A")
                .idAggregazione(resPa.getData().getTransferList().getTransfers().get(0).getFiscalCodePA()+ "-" +
                        //ctRPT.getDatiVersamento().getDatiSingoloVersamentoArray(0).getDatiSpecificiRiscossione())
                        resPa.getData().getTransferList().getTransfers().get(0).getRemittanceInformation())
                .identificativoVersante(null)
                .nominativoVersante(null).tipoDocumento("paGetPayment")
                .build();

        log.debug("rptConservazione: "+rptConservazione);
       // log.debug("rptConservazioneDao: "+rptConservazioneDao);
        rptConservazioneDao.insert(rptConservazione);
    }

    @Transactional(transactionManager = "tmFesp", propagation = Propagation.NESTED)
    public void insertConservazioneRT(Receipt receipt, Giornale giornale, DovutoElaborato dovutoElaborato) {


            try {
                if (dovutoElaborato != null) {
                    RT_Conservazione rtConservazione = RT_Conservazione.builder()
                            .rptRtEstrazioneId(0L)
                            .mygovRptRtId(0L)
                            .identificativoDominio(giornale.getIdentificativoDominio())
                            .identificativoUnivocoVersamento(receipt.getCreditorReferenceId())
                            .codiceContestoPagamento("N/A")
                            .identificativo(giornale.getIdentificativoDominio()+"-"+receipt.getReceiptId())
                            .rtXML(giornale.getParametriSpecificiInterfaccia().getBytes(StandardCharsets.UTF_8))
                            .dataRegistrazione(receipt.getPaymentDateTime())
                            .oggetto(receipt.getDescription())
                            .tipoSoggettoDestinatario("P" + receipt.getUniqueIdentifierTypeDebtor())
                            .nominativoDestinatario(receipt.getFullNameDebtor())
                            .identificativoDestinatario(receipt.getUniqueIdentifierValueDebtor())
                            .indirizzoRiferimentoDestinatario(receipt.getEmailDebtor())
                            .identificativoBeneficiario(giornale.getIdentificativoDominio())
                            .idAggregazione(giornale.getIdentificativoDominio() + "-" +
                                    dovutoElaborato.getDeEDatiPagDatiSingPagCausaleVersamento()
                            )
                            .identificativoVersante(receipt.getUniqueIdentifierValuePayer())
                            .nominativoVersante(receipt.getFullNamePayer())
                            .esitoPagamento("OK")
                            .tipoDocumento("paSendRT")
                            .build();

                    Long rtConservazioneId = rtConservazioneDao.insert(rtConservazione);
                    log.debug("rtConservazioneId: "+rtConservazioneId);
                    rtConservazione.setRtConservazioneId(rtConservazioneId);
                }
            }
            catch (Exception e) {
                log.error("Errore in insertConservazione: " + e, e);
            }

    }

    @SneakyThrows
    private String domToString(Node node) {
        // Crea un oggetto TransformerFactory
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // Crea un oggetto DOMSource dal tuo Node
        DOMSource source = new DOMSource(node);

        // Crea un oggetto StreamResult per catturare l'output
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        // Trasforma il DOMSource in una stringa
        transformer.transform(source, result);

        // Estrai la stringa XML
        String xmlString = writer.toString();
        return xmlString;

    }
}
