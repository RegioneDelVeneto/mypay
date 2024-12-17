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

import it.regioneveneto.mygov.payment.mypay4.dao.NodoFespDao;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RPT_Conservazione;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RT_Conservazione;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class NodoFespService {

    @Autowired
    NodoFespDao nodoFespDao;

    public List<String> getListaRPT(String codiceFiscaleEnte, Date dtInizioEstrazione, Date dtFineEstrazione) {

        List<RPT_Conservazione> result = nodoFespDao.getListaRPT(codiceFiscaleEnte,dtInizioEstrazione,dtFineEstrazione);
        List<String> retList = new ArrayList<String>();
        //Aggiungo la riga con i nomi colonne
        retList.add("xml_rpt;data_ora_messaggio_richiesta;causale_versamento;tipo_soggetto_pagatore;"
                + "anagrafica_pagatore;id_univoco_pagatore;email_pagatore;nominativo_beneficiario;id_univoco_beneficiario;"
                + "email_beneficiario;id_aggregazione;id_unico_versante;anagrafica_versante;IUV;codice_contesto_pagamento;");
        for (  RPT_Conservazione rptObject : result) {

            //Object[] objects=(Object[])object;
           // String rpt = (String) objects[0];
            byte[] base64 = Base64.encodeBase64(rptObject.getRptXML().getBytes());
            String rowString  = new String(base64) + ";";
//            for (int i = 1; i<15; i++) {
//                rowString = rowString +
//                        (objects[i] != null ? objects[i] : "")
//                        + ";";
//            }
            String dataOraMessaggio = rptObject.getDataRegistrazione().toString();
            rowString = addField(rowString, dataOraMessaggio);
            rowString = addField(rowString, rptObject.getIdentificativo());
            rowString = addField(rowString, rptObject.getOggetto());
            rowString = addField(rowString, rptObject.getTipoSoggettoPagatore());
            rowString = addField(rowString, rptObject.getNominativoPagatore());
            rowString = addField(rowString, rptObject.getIdentificativoPagatore());
            rowString = addField(rowString, rptObject.getIndirizzoRiferimentoPagatore());
            rowString = addField(rowString, rptObject.getNominativoBeneficiario());
            rowString = addField(rowString, rptObject.getIdentificativoBeneficiario());
            rowString = addField(rowString, rptObject.getIndirizzoRiferimentoBeneficiario());
            rowString = addField(rowString, rptObject.getIdAggregazione());
            rowString = addField(rowString, rptObject.getIdentificativoVersante());
            rowString = addField(rowString, rptObject.getNominativoVersante());
            rowString = addField(rowString, rptObject.getIdentificativoUnivocoVersamento());
            rowString = addField(rowString, rptObject.getCodiceContestoPagamento());
            retList.add(rowString);
        }
        return retList;
    }
    
    private String addField(String row ,String field) {
    	if (field == null) field = "";
    	row = row + field + ";";
    	return row;	
    }



    public List<String> getListaRT(final String codiceFiscaleEnte,final Date dtInizioEstrazione,final Date dtFineEstrazione){
        List<RT_Conservazione> result = nodoFespDao.getListaRT(codiceFiscaleEnte, dtInizioEstrazione, dtFineEstrazione);

        List<String> retList = new ArrayList<String>();
        //Aggiungo la riga con i nomi colonne
        retList.add("xml_rt;data_ora_messaggio_ricevuta;id_messaggio_ricevuta;causale_versamento;tipo_soggetto_pagatore;"
                + "anagrafica_pagatore;id_univoco_pagatore;email_pagatore;id_univoco_beneficiario;id_univoco_versante;"
                + "anagrafica_versante;IUV;esito_pagamento;codice_contesto_pagamento");
        for (  RT_Conservazione rtObject : result) {

        //    Object[] objects=(Object[])object;
       //     byte[] rt = (byte[]) objects[0];
        	byte[] rt = rtObject.getRtXML();
            byte[] base64 = Base64.encodeBase64(rt);
            String rowString  = new String(base64) + ";";
            String dataOraMessaggio = rtObject.getDataRegistrazione().toString();
            rowString = addField(rowString, dataOraMessaggio);
            rowString = addField(rowString, rtObject.getIdentificativo());
            rowString = addField(rowString, rtObject.getOggetto());
            rowString = addField(rowString, rtObject.getTipoSoggettoDestinatario());
            rowString = addField(rowString, rtObject.getNominativoDestinatario());
            rowString = addField(rowString, rtObject.getIdentificativoDestinatario());
            rowString = addField(rowString, rtObject.getIndirizzoRiferimentoDestinatario());
  
            rowString = addField(rowString, rtObject.getIdentificativoBeneficiario());
         
            rowString = addField(rowString, rtObject.getIdentificativoVersante());
            rowString = addField(rowString, rtObject.getNominativoVersante());
            rowString = addField(rowString, rtObject.getIdentificativoUnivocoVersamento());
            rowString = addField(rowString, rtObject.getEsitoPagamento());
            rowString = addField(rowString, rtObject.getCodiceContestoPagamento());
            retList.add(rowString);
        }
        return retList;
    }



}
