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
package it.regioneveneto.mygov.payment.mypay4.service.fesp;

import it.gov.digitpa.schemas._2011.pagamenti.RPT;
import it.regioneveneto.mygov.payment.mypay4.dao.fesp.RPTConservazioneDao;
import it.regioneveneto.mygov.payment.mypay4.dao.fesp.RTConservazioneDao;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RPT_Conservazione;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RT_Conservazione;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RptRt;
import it.regioneveneto.mygov.payment.mypay4.service.common.JAXBTransformService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)
public class RPTConservazioneService {

    @Autowired
    private RPTConservazioneDao rptConservazioneDao;

    @Autowired
    private RTConservazioneDao rtConservazioneDao;
    
    @Autowired
    JAXBTransformService jaxbTransformService;

    public RPT_Conservazione insertRptConservazione(RPT ctRPT, RptRt rptRt, Ente enteProp) {

        RPT_Conservazione rptConservazione = RPT_Conservazione.builder()
                .rptRtEstrazioneId(0L)
                .mygovGiornaleId(0L)
                .mygovRptRtId(rptRt.getMygovRptRtId())
                .identificativoDominio(rptRt.getCodRptInviarptIdDominio())
                .identificativoUnivocoVersamento(rptRt.getCodRptInviarptIdUnivocoVersamento())
                .codiceContestoPagamento(rptRt.getCodRptInviarptCodiceContestoPagamento())
                .identificativo(rptRt.getCodRptIdMessaggioRichiesta())
                .rptXML(this.encodeRPT(ctRPT))
                .dataRegistrazione(rptRt.getDtRptDataOraMessaggioRichiesta() != null ? rptRt.getDtRptDataOraMessaggioRichiesta() : new Date())
               //.oggetto(ctRPT.getDatiVersamento().getDatiSingoloVersamentoArray(0).getCausaleVersamento())
                .oggetto(ctRPT.getDatiVersamento().getDatiSingoloVersamentos().get(0).getCausaleVersamento())
                .tipoSoggettoPagatore("P" + rptRt.getCodRptSoggPagIdUnivPagTipoIdUnivoco())
                .nominativoPagatore(rptRt.getDeRptSoggPagAnagraficaPagatore())
                .identificativoPagatore(rptRt.getCodRptSoggPagIdUnivPagCodiceIdUnivoco())
                .indirizzoRiferimentoPagatore(rptRt.getDeRptSoggPagEmailPagatore())
                .tipoSoggettoBeneficiario("P" + rptRt.getCodRptEnteBenefIdUnivBenefTipoIdUnivoco())
                .nominativoBeneficiario(rptRt.getDeRptEnteBenefDenominazioneBeneficiario())
                .identificativoBeneficiario(rptRt.getCodRptEnteBenefIdUnivBenefCodiceIdUnivoco())
                .indirizzoRiferimentoBeneficiario(enteProp.getEmailAmministratore())
                .idAggregazione(rptRt.getCodRptEnteBenefIdUnivBenefCodiceIdUnivoco() + "-" +
                        //ctRPT.getDatiVersamento().getDatiSingoloVersamentoArray(0).getDatiSpecificiRiscossione())
                        ctRPT.getDatiVersamento().getDatiSingoloVersamentos().get(0).getDatiSpecificiRiscossione())
                .identificativoVersante(rptRt.getCodRptSoggVersIdUnivVersCodiceIdUnivoco())
                .nominativoVersante(rptRt.getDeRptSoggVersAnagraficaVersante())
                .build();

        Long rptConservazioneId = rptConservazioneDao.insert(rptConservazione);
        rptConservazione.setRptConservazioneId(rptConservazioneId);

        return rptConservazione;
    }

    public RPT_Conservazione insertRptConservazione(RPT_Conservazione rptConservazione, String tipoDocumento) {
        rptConservazione.setTipoDocumento(tipoDocumento);
        Long rptConservazioneId = rptConservazioneDao.insert(rptConservazione);
        rptConservazione.setRptConservazioneId(rptConservazioneId);

        return rptConservazione;
    }



