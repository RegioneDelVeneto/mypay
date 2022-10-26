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
package it.regioneveneto.mygov.payment.mypay4.ws.client.fesp.mock;

import gov.telematici.pagamenti.ws.nodospcpernodoregionale.IntestazioneCarrelloPPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.IntestazionePPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediCopiaRT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediCopiaRTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediElencoFlussiRendicontazioneRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediElencoQuadraturePA;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediElencoQuadraturePARisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediFlussoRendicontazione;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediFlussoRendicontazioneRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediInformativaPSP;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediInformativaPSPRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediListaPendentiRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediListaPendentiRPTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediQuadraturaPA;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediQuadraturaPARisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediSceltaWISP;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediSceltaWISPRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediStatoRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoChiediStatoRPTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaCarrelloRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaCarrelloRPTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRPT;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRPTRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRichiestaStorno;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRichiestaStornoRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRispostaRevoca;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.NodoInviaRispostaRevocaRisposta;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.TipoElencoFlussiRendicontazione;
import gov.telematici.pagamenti.ws.nodospcpernodoregionale.TipoIdRendicontazione;
import it.regioneveneto.mygov.payment.mypay4.ws.client.fesp.PagamentiTelematiciRPTClient;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.DateUtils;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.datatype.DatatypeFactory;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public class PagamentiTelematiciRPTMockClient extends PagamentiTelematiciRPTClient {
  private final DatatypeFactory datatypeFactory;
  public PagamentiTelematiciRPTMockClient(){
    try{
      this.datatypeFactory = DatatypeFactory.newInstance();
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  public NodoInviaRPTRisposta nodoInviaRPT(NodoInviaRPT request, IntestazionePPT header){
    throw new UnsupportedOperationException("not implemented in mock");
  }
  public NodoInviaCarrelloRPTRisposta nodoInviaCarrelloRPT(NodoInviaCarrelloRPT request, IntestazioneCarrelloPPT header){
    throw new UnsupportedOperationException("not implemented in mock");
  }
  public NodoChiediQuadraturaPARisposta nodoChiediQuadraturaPA(NodoChiediQuadraturaPA request){
    throw new UnsupportedOperationException("not implemented in mock");
  }
  public NodoInviaRichiestaStornoRisposta nodoInviaRichiestaStorno(NodoInviaRichiestaStorno request){
    throw new UnsupportedOperationException("not implemented in mock");
  }
  public NodoChiediElencoQuadraturePARisposta nodoChiediElencoQuadraturePA(NodoChiediElencoQuadraturePA request){
    throw new UnsupportedOperationException("not implemented in mock");
  }
  public NodoChiediListaPendentiRPTRisposta nodoChiediListaPendentiRPT(NodoChiediListaPendentiRPT request){
    throw new UnsupportedOperationException("not implemented in mock");
  }
  public NodoChiediCopiaRTRisposta nodoChiediCopiaRT(NodoChiediCopiaRT request){
    throw new UnsupportedOperationException("not implemented in mock");
  }
  public NodoChiediElencoFlussiRendicontazioneRisposta nodoChiediElencoFlussiRendicontazione(NodoChiediElencoFlussiRendicontazione request){
    NodoChiediElencoFlussiRendicontazioneRisposta response = new NodoChiediElencoFlussiRendicontazioneRisposta();
    TipoElencoFlussiRendicontazione elenco = new TipoElencoFlussiRendicontazione();
    response.setElencoFlussiRendicontazione(elenco);
    elenco.setTotRestituiti(RandomUtils.nextInt(1,10));
    for(int i=0;i<elenco.getTotRestituiti();i++){
      TipoIdRendicontazione tipoIdRendicontazione = new TipoIdRendicontazione();
      tipoIdRendicontazione.setIdentificativoFlusso(UUID.randomUUID().toString());
      Instant instant = DateUtils.addSeconds(new Date(), -RandomUtils.nextInt(1, 3600*24*31)).toInstant();
      tipoIdRendicontazione.setDataOraFlusso(datatypeFactory.newXMLGregorianCalendar(GregorianCalendar.from(instant.atZone(ZoneId.systemDefault()))));
      elenco.getIdRendicontaziones().add(tipoIdRendicontazione);
    }
    return response;
  }
  public NodoChiediInformativaPSPRisposta nodoChiediInformativaPSP(NodoChiediInformativaPSP request){
    throw new UnsupportedOperationException("not implemented in mock");
  }
  public NodoChiediStatoRPTRisposta nodoChiediStatoRPT(NodoChiediStatoRPT request){
    throw new UnsupportedOperationException("not implemented in mock");
  }
  public NodoChiediFlussoRendicontazioneRisposta nodoChiediFlussoRendicontazione(NodoChiediFlussoRendicontazione request){
    NodoChiediFlussoRendicontazioneRisposta response = new NodoChiediFlussoRendicontazioneRisposta();
    ByteArrayDataSource barrds = new ByteArrayDataSource(
        (Instant.now().toString()+"\n"+ ToStringBuilder.reflectionToString(request, ToStringStyle.MULTI_LINE_STYLE))
            .getBytes(StandardCharsets.UTF_8), "application/octet-stream");
    response.setXmlRendicontazione(new DataHandler(barrds));
    return response;
  }
  public NodoChiediSceltaWISPRisposta nodoChiediSceltaWISP(NodoChiediSceltaWISP request){
    throw new UnsupportedOperationException("not implemented in mock");
  }
  public NodoInviaRispostaRevocaRisposta nodoInviaRispostaRevoca(NodoInviaRispostaRevoca request){
    throw new UnsupportedOperationException("not implemented in mock");
  }

}