    public RT_Conservazione insertRtConservazione(Long mygovRptRtId, String codRtInviartIdDominio,
                                                  String codRtDatiPagIdUnivocoVersamento, String codRtDatiPagCodiceContestoPagamento,
                                                  String codRtIdMessaggioRicevuta, byte[] rtPayload, Date dtRtDataOraMessaggioRicevuta,
                                                  String deRtDatiPagDatiSingPagCausaleVersamento, String codRtSoggPagIdUnivPagTipoIdUnivoco,
                                                  String deRtSoggPagAnagraficaPagatore, String codRtSoggPagIdUnivPagCodiceIdUnivoco,
                                                  String deRtSoggPagEmailPagatore, String codRtEnteBenefIdUnivBenefCodiceIdUnivoco, String idAggregazione,
                                                  String codRtSoggVersIdUnivVersCodiceIdUnivocoString, String deRtSoggVersAnagraficaVersante,
                                                  String codRtDatiPagCodiceEsitoPagamento, String tipoDocumento) {

        if (codRtDatiPagCodiceEsitoPagamento != null && codRtDatiPagCodiceEsitoPagamento.equals("0")) {
            codRtDatiPagCodiceEsitoPagamento = "OK";
        }
        else {
            codRtDatiPagCodiceEsitoPagamento = "KO";
        }

        RT_Conservazione rtConservazione = RT_Conservazione.builder()
                .rptRtEstrazioneId(0L)
                .mygovRptRtId(mygovRptRtId)
                .identificativoDominio(StringUtils.abbreviate(codRtInviartIdDominio,35))
                .identificativoUnivocoVersamento(StringUtils.abbreviate(codRtDatiPagIdUnivocoVersamento,35))
                .codiceContestoPagamento(StringUtils.abbreviate(codRtDatiPagCodiceContestoPagamento, 35))
                .identificativo(StringUtils.abbreviate(codRtInviartIdDominio+"-"+codRtIdMessaggioRicevuta, 100))
                .rtXML(rtPayload)
                .dataRegistrazione(dtRtDataOraMessaggioRicevuta)
                .oggetto(StringUtils.abbreviate(deRtDatiPagDatiSingPagCausaleVersamento,140))
                .tipoSoggettoDestinatario("P"+codRtSoggPagIdUnivPagTipoIdUnivoco)
                .nominativoDestinatario(StringUtils.abbreviate(deRtSoggPagAnagraficaPagatore,70))
                .identificativoDestinatario(StringUtils.abbreviate(codRtSoggPagIdUnivPagCodiceIdUnivoco,35))
                .indirizzoRiferimentoDestinatario(StringUtils.abbreviate(deRtSoggPagEmailPagatore,256))
                .identificativoBeneficiario(StringUtils.abbreviate(codRtEnteBenefIdUnivBenefCodiceIdUnivoco,35))
                .idAggregazione(StringUtils.abbreviate(idAggregazione,140))
                .identificativoVersante(StringUtils.abbreviate(codRtSoggVersIdUnivVersCodiceIdUnivocoString,35))
                .nominativoVersante(StringUtils.abbreviate(deRtSoggVersAnagraficaVersante,70))
                .esitoPagamento(codRtDatiPagCodiceEsitoPagamento)
                .tipoDocumento(tipoDocumento)
                .build();

        Long rtConservazioneId = rtConservazioneDao.insert(rtConservazione);
        rtConservazione.setRtConservazioneId(rtConservazioneId);

        return rtConservazione;
    }





   // private String encodeRPT(it.gov.digitpa.schemas._2011.pagamenti.RPT CtRPT) {
        private String encodeRPT(RPT CtRPT) {
        	
        String RPT = null;
//
//        RPTDocument rptDoc = RPTDocument.FactoryRPT.newInstance();
//      
//        rptDoc.setRPT(CtRPT);
//
//        try {
//            //byteRPT = Base64.encodeBase64(rptDoc.toString().getBytes("UTF-8"));
//            RPT = rptDoc.toString();
//        } catch (Exception uee) {
//            throw new RuntimeException(
//                    "Failed to parse Ricevuta Pagamento Telematica ::: Exception :::", uee);
//        }
        try {
        	RPT = jaxbTransformService.marshalling(CtRPT, RPT.class);
        }
        catch (Exception uee) {
        	log.warn("Errore nel marshalling della RPT: "+uee, uee);
        }
        log.debug("RPT: "+RPT);
        return RPT;
    }
        
 

}
